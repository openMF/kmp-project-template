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
import org.mifos.core.data.repository.CryptoRepository
import org.mifos.core.data.repository.CurrencyRepository
import org.mifos.core.data.repository.NetworkMonitor
import org.mifos.core.data.repository.StoreCacheManager
import org.mifos.core.data.repository.UserDataRepository
import org.mifos.core.data.repository.UserLogoutManager
import org.mifos.core.data.repositoryImpl.CryptoRepositoryImpl
import org.mifos.core.data.repositoryImpl.CurrencyRepositoryImpl
import org.mifos.core.data.repositoryImpl.RoomFetchedAtRepository
import org.mifos.core.data.repositoryImpl.StoreCacheManagerImpl
import org.mifos.core.data.repositoryImpl.UserDataRepositoryImpl
import org.mifos.core.data.repositoryImpl.UserLogoutManagerImpl
import org.mifos.core.data.store.provideCoinDetailStore
import org.mifos.core.data.store.provideCoinMarketsStore
import org.mifos.core.data.store.provideExchangeRatesStore
import org.mifos.core.data.store.provideRateHistoryStore
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

    // Store cache manager — clears all caches on logout
    single<StoreCacheManager> {
        StoreCacheManagerImpl(
            exchangeRatesStore = get(ApplicationStoreRegistry.ExchangeRates),
            rateHistoryStore = get(ApplicationStoreRegistry.RateHistory),
            coinMarketsStore = get(ApplicationStoreRegistry.CoinMarkets),
            coinDetailStore = get(ApplicationStoreRegistry.CoinDetail),
            bookkeeperDao = get(),
        )
    }

    single<UserLogoutManager> { UserLogoutManagerImpl(get(), get(), get()) }

    // Fintech Stores (internal — exposed only through repositories)
    single(ApplicationStoreRegistry.ExchangeRates) { provideExchangeRatesStore(get(), get(), get()) }
    single(ApplicationStoreRegistry.RateHistory) { provideRateHistoryStore(get(), get(), get()) }
    single(ApplicationStoreRegistry.CoinMarkets) { provideCoinMarketsStore(get(), get(), get()) }
    single(ApplicationStoreRegistry.CoinDetail) { provideCoinDetailStore(get(), get(), get()) }

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
