/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.currency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.base.data.infra.Syncable
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.demo.currency.ExchangeRates
import kpt.core.model.demo.currency.RateHistory
import kpt.core.model.demo.currency.RateHistoryKey

/**
 * Repository surface for exchange rates + historical rate data.
 *
 * Implements [Syncable] — the
 * `sync/` module's `DataSyncWorker` calls [syncWith] in parallel with the
 * macro-indicators repo to force-refresh both Store5 caches on a schedule
 * or from a pull-to-refresh gesture.
 */
interface CurrencyRepository : Syncable {
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

    /**
     * Spot conversion-rate stream with a connectivity-driven [FetchPolicy]: [online] `true` →
     * [FetchPolicy.NETWORK_ONLY] (always fresh), `false` → [FetchPolicy.CACHE_ONLY] (no error
     * flicker offline). The caller re-requests on each connectivity flip (`flatMapLatest`).
     */
    fun spotRateStream(baseCurrency: String, online: Boolean, scope: CoroutineScope): ScreenDataStream<ExchangeRates>

    // Syncable.syncWith(synchronizer) is implemented in CurrencyRepositoryImpl —
    // force-refreshes the exchangeRatesStore for each pinned base currency via
    // Store5's native `fresh(key)` (which bypasses cache + writes through
    // SourceOfTruth automatically — the forced-refresh seam).
}
