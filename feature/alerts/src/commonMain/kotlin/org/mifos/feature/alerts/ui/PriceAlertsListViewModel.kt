/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.alerts.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.mifos.core.data.alerts.AlertsRepository
import org.mifos.core.model.alerts.PriceAlert
import template.core.base.store.screen.DataFreshness
import template.core.base.store.screen.ScreenState
import template.core.base.ui.viewmodel.BaseViewModel

/**
 * Read-side ViewModel for [PriceAlertsListScreen].
 *
 * Observes [AlertsRepository.alertsStream] (committed alerts from the fake API)
 * and maps to [ScreenState]. Pending drafts are tracked at the form-handler
 * level — they surface on the form screen's resume UI rather than here.
 */
class PriceAlertsListViewModel(
    repository: AlertsRepository,
) : BaseViewModel<Unit, Nothing, Nothing>(Unit) {

    val screenState: StateFlow<ScreenState<List<PriceAlert>>> = repository.alertsStream()
        .map { alerts ->
            if (alerts.isEmpty()) {
                ScreenState.Empty
            } else {
                ScreenState.Content(data = alerts, freshness = DataFreshness.FRESH)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

    override fun handleAction(action: Nothing) {}
}
