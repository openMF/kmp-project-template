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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.mifos.core.data.crypto.CryptoRepository
import org.mifos.core.model.crypto.CoinDetail
import template.core.base.store.screen.ScreenState
import template.core.base.ui.viewmodel.BaseViewModel

class CoinDetailViewModel(
    cryptoRepository: CryptoRepository,
    coinId: String,
) : BaseViewModel<ScreenState<CoinDetail>, Nothing, CoinDetailAction>(
    initialState = ScreenState.Loading,
) {

    private val stream = cryptoRepository.coinDetailStream(
        coinId = coinId,
        scope = viewModelScope,
    )

    /** Backward-compat alias for the inherited [stateFlow]. */
    val screenState: StateFlow<ScreenState<CoinDetail>> get() = stateFlow

    init {
        stream.state
            .onEach { newState -> updateState { newState } }
            .launchIn(viewModelScope)
    }

    fun onRetry() {
        trySendAction(CoinDetailAction.Retry)
    }

    override fun handleAction(action: CoinDetailAction) = when (action) {
        CoinDetailAction.Retry -> stream.retry()
    }
}

sealed interface CoinDetailAction {
    data object Retry : CoinDetailAction
}
