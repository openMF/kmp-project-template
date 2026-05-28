/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.bills.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.mifos.core.data.banking.BillReminderRepository
import org.mifos.core.model.banking.BillCategory
import org.mifos.core.model.banking.BillReminder
import org.mifos.core.model.banking.Recurrence
import org.mifos.core.platform.notification.bill.BillReminderSchedule
import org.mifos.feature.bills.domain.BillReminderRecurrence
import org.mifos.feature.bills.notification.BillNotificationGateway
import template.core.base.store.submit.SubmitOutbox
import template.core.base.ui.viewmodel.BaseDraftMutationViewModel
import kotlin.random.Random
import kotlin.time.Clock

/**
 * **Second multi-formKey `BaseDraftMutationViewModel` showcase** (after `EditLoanViewModel`).
 *
 * Each `EditBillReminderViewModel` instance is scoped to ONE bill — either an existing one
 * (passed `billId`) or a freshly-minted client-side UUID for "new". The `uniqueKey` baked
 * into the draft handler is that bill id, so N concurrent edits across N bills produce N
 * independent PENDING drafts in `framework_submit_drafts` — no formKey collision.
 *
 * `performSubmit` does two things:
 *  1. Upserts the bill to the local Room-backed repository.
 *  2. Hands a [BillReminderSchedule] to the platform scheduler for the **next** occurrence.
 *
 * Both happen in the same submit — if the scheduler's call to the OS fails (permission
 * denied, OS quota exhausted), the bill still persists; the in-app upcoming-bills
 * surface acts as the fallback reminder.
 */
class EditBillReminderViewModel(
    private val repository: BillReminderRepository,
    private val scheduler: BillNotificationGateway,
    private val billId: String?,
    outbox: SubmitOutbox<BillReminder>,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : BaseDraftMutationViewModel<BillReminder, BillReminder>(
    outbox = outbox,
    formKey = FORM_KEY,
    uniqueKey = billId ?: FORM_KEY_NEW,
    autoSaveDraft = true,
) {

    private val _formState = MutableStateFlow(initialFormState())
    val formState: StateFlow<BillReminderFormState> = _formState.asStateFlow()

    init {
        // If we were handed an existing billId, hydrate the form from the repo once.
        // We don't expose this as a Flow because the form is "owned" by the user from
        // that moment on — re-emitting committed rows would clobber in-progress edits.
        if (billId != null) {
            viewModelScope.launch {
                repository.getById(billId)?.let { existing ->
                    _formState.value = BillReminderFormState(
                        name = existing.name,
                        amount = existing.amount,
                        dueDay = existing.dueDay,
                        recurrence = existing.recurrence,
                        category = existing.category,
                        enabled = existing.enabled,
                        reminderDaysBefore = existing.reminderDaysBefore,
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _formState.update { it.copy(name = value) }
    }
    fun onAmountChange(value: Double) {
        _formState.update { it.copy(amount = value) }
    }
    fun onDueDayChange(value: Int) {
        _formState.update { it.copy(dueDay = value.coerceIn(MIN_DUE_DAY, MAX_DUE_DAY)) }
    }
    fun onRecurrenceChange(value: Recurrence) {
        _formState.update { it.copy(recurrence = value) }
    }
    fun onCategoryChange(value: BillCategory) {
        _formState.update { it.copy(category = value) }
    }
    fun onEnabledChange(value: Boolean) {
        _formState.update { it.copy(enabled = value) }
    }
    fun onReminderDaysBeforeChange(value: Int) {
        _formState.update { it.copy(reminderDaysBefore = value.coerceIn(0, MAX_REMINDER_DAYS_BEFORE)) }
    }

    /**
     * Validate, snapshot to a [BillReminder] payload, then delegate to the base's
     * `onSubmit(payload)`. Overload (zero-arg) — NOT an override of the inherited generic
     * `onSubmit(payload)`, so the screen can wire a plain click handler.
     */
    fun onSubmit() {
        val form = _formState.value
        if (form.name.isBlank() || form.amount <= 0.0) return
        val now = clock.now().toEpochMilliseconds()
        val id = billId ?: randomId()
        val payload = BillReminder(
            id = id,
            name = form.name.trim(),
            amount = form.amount,
            dueDay = form.dueDay,
            recurrence = form.recurrence,
            category = form.category,
            enabled = form.enabled,
            reminderDaysBefore = form.reminderDaysBefore,
            createdAtMs = now,
            updatedAtMs = now,
        )
        super.onSubmit(payload)
    }

    override fun onDismissResult() {
        super.onDismissResult()
        _formState.value = initialFormState()
    }

    override suspend fun performSubmit(payload: BillReminder): BillReminder {
        // 1. Local persistence — the only "remote" surface this feature has.
        repository.upsert(payload)
        // 2. OS notification — best-effort. Schedule only if (a) enabled, and (b) the
        //    next due-date math produces a positive triggerAtMs.
        if (payload.enabled) {
            val today = clock.todayIn(timeZone)
            val nextDue = BillReminderRecurrence.nextDueDate(payload, today)
            if (nextDue != null) {
                val triggerMs = BillReminderRecurrence.reminderTriggerMs(
                    nextDue = nextDue,
                    reminderDaysBefore = payload.reminderDaysBefore,
                    tz = timeZone,
                    clock = clock,
                )
                if (triggerMs != null) {
                    scheduler.schedule(
                        BillReminderSchedule(
                            billId = payload.id,
                            title = payload.name,
                            body = "Due in ${payload.reminderDaysBefore} day(s)",
                            triggerAtMs = triggerMs,
                        ),
                    )
                }
            }
        } else {
            // Disabled bills still persist (so the user can re-enable later) but no
            // notification should fire.
            scheduler.cancel(payload.id)
        }
        return payload
    }

    private fun initialFormState(): BillReminderFormState = BillReminderFormState()

    private fun randomId(): String {
        // Compact client-side ID; same pattern as SetPriceAlertViewModel pending UUID4.
        val chars = ('a'..'z') + ('0'..'9')
        return (1..ID_LENGTH).map { chars[Random.nextInt(chars.size)] }.joinToString("")
    }

    companion object {
        /** Stable form key shared across every bill — `uniqueKey` discriminates per row. */
        const val FORM_KEY: String = "bill"

        /** Sentinel `uniqueKey` used when the user is creating a fresh bill (no id yet). */
        const val FORM_KEY_NEW: String = "new"

        private const val MIN_DUE_DAY = 1
        private const val MAX_DUE_DAY = 31
        private const val MAX_REMINDER_DAYS_BEFORE = 30
        private const val ID_LENGTH = 16
    }
}

/**
 * UI form state — observable so the screen re-renders on every keystroke / dropdown change.
 * Defaults to "Electricity bill, $50, day 1, monthly, utilities, enabled, 1 day before".
 */
data class BillReminderFormState(
    val name: String = "",
    val amount: Double = 0.0,
    val dueDay: Int = 1,
    val recurrence: Recurrence = Recurrence.MONTHLY,
    val category: BillCategory = BillCategory.UTILITIES,
    val enabled: Boolean = true,
    val reminderDaysBefore: Int = 1,
)
