/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.crypto

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.paging.PagingScreenStream
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.crypto.CoinDetail
import kpt.core.model.crypto.CoinMarket

/**
 * Read surface for CoinGecko-backed crypto data.
 *
 * Returns `ScreenDataStream` / `PagingScreenStream`, never raw values: loading, empty, error and
 * no-network are decided in the Store, so no screen re-derives them.
 */
interface CryptoRepository {
    /** Streams the CoinGecko coin-markets list as a paged screen stream. */
    fun coinMarketsStream(scope: CoroutineScope, pageSize: Int = 20): PagingScreenStream<CoinMarket>

    /**
     * Offline-first stream for one coin's detail.
     *
     * @param coinId CoinGecko's coin id.
     * @param scope scope the underlying Store shares.
     */
    fun coinDetailStream(coinId: String, scope: CoroutineScope): ScreenDataStream<CoinDetail>
}
