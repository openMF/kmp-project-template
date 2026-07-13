/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.crypto.impl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.RetryPolicy
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.executeWithRetry
import kotlinx.coroutines.flow.map
import kpt.core.base.store.infra.DefaultValidator
import kpt.core.base.store.infra.StoreFactory
import kpt.core.base.store.paging.PageKey
import kpt.core.database.demo.crypto.dao.CoinMarketDao
import kpt.core.database.demo.crypto.mapper.toDomain
import kpt.core.database.demo.crypto.mapper.toEntity
import kpt.core.model.demo.crypto.CoinMarket
import kpt.core.network.demo.crypto.api.CoinGeckoApi
import kpt.core.store.AppStoreRegistry
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

fun provideCoinMarketsStore(
    api: CoinGeckoApi,
    networkMonitor: NetworkMonitor,
    dao: CoinMarketDao,
): Store<PageKey, List<CoinMarket>> {
    val validator = DefaultValidator.withTtl<List<CoinMarket>>(AppStoreRegistry.Ttl.COIN_MARKETS)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { key: PageKey ->
            networkMonitor.executeWithRetry(
                // Retry policy: 1 attempt per fetch. HTTP 401 is handled transparently
                // by the Ktor Auth interceptor (refresh-and-retry once). All other
                // failures propagate immediately to PagingScreenStream → DecisionEngine.
                RetryPolicy { maxAttempts = 1 },
            ) {
                api.getMarkets(page = key.page + 1, perPage = key.pageSize)
                    .map { it.toDomain() }
            }
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key ->
                dao.getPage(limit = key.pageSize, offset = key.page * key.pageSize)
                    .map { entities -> entities.map { it.toDomain() }.ifEmpty { null } }
            },
            writer = { key, markets ->
                dao.deleteByPage(key.page)
                dao.upsertAll(markets.map { it.toEntity(key.page) })
                validator.markFresh()
            },
            delete = { key -> dao.deleteByPage(key.page) },
            deleteAll = { dao.deleteAll() },
        ),
        validator = validator,
    )
}
