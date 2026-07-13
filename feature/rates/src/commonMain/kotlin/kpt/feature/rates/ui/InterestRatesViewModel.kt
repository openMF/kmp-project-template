/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.rates.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.freshness.FreshnessBand
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.economic.EconomicRatesRepository
import kpt.core.model.demo.economic.InterestRateSeries

/**
 * **B7 Interest Rate Tracker** — canonical `NETWORK_WITH_CACHE` + DataFreshness
 * showcase.
 *
 * Composes four independent FRED-backed reactive streams into a single dashboard
 * state with per-row Loading/Empty/Error/Content slots. Each row evolves
 * independently — one card may show Content while another is still Loading or
 * surfacing an error. Pull-to-refresh fans out to all four streams concurrently.
 *
 * Stream lifecycle is owned by the framework's `ScreenDataStream` (built on top
 * of Store5) — see [RateStreamFactory]. The 24h Store5 TTL means the first
 * subscription emits cached data immediately, then silently refreshes from FRED.
 */
internal class InterestRatesViewModel(
    streamFactory: RateStreamFactory,
) : BaseViewModel<RatesUiState, Nothing, RatesAction>(RatesUiState()) {

    /** Convenience constructor — uses the default repository-backed stream factory. */
    constructor(repository: EconomicRatesRepository) : this(DefaultRateStreamFactory(repository))

    private val fedFundsStream = streamFactory.open(RateSeriesCatalog.FedFunds, viewModelScope)
    private val primeStream = streamFactory.open(RateSeriesCatalog.Prime, viewModelScope)
    private val mortgage30YStream = streamFactory.open(RateSeriesCatalog.Mortgage30Y, viewModelScope)
    private val treasury10YStream = streamFactory.open(RateSeriesCatalog.Treasury10Y, viewModelScope)

    init {
        fedFundsStream.state
            .onEach { state -> updateState { copy(fedFunds = state) } }
            .launchIn(viewModelScope)

        primeStream.state
            .onEach { state -> updateState { copy(prime = state) } }
            .launchIn(viewModelScope)

        mortgage30YStream.state
            .onEach { state -> updateState { copy(mortgage30Y = state) } }
            .launchIn(viewModelScope)

        treasury10YStream.state
            .onEach { state -> updateState { copy(treasury10Y = state) } }
            .launchIn(viewModelScope)
    }

    /**
     * Per-screen aggregate freshness for the TopAppBar [FreshnessIndicator] — combines
     * the worst band across all four FRED series so the header indicator surfaces the
     * weakest signal. Per-row freshness is exposed via [fedFundsFreshness] /
     * [primeFreshness] / [mortgage30YFreshness] / [treasury10YFreshness] for the
     * per-card `RateRowCard` indicators.
     */
    val aggregateFreshness: StateFlow<FreshnessSignal> = combine(
        fedFundsStream.freshness,
        primeStream.freshness,
        mortgage30YStream.freshness,
        treasury10YStream.freshness,
    ) { fed, prime, mortgage, treasury ->
        listOf<FreshnessSignal>(fed, prime, mortgage, treasury)
            .maxByOrNull { it.band.severity() } ?: fed
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FreshnessSignal.initial(),
    )

    val fedFundsFreshness: StateFlow<FreshnessSignal> = fedFundsStream.freshness
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FreshnessSignal.initial())

    val primeFreshness: StateFlow<FreshnessSignal> = primeStream.freshness
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FreshnessSignal.initial())

    val mortgage30YFreshness: StateFlow<FreshnessSignal> = mortgage30YStream.freshness
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FreshnessSignal.initial())

    val treasury10YFreshness: StateFlow<FreshnessSignal> = treasury10YStream.freshness
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FreshnessSignal.initial())

    override fun handleAction(action: RatesAction) = when (action) {
        RatesAction.RefreshAll -> {
            fedFundsStream.refresh()
            primeStream.refresh()
            mortgage30YStream.refresh()
            treasury10YStream.refresh()
        }
        RatesAction.RetryFedFunds -> fedFundsStream.refresh()
        RatesAction.RetryPrime -> primeStream.refresh()
        RatesAction.RetryMortgage30Y -> mortgage30YStream.refresh()
        RatesAction.RetryTreasury10Y -> treasury10YStream.refresh()
    }

    /**
     * Worse-of-N severity ordering on [FreshnessBand] — used by [aggregateFreshness]
     * so the TopAppBar indicator surfaces the weakest signal across the four streams.
     * Mirrors the same private extension in `HomeViewModel`; if a third consumer
     * appears, lift to a shared helper in `core-base/store/freshness`.
     */
    private fun FreshnessBand.severity(): Int = when (this) {
        FreshnessBand.Initial -> 0
        FreshnessBand.Fresh -> 1
        FreshnessBand.Stale -> 2
        FreshnessBand.VeryStale -> 3
    }
}

/**
 * Aggregate state for the rate-tracker dashboard.
 *
 * Each slot is an independent [ScreenState] so the screen can render per-row
 * Loading / Empty / Error / Content transitions without spinning up a master
 * `combineScreenStates` reduce.
 */
data class RatesUiState(
    val fedFunds: ScreenState<InterestRateSeries> = ScreenState.Loading,
    val prime: ScreenState<InterestRateSeries> = ScreenState.Loading,
    val mortgage30Y: ScreenState<InterestRateSeries> = ScreenState.Loading,
    val treasury10Y: ScreenState<InterestRateSeries> = ScreenState.Loading,
)

sealed interface RatesAction {
    /** Pull-to-refresh — fans out to every backing stream. */
    data object RefreshAll : RatesAction

    /** Retry the Federal Funds Rate row (after a row-level error). */
    data object RetryFedFunds : RatesAction

    /** Retry the Prime Rate row. */
    data object RetryPrime : RatesAction

    /** Retry the 30-Year Mortgage row. */
    data object RetryMortgage30Y : RatesAction

    /** Retry the 10-Year Treasury row. */
    data object RetryTreasury10Y : RatesAction
}
