/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.currencyrates.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.mifos.core.data.repository.CurrencyRepository
import org.mifos.core.model.fintech.RateHistory
import org.mifos.core.model.fintech.RateHistoryKey
import template.core.base.store.ScreenState
import template.core.base.ui.BaseViewModel

class RateHistoryViewModel(
    currencyRepository: CurrencyRepository,
) : BaseViewModel<HistoryLocalState, HistoryEvent, HistoryAction>(HistoryLocalState()) {

    private val keyFlow = stateFlow.map { local ->
        RateHistoryKey(from = "USD", to = local.targetCurrency, days = local.periodDays)
    }.distinctUntilChanged()

    private val stream = currencyRepository.rateHistoryStream(
        keyFlow = keyFlow,
        scope = viewModelScope,
    )

    val screenState: StateFlow<ScreenState<RateHistory>> = stream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScreenState.Loading)

    fun onRetry() = stream.retry()

    override fun handleAction(action: HistoryAction) = when (action) {
        is HistoryAction.SelectCurrency -> updateState { copy(targetCurrency = action.code) }
        is HistoryAction.SelectPeriod -> updateState { copy(periodDays = action.days) }
    }
}

data class HistoryLocalState(val targetCurrency: String = "INR", val periodDays: Int = 30)
sealed class HistoryAction {
    data class SelectCurrency(val code: String) : HistoryAction()
    data class SelectPeriod(val days: Int) : HistoryAction()
}
sealed class HistoryEvent
