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
import kpt.core.base.store.paging.PagingCursor
import kpt.core.base.store.paging.PagingScreenStream
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.crypto.CoinDetail
import kpt.core.model.crypto.CoinMarket

interface CryptoRepository {
    /**
     * Streams the CoinGecko coin-markets list as a paged screen stream.
     *
     * @param initialCursorProvider Suspend lambda that yields the persisted
     *   [PagingCursor] for deep-scroll restore, or `null` for a first-time visit.
     *   Resolved inside the stream's `loadInitialPage` coroutine — callers must
     *   not synchronously read a durable store here (there is no `runBlocking` on
     *   Kotlin/JS or wasmJs). Defaults to `{ null }` for callers that do not need
     *   restore, preserving the pre-Phase-4 shape.
     */
    fun coinMarketsStream(
        scope: CoroutineScope,
        pageSize: Int = 20,
        initialCursorProvider: suspend () -> PagingCursor? = { null },
    ): PagingScreenStream<CoinMarket>

    fun coinDetailStream(coinId: String, scope: CoroutineScope): ScreenDataStream<CoinDetail>
}
