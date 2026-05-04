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
import org.mifos.core.data.repository.CryptoRepository
import org.mifos.core.model.fintech.CoinMarket
import template.core.base.store.PagingScreenStream
import template.core.base.ui.BaseViewModel

class CryptoWatchlistViewModel(
    cryptoRepository: CryptoRepository,
) : BaseViewModel<Unit, CryptoEvent, CryptoAction>(Unit) {

    /**
     * Exposed for [template.core.base.ui.PagingScreenContent] which observes the stream
     * directly (state, hasMore, isLoadingMore, loadMoreError) and drives load-more.
     */
    val pagingStream: PagingScreenStream<CoinMarket> = cryptoRepository.coinMarketsStream(
        scope = viewModelScope,
        pageSize = 20,
    )

    fun onRetry() = pagingStream.retry()
    fun onRefresh() = pagingStream.refresh()

    override fun handleAction(action: CryptoAction) {}
}

sealed class CryptoAction
sealed class CryptoEvent
