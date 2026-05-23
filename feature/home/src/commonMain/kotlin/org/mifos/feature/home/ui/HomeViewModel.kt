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
import org.mifos.core.data.alerts.AlertsRepository
import org.mifos.core.data.crypto.CryptoRepository
import org.mifos.core.data.currency.CurrencyRepository
import org.mifos.core.data.watchlist.WatchlistItem
import org.mifos.core.data.watchlist.WatchlistRepository
import org.mifos.core.model.alerts.PriceAlert
import org.mifos.core.model.crypto.CoinMarket
import org.mifos.core.model.currency.ExchangeRates
import template.core.base.store.screen.DataFreshness
import template.core.base.store.screen.ScreenState
import template.core.base.ui.viewmodel.BaseViewModel

/**
 * **Canonical multi-source combine showcase.**
 *
 * Composes four independent reactive sources into a single UI state with
 * per-widget loading/empty/error/content slots. Each widget's state evolves
 * independently — one card can be Loading while another shows Content; pull-
 * to-refresh fans out to the network-backed streams concurrently.
 *
 * Sources split by character:
 *  - [pagingStream] and [exchangeRateStream] are network-backed `ScreenDataStream`s
 *    with their own Loading/Error/Content + freshness state.
 *  - [WatchlistRepository.watchlist] and [AlertsRepository.alertsStream] are
 *    purely local reactive `Flow`s — mapped here into `ScreenState.Empty` /
 *    `ScreenState.Content(freshness = FRESH)` (same projection used by
 *    `PersonalWatchlistViewModel`).
 */
class HomeViewModel(
    private val cryptoRepository: CryptoRepository,
    private val currencyRepository: CurrencyRepository,
    watchlistRepository: WatchlistRepository,
    alertsRepository: AlertsRepository,
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

        // Watchlist preview: pure-local Flow → ScreenState. No retry surface
        // (Room is the only source), so just Empty / Content.
        watchlistRepository.watchlist()
            .onEach { items ->
                val next = if (items.isEmpty()) {
                    ScreenState.Empty
                } else {
                    ScreenState.Content(data = items, freshness = DataFreshness.FRESH)
                }
                updateState { copy(watchlistPreview = next) }
            }
            .launchIn(viewModelScope)

        // Active alerts: same pure-local projection. The committed-alerts stream
        // is the canonical "what the user has set up" view; pending drafts are
        // surfaced at the form-handler level (see AlertsRepository docstring).
        alertsRepository.alertsStream()
            .onEach { alerts ->
                val next = if (alerts.isEmpty()) {
                    ScreenState.Empty
                } else {
                    ScreenState.Content(data = alerts, freshness = DataFreshness.FRESH)
                }
                updateState { copy(activeAlerts = next) }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: HomeAction) = when (action) {
        HomeAction.RefreshAll -> {
            pagingStream.refresh()
            exchangeRateStream.refresh()
            // Watchlist + Alerts are local reactive Flows — nothing to refresh.
        }
        HomeAction.RetryTopMovers -> pagingStream.retry()
        HomeAction.RetryExchangeRate -> exchangeRateStream.retry()
    }
}

/**
 * Aggregate state for the home dashboard.
 *
 * Each slot is an independent [ScreenState] so the screen can render per-card
 * Loading / Empty / Error / Content states.
 */
data class HomeUiState(
    val topMovers: ScreenState<List<CoinMarket>> = ScreenState.Loading,
    val exchangeRate: ScreenState<ExchangeRates> = ScreenState.Loading,
    val watchlistPreview: ScreenState<List<WatchlistItem>> = ScreenState.Loading,
    val activeAlerts: ScreenState<List<PriceAlert>> = ScreenState.Loading,
)

sealed interface HomeAction {
    /** Pull-to-refresh — fans out to every network-backed stream. */
    data object RefreshAll : HomeAction

    /** Retry just the Top Movers widget (after an error). */
    data object RetryTopMovers : HomeAction

    /** Retry just the Exchange Rate widget (after an error). */
    data object RetryExchangeRate : HomeAction
}
