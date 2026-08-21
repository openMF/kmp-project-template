/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.crypto.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.paging.PageKey
import kpt.core.base.store.paging.PagingScreenStream
import kpt.core.base.store.paging.asPagingScreenStream
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.demo.crypto.CryptoRepository
import kpt.core.model.demo.crypto.CoinDetail
import kpt.core.model.demo.crypto.CoinMarket
import org.mobilenativefoundation.store.store5.Store

class CryptoRepositoryImpl(
    private val coinMarketsStore: Store<PageKey, List<CoinMarket>>,
    private val coinDetailStore: Store<String, CoinDetail>,
) : CryptoRepository {

    override fun coinMarketsStream(scope: CoroutineScope, pageSize: Int): PagingScreenStream<CoinMarket> =
        coinMarketsStore.asPagingScreenStream(
            cacheKey = "crypto:coinMarkets",
            scope = scope,
            pageSize = pageSize,
        )

    override fun coinDetailStream(coinId: String, scope: CoroutineScope): ScreenDataStream<CoinDetail> =
        coinDetailStore.asScreenStream(
            key = coinId,
            cacheKey = "crypto:coinDetail:$coinId",
            scope = scope,
        )
}
