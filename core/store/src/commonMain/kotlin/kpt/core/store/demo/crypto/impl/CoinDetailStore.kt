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
import kpt.core.database.demo.crypto.dao.CoinDetailDao
import kpt.core.database.demo.crypto.mapper.toDomain
import kpt.core.database.demo.crypto.mapper.toEntity
import kpt.core.model.demo.crypto.CoinDetail
import kpt.core.network.demo.crypto.api.CoinGeckoApi
import kpt.core.store.AppStoreRegistry
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

fun provideCoinDetailStore(
    api: CoinGeckoApi,
    networkMonitor: NetworkMonitor,
    dao: CoinDetailDao,
): Store<String, CoinDetail> {
    val validator = DefaultValidator.withTtl<CoinDetail>(AppStoreRegistry.Ttl.COIN_DETAIL)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { coinId: String ->
            networkMonitor.executeWithRetry(
                RetryPolicy { maxAttempts = 1 },
            ) {
                api.getCoinDetail(coinId).toDomain()
            }
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { coinId -> dao.getById(coinId).map { it?.toDomain() } },
            writer = { _, detail ->
                dao.upsert(detail.toEntity())
                validator.markFresh()
            },
            delete = { coinId -> dao.delete(coinId) },
            deleteAll = { dao.deleteAll() },
        ),
        validator = validator,
    )
}
