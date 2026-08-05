/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.alerts.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.alerts.AlertsRepository
import kpt.core.model.demo.alerts.PriceAlert

/**
 * Read-side ViewModel for the price-alerts list. Consumes the repository's offline-local
 * [AlertsRepository.alertsStream] ([kpt.core.base.store.screen.ScreenDataStream]) `.state`; the
 * Store already decided Loading/Empty/Content, so the VM only marks the empty list. Deleting an
 * alert re-emits as soon as Room propagates.
 */
class AlertsListViewModel(
    private val repository: AlertsRepository,
) : BaseViewModel<Unit, Nothing, AlertsListAction>(Unit) {

    private val stream = repository.alertsStream(viewModelScope)

    val screenState: StateFlow<ScreenState<List<PriceAlert>>> = stream.state
        .emptyIfContent { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /** Re-run the read (no-op refresh for the offline-local store; wired for ScreenContent). */
    fun onRetry() = stream.retry()

    override fun handleAction(action: AlertsListAction) {
        when (action) {
            is AlertsListAction.Delete -> viewModelScope.launch { repository.deleteAlert(action.id) }
        }
    }

    /** Convenience emitter so the screen calls `onDelete(id)` instead of `trySendAction`. */
    fun onDelete(id: String) {
        trySendAction(AlertsListAction.Delete(id))
    }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/** One-shot actions accepted by [AlertsListViewModel]. */
sealed interface AlertsListAction {
    /** Delete a price alert. Idempotent — no-op if not present. */
    data class Delete(val id: String) : AlertsListAction
}
