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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kpt.core.data.economic.EconomicRatesRepository
import kpt.core.model.economic.InterestRateSeries
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel

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
