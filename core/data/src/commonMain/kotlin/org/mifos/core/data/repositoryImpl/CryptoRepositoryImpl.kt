/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.repositoryImpl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import org.mifos.core.data.repository.CryptoRepository
import org.mifos.core.model.fintech.CoinDetail
import org.mifos.core.model.fintech.CoinMarket
import org.mobilenativefoundation.store.store5.Store
import template.core.base.store.infra.FetchedAtRepository
import template.core.base.store.paging.PageKey
import template.core.base.store.paging.PagingScreenStream
import template.core.base.store.paging.asPagingScreenStream
import template.core.base.store.screen.ScreenDataStream
import template.core.base.store.screen.asScreenStream

class CryptoRepositoryImpl(
    private val coinMarketsStore: Store<PageKey, List<CoinMarket>>,
    private val coinDetailStore: Store<String, CoinDetail>,
    private val networkMonitor: NetworkMonitor,
    private val fetchedAtRepository: FetchedAtRepository,
) : CryptoRepository {

    override fun coinMarketsStream(
        scope: CoroutineScope,
        pageSize: Int,
    ): PagingScreenStream<CoinMarket> = coinMarketsStore.asPagingScreenStream(
        networkMonitor = networkMonitor,
        fetchedAtRepository = fetchedAtRepository,
        cacheKey = "crypto:coinMarkets",
        scope = scope,
        pageSize = pageSize,
    )

    override fun coinDetailStream(
        coinId: String,
        scope: CoroutineScope,
    ): ScreenDataStream<CoinDetail> = coinDetailStore.asScreenStream(
        key = coinId,
        networkMonitor = networkMonitor,
        fetchedAtRepository = fetchedAtRepository,
        cacheKey = "crypto:coinDetail:$coinId",
        scope = scope,
    )
}
