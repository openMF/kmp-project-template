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
import org.mifos.core.data.crypto.CryptoRepository
import org.mifos.core.model.crypto.CoinDetail
import template.core.base.store.screen.ScreenState
import template.core.base.ui.viewmodel.BaseViewModel

class CoinDetailViewModel(
    cryptoRepository: CryptoRepository,
    coinId: String,
) : BaseViewModel<Unit, Nothing, Nothing>(Unit) {

    private val stream = cryptoRepository.coinDetailStream(
        coinId = coinId,
        scope = viewModelScope,
    )

    val screenState: StateFlow<ScreenState<CoinDetail>> = stream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScreenState.Loading)

    fun onRetry() = stream.retry()

    override fun handleAction(action: Nothing) {}
}
