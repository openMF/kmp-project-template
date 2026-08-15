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
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.combineContent
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.currency.CurrencyRepository
import kpt.core.model.demo.currency.ExchangeRates

/**
 * **Archetype showcase: CACHE_ONLY + NETWORK_ONLY**
 *
 * In addition to the regular exchange-rates stream (NETWORK_WITH_CACHE default),
 * this ViewModel demonstrates policy routing based on network status:
 * - Online  → [FetchPolicy.NETWORK_ONLY]  (always-fresh spot rate, no stale cache)
 * - Offline → [FetchPolicy.CACHE_ONLY]    (read cached value, never call API)
 *
 * The [spotConversionRate] property is the canonical reference implementation for
 * this archetype pattern. See [AppStoreRegistry.SpotRate] for the store registration.
 */
class CurrencyRatesViewModel(
    private val currencyRepository: CurrencyRepository,
    private val networkMonitor: NetworkMonitor,
) : BaseViewModel<RatesLocalState, Nothing, RatesAction>(RatesLocalState()) {

    private val stream = currencyRepository.exchangeRatesStream(
        baseCurrency = "USD",
        scope = viewModelScope,
    )

    val screenState: StateFlow<ScreenState<RatesDisplay>> = stream.state
        .combineContent(stateFlow) { rates, local, _ ->
            RatesDisplay(
                base = rates.base,
                date = rates.date,
                rates = rates.rates.filter {
                    local.searchQuery.isEmpty() || it.key.contains(local.searchQuery, true)
                },
            )
        }
        .emptyIfContent { it.rates.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScreenState.Loading)

    /**
     * Per-screen freshness — drives the [FreshnessIndicator] info icon in the TopAppBar.
     * Pure time-based staleness derived from the same StoreDataStream snapshot;
     * network connectivity is rendered separately by the global `ConnectivityBanner`.
     */
    val freshness: StateFlow<FreshnessSignal> = stream.freshness
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FreshnessSignal.initial(),
        )

    /**
     * **Archetype: CACHE_ONLY + NETWORK_ONLY**
     *
     * Spot conversion rate for the currently-selected currency pair (defaults to
     * "USD"). Policy is chosen based on live network connectivity:
     * - Online  → [FetchPolicy.NETWORK_ONLY]:  skip cache, always fetch fresh rate.
     * - Offline → [FetchPolicy.CACHE_ONLY]:    read from cache, never call the API
     *   (avoids an error-state flicker when the user is known to be offline).
     *
     * The stream is rebuilt via [flatMapLatest] whenever connectivity flips so the
     * displayed rate immediately reflects the right freshness contract.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val spotConversionRate: StateFlow<ScreenState<ExchangeRates>> =
        networkMonitor.networkStatus
            .map { status -> status is NetworkStatus.Available }
            .flatMapLatest { isOnline ->
                currencyRepository.spotRateStream(baseCurrency = "USD", online = isOnline, scope = viewModelScope).state
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ScreenState.Loading,
            )

    fun onRetry() {
        trySendAction(RatesAction.Retry)
    }

    fun onRefresh() {
        trySendAction(RatesAction.Refresh)
    }

    override fun handleAction(action: RatesAction) = when (action) {
        is RatesAction.Search -> updateState { copy(searchQuery = action.query) }
        is RatesAction.ConverterAmount -> updateState { copy(converterAmount = action.amount) }
        is RatesAction.ConverterTarget -> updateState { copy(converterTarget = action.code) }
        RatesAction.Retry -> stream.retry()
        RatesAction.Refresh -> stream.refresh()
    }
}

/**
 * @property searchQuery filter applied to the rate list.
 * @property converterAmount raw amount text typed into the converter card (kept as
 *   String so the field renders partial/empty input; parsed at render time).
 * @property converterTarget target currency code the converter converts the base
 *   ([spotConversionRate]'s USD base) into. Uppercased at lookup time.
 */
data class RatesLocalState(
    val searchQuery: String = "",
    val converterAmount: String = "1",
    val converterTarget: String = "EUR",
)

data class RatesDisplay(val base: String, val date: String, val rates: Map<String, Double>)

sealed interface RatesAction {
    data class Search(val query: String) : RatesAction
    data class ConverterAmount(val amount: String) : RatesAction
    data class ConverterTarget(val code: String) : RatesAction
    data object Retry : RatesAction
    data object Refresh : RatesAction
}
