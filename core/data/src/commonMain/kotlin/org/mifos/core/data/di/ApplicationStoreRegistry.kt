/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.di

import template.core.base.store.StoreRegistry
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object ApplicationStoreRegistry : StoreRegistry() {
    val ExchangeRates = store("exchangeRates")
    val RateHistory = store("rateHistory")
    val CoinMarkets = store("coinMarkets")
    val CoinDetail = store("coinDetail")

    /** TTL durations — financial data has different freshness requirements. */
    object Ttl {
        val EXCHANGE_RATES = 5.minutes
        val RATE_HISTORY = 1.hours
        val COIN_MARKETS = 2.minutes
        val COIN_DETAIL = 5.minutes
    }
}
