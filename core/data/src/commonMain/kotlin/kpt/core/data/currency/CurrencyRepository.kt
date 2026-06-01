/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.currency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.currency.ExchangeRates
import kpt.core.model.currency.RateHistory
import kpt.core.model.currency.RateHistoryKey

interface CurrencyRepository {
    /**
     * Stream of exchange rates for [baseCurrency].
     *
     * @param fetchPolicy Controls network vs. cache strategy. Defaults to
     *   [FetchPolicy.NETWORK_WITH_CACHE] (cached data shown immediately, refreshed
     *   in background). Pass [FetchPolicy.PERIODIC] for ambient surfaces like the
     *   home dashboard tile that should auto-refresh on a cadence.
     */
    fun exchangeRatesStream(
        baseCurrency: String,
        scope: CoroutineScope,
        fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
    ): ScreenDataStream<ExchangeRates>

    fun rateHistoryStream(keyFlow: Flow<RateHistoryKey>, scope: CoroutineScope): ScreenDataStream<RateHistory>
}
