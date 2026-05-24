/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.store.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.mifos.core.store.AppStoreRegistry
import org.mifos.core.store.crypto.impl.provideCoinDetailStore
import org.mifos.core.store.crypto.impl.provideCoinMarketsStore
import org.mifos.core.store.currency.impl.provideExchangeRatesStore
import org.mifos.core.store.currency.impl.provideRateHistoryStore
import org.mifos.core.store.economic.impl.provideInterestRateSeriesStore
import org.mifos.core.store.economic.impl.provideMacroIndicatorStore
import org.mifos.core.store.infra.StoreCacheManager
import org.mifos.core.store.infra.impl.StoreCacheManagerImpl

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
        provideInterestRateSeriesStore(get(), get(), get())
    }
    single(AppStoreRegistry.MacroIndicator) {
        provideMacroIndicatorStore(get(), get())
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
    }
}
