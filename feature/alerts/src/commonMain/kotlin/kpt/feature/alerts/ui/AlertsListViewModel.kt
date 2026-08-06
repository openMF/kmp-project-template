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
import kotlinx.coroutines.launch
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.alerts.AlertsRepository
import kpt.core.model.demo.alerts.PriceAlert

/**
 * Read-side ViewModel for the price-alerts list. A pure passthrough — it exposes the repository's
 * offline-local [AlertsRepository.alertsStream] ([ScreenDataStream]) directly; the screen consumes
 * it via `ScreenContent(stream = viewModel.alerts)`, so state (Loading/Empty/Content) is owned by
 * the stream and never set here. Deleting an alert re-emits as soon as Room propagates.
 */
class AlertsListViewModel(
    private val repository: AlertsRepository,
) : BaseViewModel<Unit, Nothing, AlertsListAction>(Unit) {

    /** The repository-built stream (its `isEmpty` already yields Empty for a cleared list). */
    val alerts: ScreenDataStream<List<PriceAlert>> = repository.alertsStream(viewModelScope)

    override fun handleAction(action: AlertsListAction) {
        when (action) {
            is AlertsListAction.Delete -> viewModelScope.launch { repository.deleteAlert(action.id) }
        }
    }

    /** Convenience emitter so the screen calls `onDelete(id)` instead of `trySendAction`. */
    fun onDelete(id: String) {
        trySendAction(AlertsListAction.Delete(id))
    }
}

/** One-shot actions accepted by [AlertsListViewModel]. */
sealed interface AlertsListAction {
    /** Delete a price alert. Idempotent — no-op if not present. */
    data class Delete(val id: String) : AlertsListAction
}
