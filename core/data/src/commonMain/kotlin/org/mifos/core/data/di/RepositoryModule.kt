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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.mifos.core.data.alerts.AlertsRepository
import org.mifos.core.data.alerts.impl.AlertsRepositoryImpl
import org.mifos.core.data.crypto.CryptoRepository
import org.mifos.core.data.crypto.impl.CryptoRepositoryImpl
import org.mifos.core.data.currency.CurrencyRepository
import org.mifos.core.data.currency.impl.CurrencyRepositoryImpl
import org.mifos.core.data.infra.NetworkMonitor
import org.mifos.core.data.infra.impl.RoomFetchedAtRepository
import org.mifos.core.data.infra.impl.RoomSubmitOutbox
import org.mifos.core.data.user.UserDataRepository
import org.mifos.core.data.user.UserLogoutManager
import org.mifos.core.data.user.impl.UserDataRepositoryImpl
import org.mifos.core.data.user.impl.UserLogoutManagerImpl
import org.mifos.core.data.watchlist.WatchlistRepository
import org.mifos.core.data.watchlist.impl.WatchlistRepositoryImpl
import org.mifos.core.database.AppDatabase
import org.mifos.core.database.di.DatabaseModule
import org.mifos.core.datastore.di.DatastoreModule
import org.mifos.core.model.alerts.PriceAlert
import org.mifos.core.network.alerts.api.AlertsApi
import org.mifos.core.network.alerts.api.FakeAlertsApi
import org.mifos.core.network.di.NetworkModule
import org.mifos.core.store.AppStoreRegistry
import template.core.base.common.di.CommonModule
import template.core.base.store.infra.FetchedAtRepository
import template.core.base.store.submit.OfflineSubmitSyncer
import template.core.base.store.submit.SubmitOutbox

val DataModule = module {
    includes(platformModule, CommonModule, DatabaseModule, DatastoreModule, NetworkModule)

    single<NetworkMonitor> { NetworkMonitorProvider.install() }
    singleOf(::UserDataRepositoryImpl) bind UserDataRepository::class

    // Framework FetchedAtRepository — durable lastFetchedAt persistence backing
    // DataFreshnessIndicator timestamps. Room-only by design (no in-memory fallback).
    single<FetchedAtRepository> { RoomFetchedAtRepository(get<AppDatabase>().fetchedAtDao) }

    // Framework DraftDao — backing store for SubmitOutbox / DraftSubmitHandler
    single { get<AppDatabase>().draftDao }

    // Personal watchlist — local-only persistence for the SubmitHandler showcase.
    single { get<AppDatabase>().watchlistDao }
    single<WatchlistRepository> { WatchlistRepositoryImpl(get()) }

    single<UserLogoutManager> { UserLogoutManagerImpl(get(), get(), get()) }

    // Fintech Repositories
    single<CurrencyRepository> {
        CurrencyRepositoryImpl(
            exchangeRatesStore = get(AppStoreRegistry.ExchangeRates),
            rateHistoryStore = get(AppStoreRegistry.RateHistory),
            networkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }
    single<CryptoRepository> {
        CryptoRepositoryImpl(
            coinMarketsStore = get(AppStoreRegistry.CoinMarkets),
            coinDetailStore = get(AppStoreRegistry.CoinDetail),
            networkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }

    // Price alerts — fake-API-backed, with DraftSubmitHandler offline-resilience showcase.
    // Real forks substitute FakeAlertsApi with a Ktorfit-backed AlertsApi client.
    single<AlertsApi> { FakeAlertsApi() }
    single<AlertsRepository> { AlertsRepositoryImpl(api = get()) }

    // Outbox for PriceAlert payloads — RoomSubmitOutbox writes to framework_submit_drafts.
    single<SubmitOutbox<PriceAlert>> {
        RoomSubmitOutbox(dao = get(), serializer = PriceAlert.serializer())
    }

    // App-scoped CoroutineScope for cross-VM long-running coroutines (e.g., OfflineSubmitSyncer).
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    // Eager singleton — starts watching network online events at Koin start; retries
    // any pending alerts when connectivity returns.
    single(createdAtStart = true) {
        val syncer = OfflineSubmitSyncer<PriceAlert, PriceAlert>(
            scope = get(),
            outbox = get(),
            isOnlineFlow = get<NetworkMonitor>().isOnline,
            submitBlock = { payload -> get<AlertsApi>().create(payload) },
        )
        syncer.start()
        syncer
    }
}

expect val platformModule: Module
