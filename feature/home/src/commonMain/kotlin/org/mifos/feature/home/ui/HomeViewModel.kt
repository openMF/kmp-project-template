/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.mifos.core.data.crypto.CryptoRepository
import org.mifos.core.data.currency.CurrencyRepository
import org.mifos.core.model.crypto.CoinMarket
import org.mifos.core.model.currency.ExchangeRates
import template.core.base.store.screen.ScreenState
import template.core.base.ui.viewmodel.BaseViewModel

/**
 * **Canonical multi-source combine showcase.**
 *
 * Composes two independent `ScreenDataStream`s into a single UI state with
 * per-widget loading/error/content slots. Each widget's state evolves
 * independently — one card can be Loading while another shows Content; pull-
 * to-refresh fans out to both streams concurrently.
 *
 * Extension points for sub-plans 03 (Watchlist) and 04 (Alerts) widgets are
 * documented in [HomeUiState]; adding them is a small follow-up once
 * `WatchlistRepository` and `AlertsRepository` land on `dev`.
 */
class HomeViewModel(
    private val cryptoRepository: CryptoRepository,
    private val currencyRepository: CurrencyRepository,
) : BaseViewModel<HomeUiState, Nothing, HomeAction>(HomeUiState()) {

    // Take the first page of coin markets; we'll show the top 5 in the widget.
    private val pagingStream = cryptoRepository.coinMarketsStream(
        scope = viewModelScope,
        pageSize = 5,
    )

    private val exchangeRateStream = currencyRepository.exchangeRatesStream(
        baseCurrency = "USD",
        scope = viewModelScope,
    )

    init {
        // Top Movers widget: project the paging stream's state into a flat
        // ScreenState<List<CoinMarket>>, truncated to the first 5.
        pagingStream.state
            .onEach { state ->
                updateState { copy(topMovers = state) }
            }
            .launchIn(viewModelScope)

        // Exchange Rate widget: direct ScreenState<ExchangeRates>.
        exchangeRateStream.state
            .onEach { state ->
                updateState { copy(exchangeRate = state) }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: HomeAction) = when (action) {
        HomeAction.RefreshAll -> {
            pagingStream.refresh()
            exchangeRateStream.refresh()
        }
        HomeAction.RetryTopMovers -> pagingStream.retry()
        HomeAction.RetryExchangeRate -> exchangeRateStream.retry()
    }
}

/**
 * Aggregate state for the home dashboard.
 *
 * Each slot is an independent [ScreenState] so the screen can render per-card
 * Loading / Empty / Error / Content states. Sub-plans 03 + 04 will add two more
 * slots (`watchlistPreview`, `activeAlerts`) once their repositories land.
 */
data class HomeUiState(
    val topMovers: ScreenState<List<CoinMarket>> = ScreenState.Loading,
    val exchangeRate: ScreenState<ExchangeRates> = ScreenState.Loading,
)

sealed interface HomeAction {
    /** Pull-to-refresh — fans out to every stream. */
    data object RefreshAll : HomeAction

    /** Retry just the Top Movers widget (after an error). */
    data object RetryTopMovers : HomeAction

    /** Retry just the Exchange Rate widget (after an error). */
    data object RetryExchangeRate : HomeAction
}
