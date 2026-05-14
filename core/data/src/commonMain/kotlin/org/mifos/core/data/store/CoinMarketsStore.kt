/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.store

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.RetryPolicy
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.executeWithRetry
import kotlinx.coroutines.flow.map
import org.mifos.core.data.di.ApplicationStoreRegistry
import org.mifos.core.database.dao.CoinMarketDao
import org.mifos.core.database.mapper.toDomain
import org.mifos.core.database.mapper.toEntity
import org.mifos.core.model.fintech.CoinMarket
import org.mifos.core.network.fintech.CoinGeckoApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import template.core.base.store.infra.DefaultValidator
import template.core.base.store.infra.StoreFactory
import template.core.base.store.paging.PageKey

fun provideCoinMarketsStore(
    api: CoinGeckoApi,
    networkMonitor: NetworkMonitor,
    dao: CoinMarketDao,
): Store<PageKey, List<CoinMarket>> {
    val validator = DefaultValidator.withTtl<List<CoinMarket>>(ApplicationStoreRegistry.Ttl.COIN_MARKETS)
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
