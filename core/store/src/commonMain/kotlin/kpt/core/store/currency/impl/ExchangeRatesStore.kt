/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.currency.impl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.RetryPolicy
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.executeWithRetry
import kotlinx.coroutines.flow.map
import kpt.core.database.currency.dao.ExchangeRatesDao
import kpt.core.database.currency.mapper.toDomain
import kpt.core.database.currency.mapper.toEntity
import kpt.core.model.currency.ExchangeRates
import kpt.core.network.currency.api.FrankfurterApi
import kpt.core.store.AppStoreRegistry
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kpt.core.base.store.infra.DefaultValidator
import kpt.core.base.store.infra.StoreFactory

fun provideExchangeRatesStore(
    api: FrankfurterApi,
    networkMonitor: NetworkMonitor,
    dao: ExchangeRatesDao,
): Store<String, ExchangeRates> {
    val validator = DefaultValidator.withTtl<ExchangeRates>(AppStoreRegistry.Ttl.EXCHANGE_RATES)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { baseCurrency: String ->
            networkMonitor.executeWithRetry(
                RetryPolicy { maxAttempts = 1 },
            ) {
                api.getLatestRates(from = baseCurrency).toDomain()
            }
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { baseCurrency -> dao.getByBase(baseCurrency).map { it?.toDomain() } },
            writer = { baseCurrency, rates ->
                dao.upsert(rates.toEntity(baseCurrency))
                validator.markFresh()
            },
            delete = { baseCurrency -> dao.deleteByBase(baseCurrency) },
            deleteAll = { dao.deleteAll() },
        ),
        validator = validator,
    )
}
