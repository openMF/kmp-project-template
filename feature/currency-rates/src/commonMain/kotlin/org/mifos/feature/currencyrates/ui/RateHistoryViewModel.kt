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
import org.mifos.core.data.currency.CurrencyRepository
import org.mifos.core.model.currency.RateHistory
import org.mifos.core.model.currency.RateHistoryKey
import template.core.base.store.freshness.FreshnessSignal
import template.core.base.store.screen.ScreenState
import template.core.base.ui.viewmodel.BaseViewModel

class RateHistoryViewModel(
    currencyRepository: CurrencyRepository,
) : BaseViewModel<HistoryLocalState, Nothing, HistoryAction>(HistoryLocalState()) {

    private val keyFlow = stateFlow.map { local ->
        RateHistoryKey(from = "USD", to = local.targetCurrency, days = local.periodDays)
    }.distinctUntilChanged()

    private val stream = currencyRepository.rateHistoryStream(
        keyFlow = keyFlow,
        scope = viewModelScope,
    )

    val screenState: StateFlow<ScreenState<RateHistory>> = stream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScreenState.Loading)

    /**
     * Per-card freshness signal. Input-type store: when the user changes period/currency
     * and the new key's fetch fails, the previous selection's data is preserved as a
     * stale fallback (via [ScreenDataStream.flatMapLatest] carry-forward) — the
     * freshness Flow surfaces a `VeryStale` band + categorised `lastError`, letting the
     * UI render an inline `RefreshStateChip` ("No network · Showing previous data ↺")
     * instead of a full-screen `NoNetwork`.
     */
    val freshness: StateFlow<FreshnessSignal> = stream.freshness
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FreshnessSignal.initial())

    fun onRetry() {
        trySendAction(HistoryAction.Retry)
    }

    override fun handleAction(action: HistoryAction) = when (action) {
        is HistoryAction.SelectCurrency -> updateState { copy(targetCurrency = action.code) }
        is HistoryAction.SelectPeriod -> updateState { copy(periodDays = action.days) }
        HistoryAction.Retry -> stream.retry()
    }
}

data class HistoryLocalState(val targetCurrency: String = "INR", val periodDays: Int = 30)

sealed interface HistoryAction {
    data class SelectCurrency(val code: String) : HistoryAction
    data class SelectPeriod(val days: Int) : HistoryAction
    data object Retry : HistoryAction
}
