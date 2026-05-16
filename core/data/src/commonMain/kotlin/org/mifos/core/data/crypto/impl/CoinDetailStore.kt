/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.crypto.impl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.RetryPolicy
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.executeWithRetry
import kotlinx.coroutines.flow.map
import org.mifos.core.data.di.ApplicationStoreRegistry
import org.mifos.core.database.crypto.dao.CoinDetailDao
import org.mifos.core.database.crypto.mapper.toDomain
import org.mifos.core.database.crypto.mapper.toEntity
import org.mifos.core.model.crypto.CoinDetail
import org.mifos.core.network.crypto.api.CoinGeckoApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import template.core.base.store.infra.DefaultValidator
import template.core.base.store.infra.StoreFactory

fun provideCoinDetailStore(
    api: CoinGeckoApi,
    networkMonitor: NetworkMonitor,
    dao: CoinDetailDao,
): Store<String, CoinDetail> {
    val validator = DefaultValidator.withTtl<CoinDetail>(ApplicationStoreRegistry.Ttl.COIN_DETAIL)
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
