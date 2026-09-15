/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.crypto.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.paging.PageKey
import kpt.core.base.store.paging.PagingScreenStream
import kpt.core.base.store.paging.asPagingScreenStream
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.crypto.CryptoRepository
import kpt.core.model.crypto.CoinDetail
import kpt.core.model.crypto.CoinMarket
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = CryptoRepository::class)
class CryptoRepositoryImpl(
    @FromStore(AppStoreIds.CoinMarkets) private val coinMarketsStore: Store<PageKey, List<CoinMarket>>,
    @FromStore(AppStoreIds.CoinDetail) private val coinDetailStore: Store<String, CoinDetail>,
) : CryptoRepository {

    override fun coinMarketsStream(scope: CoroutineScope, pageSize: Int): PagingScreenStream<CoinMarket> =
        coinMarketsStore.asPagingScreenStream(
            cacheKey = AppCacheKeys.CoinMarkets.LIST,
            scope = scope,
            pageSize = pageSize,
        )

    override fun coinDetailStream(coinId: String, scope: CoroutineScope): ScreenDataStream<CoinDetail> =
        coinDetailStore.asScreenStream(
            key = coinId,
            cacheKey = AppCacheKeys.CoinDetail.item(coinId),
            scope = scope,
        )
}
