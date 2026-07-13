/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.currency.impl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.base.store.screen.requireData
import kpt.core.data.demo.currency.CurrencyRepository
import kpt.core.data.infra.Synchronizer
import kpt.core.data.infra.snapshotSync
import kpt.core.model.demo.currency.ExchangeRates
import kpt.core.model.demo.currency.RateHistory
import kpt.core.model.demo.currency.RateHistoryKey
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse

/**
 * Pinned base currencies that the [syncWith] forced-refresh fans out across.
 * At v1 these are hardcoded; payload-driven extensibility (per-key opt-in
 * from the worker enqueue side) is a follow-up.
 */
private val PINNED_BASE_CURRENCIES = listOf("USD", "EUR", "INR")

class CurrencyRepositoryImpl(
    private val exchangeRatesStore: Store<String, ExchangeRates>,
    private val rateHistoryStore: Store<RateHistoryKey, RateHistory>,
    private val networkMonitor: NetworkMonitor,
    private val fetchedAtRepository: FetchedAtRepository,
) : CurrencyRepository {

    override fun exchangeRatesStream(
        baseCurrency: String,
        scope: CoroutineScope,
        fetchPolicy: FetchPolicy,
    ): ScreenDataStream<ExchangeRates> = exchangeRatesStore.asScreenStream(
        key = baseCurrency,
        networkMonitor = networkMonitor,
        fetchedAtRepository = fetchedAtRepository,
        cacheKey = "currency:exchangeRates:$baseCurrency",
        scope = scope,
        fetchPolicy = fetchPolicy,
    )

    override fun rateHistoryStream(
        keyFlow: Flow<RateHistoryKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<RateHistory> = rateHistoryStore.asScreenStream(
        keyFlow = keyFlow,
        networkMonitor = networkMonitor,
        fetchedAtRepository = fetchedAtRepository,
        cacheKeyFor = { key -> "currency:rateHistory:${key.from}-${key.to}-${key.days}d" },
        scope = scope,
    )

    /**
     * Forced-refresh seam called by `sync/` module's `DataSyncWorker`.
     *
     * For each pinned base currency (in parallel) issues Store5's one-shot
     * `stream(StoreReadRequest.fresh(key))` — the forced-refresh seam. `fresh`
     * bypasses the cache, hits Frankfurter via the fetcher, and writes through
     * SourceOfTruth automatically. A cold `stream(fresh)` completes on the first
     * terminal response, so the worker returns promptly — unlike `asScreenStream`,
     * which launches perpetual auto-refresh / network-monitor coroutines into its
     * `scope` (those are for long-lived UI subscriptions, not one-shot sync).
     *
     * On any per-base failure the surrounding [snapshotSync] propagates the exception,
     * which the worker's `runCatching` guard turns into false → Result.retry().
     */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean =
        synchronizer.snapshotSync(name = "currency-rates") {
            coroutineScope {
                PINNED_BASE_CURRENCIES.map { base ->
                    async {
                        exchangeRatesStore.stream(StoreReadRequest.fresh(base))
                            .first { it is StoreReadResponse.Data<*> || it is StoreReadResponse.Error }
                            .requireData()
                    }
                }.awaitAll()
            }
        }
}
