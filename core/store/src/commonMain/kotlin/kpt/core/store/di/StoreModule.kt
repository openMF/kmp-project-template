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

import kpt.core.base.store.infra.DraftInventory
import kpt.core.base.store.infra.StoreCacheManager
import kpt.core.base.store.infra.impl.DraftInventoryImpl
import kpt.core.base.store.infra.impl.StoreCacheManagerImpl
import kpt.core.store.AppStoreRegistry
import kpt.core.store.demo.alerts.impl.provideAlertsStore
import kpt.core.store.demo.alerts.impl.provideAlertsWriteStore
import kpt.core.store.demo.banking.impl.provideBillRemindersStore
import kpt.core.store.demo.banking.impl.provideBillRemindersWriteStore
import kpt.core.store.demo.banking.impl.provideLoansStore
import kpt.core.store.demo.banking.impl.provideLoansWriteStore
import kpt.core.store.demo.cloudtodo.impl.provideCloudTodoReadStore
import kpt.core.store.demo.cloudtodo.impl.provideCloudTodoStore
import kpt.core.store.demo.crypto.impl.provideCoinDetailStore
import kpt.core.store.demo.crypto.impl.provideCoinMarketsStore
import kpt.core.store.demo.currency.impl.provideExchangeRatesStore
import kpt.core.store.demo.currency.impl.provideRateHistoryStore
import kpt.core.store.demo.economic.impl.provideInterestRateSeriesStore
import kpt.core.store.demo.economic.impl.provideMacroIndicatorStore
import kpt.core.store.demo.exchange.impl.provideSpotRateLookupStore
import kpt.core.store.demo.watchlist.impl.provideWatchlistStore
import kpt.core.store.demo.watchlist.impl.provideWatchlistWriteStore
import org.koin.core.module.Module
import org.koin.dsl.module
import kpt.core.base.store.di.StoreModule as CoreBaseStoreModule

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
    // Framework write-SoT: the base-store module provides the single write door (MutationGateway)
    // + its Room-backed ConflictInbox. Every repo migrated onto `gateway.*` resolves `get()` here,
    // so a fork wiring `appStoreModule` gets the gateway for free (needs ConflictDao from
    // DatabaseModule + NetworkMonitor on the graph — both present in KoinModules.allModules).
    includes(CoreBaseStoreModule)

    // Store cache manager — clears all registered caches on logout (registration-based)
    single<StoreCacheManager> {
        StoreCacheManagerImpl(
            bookkeeperDao = get(),
            draftDao = get(),
        )
    }

    // Cross-form drafts inventory — the live feed + actions behind the template-level
    // Settings → "Sync & Drafts" screen. Framework infra (not a demo store); survives sync.
    single<DraftInventory> { DraftInventoryImpl(draftDao = get()) }

    // demo:begin — customizer --clean strips all demo stores + their logout registration
    // Fintech Stores (internal — exposed only through repositories)
    single(AppStoreRegistry.ExchangeRates) { provideExchangeRatesStore(get(), get(), get()) }
    single(AppStoreRegistry.RateHistory) { provideRateHistoryStore(get(), get(), get()) }
    single(AppStoreRegistry.CoinMarkets) { provideCoinMarketsStore(get(), get(), get()) }
    single(AppStoreRegistry.CloudTodo) { provideCloudTodoReadStore(get(), get()) }
    single(AppStoreRegistry.CloudTodoMutable) { provideCloudTodoStore(api = get(), dao = get(), bookkeeper = get()) }
    single(AppStoreRegistry.CoinDetail) { provideCoinDetailStore(get(), get(), get()) }

    // Economic Stores (Banking Utility Toolkit — FRED + World Bank)
    single(AppStoreRegistry.InterestRateSeries) {
        // Updated: now persists to InterestRateSeriesDao (NETWORK_WITH_CACHE archetype).
        provideInterestRateSeriesStore(get(), get(), get(), get())
    }
    single(AppStoreRegistry.MacroIndicator) {
        provideMacroIndicatorStore(get(), get(), get())
    }

    // Banking Utility Toolkit — offline-local stores (OFFLINE_LOCAL_ONLY archetype)
    single(AppStoreRegistry.Alerts) {
        provideAlertsStore(dao = get())
    }
    single(AppStoreRegistry.AlertsMutable) {
        provideAlertsWriteStore(dao = get())
    }
    single(AppStoreRegistry.Watchlist) {
        provideWatchlistStore(dao = get())
    }
    single(AppStoreRegistry.WatchlistMutable) {
        provideWatchlistWriteStore(dao = get())
    }
    single(AppStoreRegistry.Loans) {
        provideLoansStore(dao = get())
    }
    single(AppStoreRegistry.LoansMutable) {
        provideLoansWriteStore(dao = get())
    }
    single(AppStoreRegistry.BillReminders) {
        provideBillRemindersStore(dao = get())
    }
    single(AppStoreRegistry.BillRemindersMutable) {
        provideBillRemindersWriteStore(dao = get())
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
        mgr.register(get(AppStoreRegistry.CloudTodo))
        mgr.register(get(AppStoreRegistry.CoinDetail))
        mgr.register(get(AppStoreRegistry.InterestRateSeries))
        mgr.register(get(AppStoreRegistry.MacroIndicator))
        mgr.register(get(AppStoreRegistry.Alerts))
        mgr.register(get(AppStoreRegistry.Watchlist))
        mgr.register(get(AppStoreRegistry.Loans))
        mgr.register(get(AppStoreRegistry.BillReminders))
        mgr.register(get(AppStoreRegistry.SpotRate))
    }
    // demo:end
}
