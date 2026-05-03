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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.mifos.core.data.repository.CryptoRepository
import org.mifos.core.model.fintech.CoinMarket
import template.core.base.store.PagingScreenStream
import template.core.base.store.ScreenState
import template.core.base.ui.BaseViewModel

class CryptoWatchlistViewModel(
    cryptoRepository: CryptoRepository,
) : BaseViewModel<Unit, CryptoEvent, CryptoAction>(Unit) {

    private val pagingStream: PagingScreenStream<CoinMarket> = cryptoRepository.coinMarketsStream(
        scope = viewModelScope,
        pageSize = 20,
    )

    val screenState: StateFlow<ScreenState<List<CoinMarket>>> = pagingStream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScreenState.Loading)
    val hasMore: StateFlow<Boolean> = pagingStream.hasMore
    val isLoadingMore: StateFlow<Boolean> = pagingStream.isLoadingMore

    fun onLoadMore() = pagingStream.loadNextPage()
    fun onRetry() = pagingStream.retry()
    fun onRefresh() = pagingStream.refresh()

    override fun handleAction(action: CryptoAction) {}
}

sealed class CryptoAction
sealed class CryptoEvent
