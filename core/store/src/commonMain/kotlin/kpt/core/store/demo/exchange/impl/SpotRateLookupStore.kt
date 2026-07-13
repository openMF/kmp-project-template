/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.exchange.impl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.RetryPolicy
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.executeWithRetry
import kotlinx.coroutines.flow.map
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.demo.currency.dao.ExchangeRatesDao
import kpt.core.database.demo.currency.mapper.toDomain
import kpt.core.database.demo.currency.mapper.toEntity
import kpt.core.model.demo.currency.ExchangeRates
import kpt.core.network.demo.currency.api.FrankfurterApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Build a network-backed [Store] for spot (current) exchange-rate lookups.
 *
 * This store is architecturally identical to the main exchange-rates store but
 * is registered under [kpt.core.store.AppStoreRegistry.SpotRate] so that
 * consumers can apply a [kpt.core.base.store.screen.FetchPolicy.NETWORK_ONLY]
 * at the stream callsite — forcing a fresh fetch for point-in-time rate queries
 * (e.g., transaction conversion preview) rather than serving cached data.
 *
 * Key: [String] — the base currency code (e.g., "USD", "EUR").
 * Value: [ExchangeRates] — domain model with all target rates for that base.
 */
fun provideSpotRateLookupStore(
    api: FrankfurterApi,
    networkMonitor: NetworkMonitor,
    dao: ExchangeRatesDao,
): Store<String, ExchangeRates> = StoreFactory.createStore(
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
        },
        delete = { baseCurrency -> dao.deleteByBase(baseCurrency) },
        deleteAll = { dao.deleteAll() },
    ),
)
