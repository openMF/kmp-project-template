/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.economic.impl

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
import kpt.core.data.economic.MacroIndicatorsRepository
import kpt.core.data.infra.Synchronizer
import kpt.core.data.infra.snapshotSync
import kpt.core.model.economic.IndicatorKind
import kpt.core.model.economic.MacroIndicator
import kpt.core.store.economic.impl.MacroIndicatorKey
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse

/**
 * Pinned (country, indicator) pairs the [syncWith] forced-refresh fans out
 * across. Hardcoded at v1; payload-driven extensibility is a follow-up. Pinned
 * to a small set so we don't accidentally bulk-fetch the World Bank API.
 */
private val PINNED_MACRO_KEYS = listOf(
    MacroIndicatorKey(countryCode = "US", indicator = IndicatorKind.GDP),
    MacroIndicatorKey(countryCode = "IN", indicator = IndicatorKind.GDP),
)

class MacroIndicatorsRepositoryImpl(
    private val macroIndicatorStore: Store<MacroIndicatorKey, MacroIndicator>,
    private val networkMonitor: NetworkMonitor,
    private val fetchedAtRepository: FetchedAtRepository,
) : MacroIndicatorsRepository {

    override fun macroIndicatorStream(
        key: MacroIndicatorKey,
        scope: CoroutineScope,
        fetchPolicy: FetchPolicy,
    ): ScreenDataStream<MacroIndicator> = macroIndicatorStore.asScreenStream(
        key = key,
        networkMonitor = networkMonitor,
        fetchedAtRepository = fetchedAtRepository,
        cacheKey = "economic:macro:${key.countryCode}:${key.indicator.name}:${key.years}y",
        scope = scope,
        fetchPolicy = fetchPolicy,
    )

    override fun macroIndicatorStream(
        keyFlow: Flow<MacroIndicatorKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<MacroIndicator> = macroIndicatorStore.asScreenStream(
        keyFlow = keyFlow,
        networkMonitor = networkMonitor,
        fetchedAtRepository = fetchedAtRepository,
        cacheKeyFor = { key ->
            "economic:macro:${key.countryCode}:${key.indicator.name}:${key.years}y"
        },
        scope = scope,
    )

    /**
     * Forced-refresh seam called by `sync/` module's `DataSyncWorker`.
     *
     * For each pinned (country, indicator) pair (in parallel) issues Store5's one-shot
     * `stream(StoreReadRequest.fresh(key))` — the forced-refresh seam. `fresh`
     * bypasses the cache, hits World Bank via the fetcher, and writes through
     * SourceOfTruth automatically. A cold `stream(fresh)` completes on the first
     * terminal response so the worker returns promptly — unlike `asScreenStream`, whose
     * scope-launched auto-refresh coroutines are for long-lived UI subscriptions.
     */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean =
        synchronizer.snapshotSync(name = "macro-indicators") {
            coroutineScope {
                PINNED_MACRO_KEYS.map { key ->
                    async {
                        macroIndicatorStore.stream(StoreReadRequest.fresh(key))
                            .first { it is StoreReadResponse.Data<*> || it is StoreReadResponse.Error }
                            .requireData()
                    }
                }.awaitAll()
            }
        }
}
