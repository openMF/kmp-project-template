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
import kpt.core.data.currency.CurrencyRepository
import kpt.core.model.currency.ExchangeRates
import org.mobilenativefoundation.store.store5.Store
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.asScreenStream
import kpt.core.base.store.screen.combineContent
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.ui.viewmodel.BaseViewModel

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
    currencyRepository: CurrencyRepository,
    private val networkMonitor: NetworkMonitor,
    private val fetchedAtRepository: FetchedAtRepository,
    private val spotRateStore: Store<String, ExchangeRates>,
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
                val policy = if (isOnline) FetchPolicy.NETWORK_ONLY else FetchPolicy.CACHE_ONLY
                spotRateStore.asScreenStream(
                    key = "USD",
                    networkMonitor = networkMonitor,
                    fetchedAtRepository = fetchedAtRepository,
                    cacheKey = "currency:spotRate:USD",
                    scope = viewModelScope,
                    fetchPolicy = policy,
                ).state
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
        RatesAction.Retry -> stream.retry()
        RatesAction.Refresh -> stream.refresh()
    }
}

data class RatesLocalState(val searchQuery: String = "")
data class RatesDisplay(val base: String, val date: String, val rates: Map<String, Double>)

sealed interface RatesAction {
    data class Search(val query: String) : RatesAction
    data object Retry : RatesAction
    data object Refresh : RatesAction
}
