/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.di

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitorProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.mifos.core.data.crypto.CryptoRepository
import org.mifos.core.data.crypto.impl.CryptoRepositoryImpl
import org.mifos.core.data.crypto.impl.provideCoinDetailStore
import org.mifos.core.data.crypto.impl.provideCoinMarketsStore
import org.mifos.core.data.currency.CurrencyRepository
import org.mifos.core.data.currency.impl.CurrencyRepositoryImpl
import org.mifos.core.data.currency.impl.provideExchangeRatesStore
import org.mifos.core.data.currency.impl.provideRateHistoryStore
import org.mifos.core.data.infra.NetworkMonitor
import org.mifos.core.data.infra.StoreCacheManager
import org.mifos.core.data.infra.impl.RoomFetchedAtRepository
import org.mifos.core.data.infra.impl.StoreCacheManagerImpl
import org.mifos.core.data.user.UserDataRepository
import org.mifos.core.data.user.UserLogoutManager
import org.mifos.core.data.user.impl.UserDataRepositoryImpl
import org.mifos.core.data.user.impl.UserLogoutManagerImpl
import org.mifos.core.database.AppDatabase
import org.mifos.core.database.di.DatabaseModule
import org.mifos.core.datastore.di.DatastoreModule
import org.mifos.core.network.di.NetworkModule
import template.core.base.common.di.CommonModule
import template.core.base.store.infra.FetchedAtRepository

val DataModule = module {
    includes(platformModule, CommonModule, DatabaseModule, DatastoreModule, NetworkModule)

    single<NetworkMonitor> { NetworkMonitorProvider.install() }
    singleOf(::UserDataRepositoryImpl) bind UserDataRepository::class

    // Framework FetchedAtRepository — durable lastFetchedAt persistence backing
    // DataFreshnessIndicator timestamps. Room-only by design (no in-memory fallback).
    single<FetchedAtRepository> { RoomFetchedAtRepository(get<AppDatabase>().fetchedAtDao) }

    // Framework DraftDao — backing store for SubmitOutbox / DraftSubmitHandler
    single { get<AppDatabase>().draftDao }

    // Store cache manager — clears all registered caches on logout (registration-based)
    single<StoreCacheManager> {
        StoreCacheManagerImpl(
            bookkeeperDao = get(),
            draftDao = get(),
        )
    }

    single<UserLogoutManager> { UserLogoutManagerImpl(get(), get(), get()) }

    // Fintech Stores (internal — exposed only through repositories)
    single(ApplicationStoreRegistry.ExchangeRates) { provideExchangeRatesStore(get(), get(), get()) }
    single(ApplicationStoreRegistry.RateHistory) { provideRateHistoryStore(get(), get(), get()) }
    single(ApplicationStoreRegistry.CoinMarkets) { provideCoinMarketsStore(get(), get(), get()) }
    single(ApplicationStoreRegistry.CoinDetail) { provideCoinDetailStore(get(), get(), get()) }

    // Register fintech feature stores for logout cache clearing
    single(createdAtStart = true) {
        val mgr = get<StoreCacheManager>() as StoreCacheManagerImpl
        mgr.register(get(ApplicationStoreRegistry.ExchangeRates))
        mgr.register(get(ApplicationStoreRegistry.RateHistory))
        mgr.register(get(ApplicationStoreRegistry.CoinMarkets))
        mgr.register(get(ApplicationStoreRegistry.CoinDetail))
    }

    // Fintech Repositories
    single<CurrencyRepository> {
        CurrencyRepositoryImpl(
            exchangeRatesStore = get(ApplicationStoreRegistry.ExchangeRates),
            rateHistoryStore = get(ApplicationStoreRegistry.RateHistory),
            networkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }
    single<CryptoRepository> {
        CryptoRepositoryImpl(
            coinMarketsStore = get(ApplicationStoreRegistry.CoinMarkets),
            coinDetailStore = get(ApplicationStoreRegistry.CoinDetail),
            networkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }
}

expect val platformModule: Module
