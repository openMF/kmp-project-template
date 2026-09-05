/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.bills.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.combineContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.banking.BillReminderRepository
import kpt.core.model.demo.banking.BillReminder
import kpt.feature.bills.notification.BillNotificationGateway
import kotlin.time.Clock

/**
 * Read-side ViewModel for the Bill Reminders dashboard.
 *
 * Composes three reactive sources into one [BillRemindersUiState] via [combine]:
 *  - `observeAll()` — every reminder ordered by due-day,
 *  - `observeUpcoming(7)` — reminders due in the next week (drives the "Upcoming" tile),
 *  - `observeTotalUpcomingAmount(30)` — sum of amounts due in the next 30 days.
 *
 * Side-actions ([BillRemindersAction.MarkPaid], [BillRemindersAction.Delete],
 * [BillRemindersAction.ToggleEnabled]) hit the repository directly — the read-side state
 * re-emits as soon as Room propagates the change. Notifications are re-scheduled on
 * `MarkPaid` and cancelled on `Delete` so the OS state mirrors the user's intent.
 */
class BillRemindersListViewModel(
    private val repository: BillReminderRepository,
    private val scheduler: BillNotificationGateway,
    private val clock: Clock = Clock.System,
) : BaseViewModel<Unit, Nothing, BillRemindersAction>(Unit) {

    /**
     * Composite [ScreenState] for the dashboard. `Empty` when zero bills exist; `Content`
     * otherwise — the per-tile splits live inside [BillRemindersUiState].
     */
    private val stream = repository.billRemindersStream(viewModelScope)

    private val upcomingTiles = combine(
        repository.observeUpcoming(UPCOMING_WINDOW_DAYS),
        repository.observeTotalUpcomingAmount(TOTAL_WINDOW_DAYS),
    ) { upcoming, total -> upcoming to total }

    val screenState: StateFlow<ScreenState<BillRemindersUiState>> = stream.state
        .combineContent(upcomingTiles) { all, (upcoming, total), _ ->
            BillRemindersUiState(all = all, upcoming = upcoming, totalUpcomingAmount = total)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /** Re-run the read (no-op refresh for the offline-local store; wired for ScreenContent). */
    fun onRetry() = stream.retry()

    override fun handleAction(action: BillRemindersAction) {
        when (action) {
            is BillRemindersAction.MarkPaid -> viewModelScope.launch { markPaid(action.billId) }
            is BillRemindersAction.Delete -> viewModelScope.launch { delete(action.billId) }
            is BillRemindersAction.ToggleEnabled -> viewModelScope.launch { toggle(action.billId) }
        }
    }

    /** Convenience emitter so the screen calls `onMarkPaid(id)` instead of `trySendAction`. */
    fun onMarkPaid(billId: String) {
        trySendAction(BillRemindersAction.MarkPaid(billId))
    }

    /** Convenience emitter — see [onMarkPaid]. */
    fun onDelete(billId: String) {
        trySendAction(BillRemindersAction.Delete(billId))
    }

    /** Convenience emitter — see [onMarkPaid]. */
    fun onToggleEnabled(billId: String) {
        trySendAction(BillRemindersAction.ToggleEnabled(billId))
    }

    /**
     * Marks a bill paid. For recurring bills this is a domain no-op in the row itself
     * (the next-due-date is derived from `dueDay` + today, not stored), but we bump
     * `updatedAtMs` so the row sorts to the top of "recently touched" surfaces and the
     * notification scheduler re-registers a fresh request — pinning the contract even
     * before per-row "lastPaidAtMs" lands.
     */
    /**
     * One-shot read of ONE reminder THROUGH the store — the read half of a read-modify-write.
     * Was `repository.getById(...)`, which went straight to the DAO and bypassed the store read
     * path while the paired `upsert` went through the write store (the S5-2 split-read defect).
     */
    private suspend fun loadOne(billId: String): BillReminder? =
        (
            repository.billReminderDetailStream(billId, viewModelScope).state
                .firstOrNull { it is ScreenState.Content<BillReminder> || it is ScreenState.Empty }
                as? ScreenState.Content<BillReminder>
            )?.data

    private suspend fun markPaid(billId: String) {
        val existing = loadOne(billId) ?: return
        repository.upsert(existing.copy(updatedAtMs = clock.now().toEpochMilliseconds()))
        // Re-scheduling is the responsibility of [EditBillReminderViewModel] on the
        // happy path; here we just cancel-and-let-the-next-edit-or-app-launch reschedule.
        scheduler.cancel(billId)
    }

    private suspend fun delete(billId: String) {
        repository.delete(billId)
        scheduler.cancel(billId)
    }

    private suspend fun toggle(billId: String) {
        val existing = loadOne(billId) ?: return
        val flipped = existing.copy(
            enabled = !existing.enabled,
            updatedAtMs = clock.now().toEpochMilliseconds(),
        )
        repository.upsert(flipped)
        if (!flipped.enabled) {
            scheduler.cancel(billId)
        }
    }

    private companion object {
        const val UPCOMING_WINDOW_DAYS = 7
        const val TOTAL_WINDOW_DAYS = 30
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * Combined dashboard state — surfaced as the `data` slot of a single [ScreenState.Content].
 *
 * @property all Every bill reminder in due-day order.
 * @property upcoming Reminders due in the next [BillRemindersListViewModel.UPCOMING_WINDOW_DAYS] days.
 * @property totalUpcomingAmount Sum of [BillReminder.amount] for reminders due in the next
 *   [BillRemindersListViewModel.TOTAL_WINDOW_DAYS] days. Drives the summary tile.
 */
data class BillRemindersUiState(
    val all: List<BillReminder>,
    val upcoming: List<BillReminder>,
    val totalUpcomingAmount: Double,
)

/** One-shot actions accepted by [BillRemindersListViewModel]. */
sealed interface BillRemindersAction {
    /** Bump `updatedAtMs` + cancel the pending notification — the user has just paid this bill. */
    data class MarkPaid(val billId: String) : BillRemindersAction

    /** Permanently delete the reminder + cancel any pending notification. */
    data class Delete(val billId: String) : BillRemindersAction

    /** Flip the `enabled` flag — disabling cancels the pending notification but keeps the row. */
    data class ToggleEnabled(val billId: String) : BillRemindersAction
}
