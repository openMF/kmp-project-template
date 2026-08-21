/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.watchlist.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.invalidation.daoFlow
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.demo.watchlist.WatchlistRepository
import kpt.core.database.demo.watchlist.dao.WatchlistDao
import kpt.core.model.demo.watchlist.WatchlistItem
import kpt.core.store.AppCacheKeys
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import kotlin.time.Clock

/**
 * Local-only impl of [WatchlistRepository].
 *
 * Store5 single-source-of-truth: every write flows through the [watchlistWriteStore] — `store.write`
 * (add) / `store.clear` (remove). The repository never touches the DAO for writes; the write store's
 * `SourceOfTruth` writer/delete are the only DAO write callers, so Room stays the durable SoT and the
 * paired read store ([watchlistStore]) re-projects reactively (same `personal_watchlist` table). The
 * SoT writer/delete fire [notifyingWrite] so wasmJs collectors re-emit after add/remove even when
 * Room 3 alpha05's async InvalidationTracker fails to fan out (no-op on Android/Desktop/iOS). See
 * `core-base/database/.../invalidation/README.md`. The [dao] is retained only for the reactive
 * [contains] membership read.
 */
internal class WatchlistRepositoryImpl(
    private val watchlistStore: Store<Unit, List<WatchlistItem>>,
    private val watchlistWriteStore: MutableStore<String, WatchlistItem>,
    private val dao: WatchlistDao,
) : WatchlistRepository {

    override fun watchlistStream(scope: CoroutineScope): ScreenDataStream<List<WatchlistItem>> =
        watchlistStore.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.WATCHLIST,
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
            isEmpty = { it.isEmpty() },
        )

    override fun contains(coinId: String): Flow<Boolean> = daoFlow(WATCHLIST_TABLE) { dao.observeContains(coinId) }

    override suspend fun add(coinId: String) {
        // Write through the store — persists to the Room SoT (via the SoT writer); the read store re-emits.
        watchlistWriteStore.write(
            StoreWriteRequest.of<String, WatchlistItem, Any>(
                key = coinId,
                value = WatchlistItem(coinId = coinId, addedAtMs = Clock.System.now().toEpochMilliseconds()),
            ),
        )
    }

    override suspend fun remove(coinId: String) {
        // Clear through the store — removes the row from the Room SoT (via the SoT delete).
        watchlistWriteStore.clear(coinId)
    }

    private companion object {
        /** Room `@Entity(tableName = …)` for [WatchlistEntity]. */
        const val WATCHLIST_TABLE = "personal_watchlist"
    }
}
