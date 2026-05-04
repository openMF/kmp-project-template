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
import org.mifos.core.database.dao.ExchangeRatesDao
import org.mifos.core.database.mapper.toDomain
import org.mifos.core.database.mapper.toEntity
import org.mifos.core.model.fintech.ExchangeRates
import org.mifos.core.network.fintech.FrankfurterApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import template.core.base.store.DefaultValidator
import template.core.base.store.StoreFactory

fun provideExchangeRatesStore(
    api: FrankfurterApi,
    networkMonitor: NetworkMonitor,
    dao: ExchangeRatesDao,
): Store<String, ExchangeRates> {
    val validator = DefaultValidator.withTtl<ExchangeRates>(ApplicationStoreRegistry.Ttl.EXCHANGE_RATES)
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
