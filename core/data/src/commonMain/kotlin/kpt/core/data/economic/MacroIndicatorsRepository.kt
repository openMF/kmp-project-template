/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.economic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.data.Syncable
import kpt.core.model.economic.MacroIndicator
import kpt.core.store.economic.impl.MacroIndicatorKey

/**
 * Repository surface for World Bank macro-indicator series.
 *
 * Consumes the `MacroIndicator` Store5 cache under the hood — callers get a
 * [ScreenDataStream] that emits loading/empty/error/content transitions
 * automatically (per the toolkit's offline-first store contract).
 *
 * Implements [Syncable] — the `sync/` module's `DataSyncWorker` calls
 * [syncWith] to force-refresh the macro cache on a schedule.
 */
interface MacroIndicatorsRepository : Syncable {

    /**
     * Stream observations for a single static (country, indicator) pair.
     *
     * [fetchPolicy] defaults to [FetchPolicy.NETWORK_WITH_CACHE] for UI callers;
     * the `syncWith` forced-refresh seam passes [FetchPolicy.NETWORK_ONLY] to
     * drive a network re-fetch through Store5's SourceOfTruth (D21).
     */
    fun macroIndicatorStream(
        key: MacroIndicatorKey,
        scope: CoroutineScope,
        fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
    ): ScreenDataStream<MacroIndicator>

    /**
     * Stream observations for a parameter-flow — typically driven by a UI
     * picker that lets the user switch countries / indicators.
     */
    fun macroIndicatorStream(
        keyFlow: Flow<MacroIndicatorKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<MacroIndicator>

    // Syncable.syncWith(synchronizer) is implemented in MacroIndicatorsRepositoryImpl —
    // force-refreshes each pinned (country, indicator) pair by re-collecting one
    // emission from macroIndicatorStream(key, scope, NETWORK_ONLY). See GOAL.md D21 + Δ-3.
}
