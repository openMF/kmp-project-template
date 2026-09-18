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
import kpt.core.base.store.paging.PagingScreenStream
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.crypto.CryptoRepository
import kpt.core.model.crypto.CoinMarket

/**
 * Read-side ViewModel for [CoinMarketsScreen].
 *
 * Scroll position retention is a Compose concern — `rememberLazyListState()` uses
 * `rememberSaveable` internally, so the list position survives tab-switch and
 * config-change via Navigation's state-holder and Android's saved-instance-state
 * Bundle. The ViewModel carries no retention plumbing.
 */
class CoinMarketsViewModel(
    private val repository: CryptoRepository,
) : BaseViewModel<Unit, Nothing, CoinMarketsAction>(Unit) {

    val pagingStream: PagingScreenStream<CoinMarket> = repository.coinMarketsStream(
        scope = viewModelScope,
        pageSize = DEFAULT_PAGE_SIZE,
    )

    /** Pull-to-refresh / initial retry. Resets the paging cursor to page 0. */
    fun retry() = pagingStream.refresh()

    override fun handleAction(action: CoinMarketsAction) = Unit

    private companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}

/** Placeholder action hierarchy — the read-only screen has no dispatch surface today. */
sealed interface CoinMarketsAction
