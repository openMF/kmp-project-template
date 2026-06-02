/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.di

import kpt.core.store.AppStoreRegistry
import kpt.core.store.alerts.impl.provideAlertsStore
import kpt.core.store.banking.impl.provideBillRemindersStore
import kpt.core.store.banking.impl.provideLoansStore
import kpt.core.store.crypto.impl.provideCoinDetailStore
import kpt.core.store.crypto.impl.provideCoinMarketsStore
import kpt.core.store.currency.impl.provideExchangeRatesStore
import kpt.core.store.currency.impl.provideRateHistoryStore
import kpt.core.store.economic.impl.provideInterestRateSeriesStore
import kpt.core.store.economic.impl.provideMacroIndicatorStore
import kpt.core.store.exchange.impl.provideSpotRateLookupStore
import kpt.core.store.infra.StoreCacheManager
import kpt.core.store.infra.impl.StoreCacheManagerImpl
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for app-level Store wiring.
 *
 * Forks register their `Store` instances here, qualifier-bound via [AppStoreRegistry].
 * The 4 demo stores ship as forkable examples — add your own `single(qualifier = ...)`
 * blocks next to them.
 *
 * Wire into the Koin start-up:
 * ```kotlin
 * startKoin {
 *     modules(appStoreModule, /* ...other modules */)
 * }
 * ```
 */
val appStoreModule: Module = module {
    // Store cache manager — clears all registered caches on logout (registration-based)
    single<StoreCacheManager> {
        StoreCacheManagerImpl(
            bookkeeperDao = get(),
            draftDao = get(),
        )
    }

    // Fintech Stores (internal — exposed only through repositories)
    single(AppStoreRegistry.ExchangeRates) { provideExchangeRatesStore(get(), get(), get()) }
    single(AppStoreRegistry.RateHistory) { provideRateHistoryStore(get(), get(), get()) }
    single(AppStoreRegistry.CoinMarkets) { provideCoinMarketsStore(get(), get(), get()) }
    single(AppStoreRegistry.CoinDetail) { provideCoinDetailStore(get(), get(), get()) }

    // Economic Stores (Banking Utility Toolkit — FRED + World Bank)
    single(AppStoreRegistry.InterestRateSeries) {
        // Updated: now persists to InterestRateSeriesDao (NETWORK_WITH_CACHE archetype).
        provideInterestRateSeriesStore(get(), get(), get(), get())
    }
    single(AppStoreRegistry.MacroIndicator) {
        provideMacroIndicatorStore(get(), get())
    }

    // Banking Utility Toolkit — offline-local stores (OFFLINE_LOCAL_ONLY archetype)
    single(AppStoreRegistry.Alerts) {
        provideAlertsStore(dao = get())
    }
    single(AppStoreRegistry.Loans) {
        provideLoansStore(dao = get())
    }
    single(AppStoreRegistry.BillReminders) {
        provideBillRemindersStore(dao = get())
    }

    // Banking Utility Toolkit — spot exchange-rate lookup (NETWORK_ONLY callsite archetype)
    single(AppStoreRegistry.SpotRate) {
        provideSpotRateLookupStore(api = get(), networkMonitor = get(), dao = get())
    }

    // Register fintech feature stores for logout cache clearing
    single(createdAtStart = true) {
        val mgr = get<StoreCacheManager>() as StoreCacheManagerImpl
        mgr.register(get(AppStoreRegistry.ExchangeRates))
        mgr.register(get(AppStoreRegistry.RateHistory))
        mgr.register(get(AppStoreRegistry.CoinMarkets))
        mgr.register(get(AppStoreRegistry.CoinDetail))
        mgr.register(get(AppStoreRegistry.InterestRateSeries))
        mgr.register(get(AppStoreRegistry.MacroIndicator))
        mgr.register(get(AppStoreRegistry.Alerts))
        mgr.register(get(AppStoreRegistry.Loans))
        mgr.register(get(AppStoreRegistry.BillReminders))
        mgr.register(get(AppStoreRegistry.SpotRate))
    }
}
