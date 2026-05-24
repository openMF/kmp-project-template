/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.store

import template.core.base.store.infra.StoreRegistry
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Application-level [StoreRegistry] — the single named-qualifier registry for every
 * `org.mobilenativefoundation.store.store5.Store` the app exposes.
 *
 * The 4 demo stores ([ExchangeRates], [RateHistory], [CoinMarkets], [CoinDetail]) live
 * here as the canonical example shape for forks. Add your own next to them, e.g.:
 *
 * ```kotlin
 * object AppStoreRegistry : StoreRegistry() {
 *     // Demo stores (kept as forkable examples)
 *     val ExchangeRates = store("exchangeRates")
 *     // …
 *
 *     // Your app's stores
 *     val UserProfile  = store("userProfile")
 *     val Transactions = store("transactions")
 * }
 * ```
 *
 * Then reference the qualifier from Koin DI in [appStoreModule]:
 *
 * ```kotlin
 * single<Store<UserId, UserProfile>>(qualifier = AppStoreRegistry.UserProfile) { ... }
 * ```
 *
 * Centralizing here gives a one-place audit of every Store the app owns and prevents
 * qualifier-name collisions across feature modules.
 */
object AppStoreRegistry : StoreRegistry() {
    val ExchangeRates = store("exchangeRates")
    val RateHistory = store("rateHistory")
    val CoinMarkets = store("coinMarkets")
    val CoinDetail = store("coinDetail")

    // Banking Utility Toolkit — economic data
    val InterestRateSeries = store("interestRateSeries")
    val MacroIndicator = store("macroIndicator")

    /** TTL durations — financial data has different freshness requirements. */
    object Ttl {
        val EXCHANGE_RATES = 5.minutes
        val RATE_HISTORY = 1.hours
        val COIN_MARKETS = 2.minutes
        val COIN_DETAIL = 5.minutes

        // Banking Utility Toolkit
        /** FRED publishes daily; 24h freshness window matches their cadence. */
        val INTEREST_RATE_SERIES = 24.hours

        /** World Bank publishes annually; 7 days is conservatively fresh. */
        val MACRO_INDICATOR = 7.days
    }
}
