/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.crypto.ui

import androidx.lifecycle.viewModelScope
import org.mifos.core.data.crypto.CryptoRepository
import org.mifos.core.model.crypto.CoinMarket
import template.core.base.store.paging.PagingScreenStream
import template.core.base.ui.viewmodel.BaseViewModel

class CryptoWatchlistViewModel(
    cryptoRepository: CryptoRepository,
) : BaseViewModel<Unit, Nothing, CryptoWatchlistAction>(Unit) {

    /**
     * Exposed for [template.core.base.ui.PagingScreenContent] which observes the stream
     * directly (state, hasMore, isLoadingMore, loadMoreError) and drives load-more.
     * Paging state inherently spans multiple flows, so it stays as a field rather than
     * being folded into the inherited [stateFlow].
     */
    val pagingStream: PagingScreenStream<CoinMarket> = cryptoRepository.coinMarketsStream(
        scope = viewModelScope,
        pageSize = 20,
    )

    fun onRetry() {
        trySendAction(CryptoWatchlistAction.Retry)
    }

    fun onRefresh() {
        trySendAction(CryptoWatchlistAction.Refresh)
    }

    override fun handleAction(action: CryptoWatchlistAction) = when (action) {
        CryptoWatchlistAction.Retry -> pagingStream.retry()
        CryptoWatchlistAction.Refresh -> pagingStream.refresh()
    }
}

sealed interface CryptoWatchlistAction {
    data object Retry : CryptoWatchlistAction
    data object Refresh : CryptoWatchlistAction
}
