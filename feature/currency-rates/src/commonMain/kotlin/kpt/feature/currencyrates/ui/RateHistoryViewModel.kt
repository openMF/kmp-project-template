/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.currencyrates.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.currency.CurrencyRepository
import kpt.core.model.currency.RateHistory
import kpt.core.model.currency.RateHistoryKey

/**
 * Drives the historical chart. The pair and window form the store key, so changing either re-keys the stream rather
 * than filtering in memory.
 */
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

    /**
     * The series as a screen state. Re-keys whenever the pair or window changes, so a selection change produces a
     * fresh load rather than an in-memory filter of the previous one.
     */
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

    /** Retries the failed fetch. */
    fun onRetry() {
        trySendAction(HistoryAction.Retry)
    }

    override fun handleAction(action: HistoryAction) = when (action) {
        is HistoryAction.SelectCurrency -> updateState { copy(targetCurrency = action.code) }
        is HistoryAction.SelectPeriod -> updateState { copy(periodDays = action.days) }
        HistoryAction.Retry -> stream.retry()
    }
}

/**
 * The chart's selection — which pair and how far back.
 *
 * @property targetCurrency the quote currency to chart.
 * @property periodDays how far back to chart, in days.
 */
data class HistoryLocalState(val targetCurrency: String = "INR", val periodDays: Int = 30)

/** What the chart can be asked to do. */
sealed interface HistoryAction {
    /**
     * The target currency changed.
     *
     * @property code the target currency code.
     */
    data class SelectCurrency(val code: String) : HistoryAction

    /**
     * The window length changed. A different window is a different store key, not a filter.
     *
     * @property days the window length in days.
     */
    data class SelectPeriod(val days: Int) : HistoryAction

    /** Retry the failed fetch. */
    data object Retry : HistoryAction
}
