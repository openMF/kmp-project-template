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

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.invalidation.daoFlow
import kpt.core.base.database.invalidation.notifyingWrite
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.demo.watchlist.WatchlistRepository
import kpt.core.database.demo.watchlist.dao.WatchlistDao
import kpt.core.database.demo.watchlist.entity.WatchlistEntity
import kpt.core.model.demo.watchlist.WatchlistItem
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

/**
 * Local-only impl of [WatchlistRepository].
 *
 * Read-path contract: the repository builds the [ScreenDataStream] over the offline-local
 * [kpt.core.store.demo.watchlist.impl.provideWatchlistStore] (CACHE_ONLY); the ViewModel consumes
 * `.state`. Writes go direct-to-DAO via [notifyingWrite] so wasmJs collectors re-emit after
 * add/remove even when Room 3 alpha05's async InvalidationTracker fails to fan out (no-op on
 * Android/Desktop/iOS). See `core-base/database/.../invalidation/README.md`.
 */
internal class WatchlistRepositoryImpl(
    private val watchlistStore: Store<Unit, List<WatchlistItem>>,
    private val dao: WatchlistDao,
    private val networkMonitor: NetworkMonitor,
    private val fetchedAtRepository: FetchedAtRepository,
) : WatchlistRepository {

    override fun watchlistStream(scope: CoroutineScope): ScreenDataStream<List<WatchlistItem>> =
        watchlistStore.asScreenStream(
            key = Unit,
            networkMonitor = networkMonitor,
            fetchedAtRepository = fetchedAtRepository,
            cacheKey = "watchlist",
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
            isEmpty = { it.isEmpty() },
        )

    override fun contains(coinId: String): Flow<Boolean> = daoFlow(WATCHLIST_TABLE) { dao.observeContains(coinId) }

    override suspend fun add(coinId: String) {
        notifyingWrite(WATCHLIST_TABLE) {
            dao.insert(WatchlistEntity(coinId = coinId, addedAtMs = Clock.System.now().toEpochMilliseconds()))
        }
    }

    override suspend fun remove(coinId: String) {
        notifyingWrite(WATCHLIST_TABLE) {
            dao.delete(coinId)
        }
    }

    private companion object {
        /** Room `@Entity(tableName = …)` for [WatchlistEntity]. */
        const val WATCHLIST_TABLE = "personal_watchlist"
    }
}
