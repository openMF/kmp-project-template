/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.crypto.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.crypto.CryptoRepository
import kpt.core.model.demo.crypto.CoinDetail

/**
 * ViewModel for the per-coin detail screen ([CoinDetailScreen]).
 *
 * Streams the full [CoinDetail] for [coinId] through the same NETWORK_WITH_CACHE
 * Store5 path (`CoinDetailStore` → `CryptoRepository.coinDetailStream`) that the
 * list screen's paging stream sits beside — turning the previously-orphaned
 * `coinDetailStream` into a screen-consumed read path.
 *
 * Mirrors `feature/rates`'s `InterestRateDetailViewModel` shape verbatim: the
 * repository builds the stream, the screen renders it directly via
 * `ScreenContent(stream = detail)`, and the ViewModel owns only the freshness
 * projection + retry/refresh dispatch.
 *
 * @param coinId CoinGecko coin id (e.g. `"bitcoin"`) — supplied by the nav route
 *   via `koinViewModel { parametersOf(coinId) }`.
 */
internal class CoinDetailViewModel(
    coinId: String,
    repository: CryptoRepository,
) : BaseViewModel<Unit, Nothing, CoinDetailAction>(Unit) {

    /** The repository-built stream — the screen renders it directly via `ScreenContent(stream)`. */
    val detail: ScreenDataStream<CoinDetail> = repository.coinDetailStream(
        coinId = coinId,
        scope = viewModelScope,
    )

    /**
     * Per-screen freshness — drives the TopAppBar [FreshnessIndicator] info icon.
     * Pure time-based staleness; network connectivity is rendered separately by
     * the global `ConnectivityBanner`.
     */
    val freshness: StateFlow<FreshnessSignal> = detail.freshness
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FreshnessSignal.initial())

    fun onRetry() {
        trySendAction(CoinDetailAction.Retry)
    }

    fun onRefresh() {
        trySendAction(CoinDetailAction.Refresh)
    }

    override fun handleAction(action: CoinDetailAction) = when (action) {
        CoinDetailAction.Retry, CoinDetailAction.Refresh -> detail.refresh()
    }
}

sealed interface CoinDetailAction {
    data object Retry : CoinDetailAction
    data object Refresh : CoinDetailAction
}
