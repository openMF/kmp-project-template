/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.watchlist.impl

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.watchlist.dao.WatchlistDao
import kpt.core.database.watchlist.entity.WatchlistEntity
import kpt.core.model.watchlist.WatchlistItem
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Offline-only [Store] for the personal watchlist (`read_local_list` demo). Backed exclusively by
 * [WatchlistDao] — no remote fetcher. Emits the DOMAIN [WatchlistItem] (entity→domain map in the
 * `SourceOfTruth`, per the read-path contract); `WatchlistRepositoryImpl` builds the
 * `ScreenDataStream` on top and writes via the DAO's a plain DAO write.
 *
 * Key: [Unit] — always returns the full watchlist. The DAO reader is a plain Room `Flow` so
 * wasmJs collectors re-emit after writes even when Room 3 alpha05's async InvalidationTracker
 * fails to fan out (no-op on Android/Desktop/iOS).
 */
@StoreProvider(id = "watchlist")
@CacheKey(name = "LIST", key = "watchlist")
fun provideWatchlistStore(dao: WatchlistDao): Store<Unit, List<WatchlistItem>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit ->
            dao.observeAll()
                .map { rows -> rows.map { WatchlistItem(coinId = it.coinId, addedAtMs = it.addedAtMs) } }
        },
        // Offline-local writes go through the repository's DAO path, not the Store writer;
        // this writer keeps the SourceOfTruth complete for any direct store.write().
        writer = { _: Unit, _: List<WatchlistItem> -> Unit },
        delete = { _: Unit -> dao.deleteAll() },
        deleteAll = { dao.deleteAll() },
    ),
)

/**
 * Per-item WRITE store for the watchlist (keyed by coin id). Every mutation flows through
 * `store.write` (add) / `store.clear` (remove), so `WatchlistRepositoryImpl` never touches the DAO for
 * writes — the SoT writer/delete are the single DAO callers. Local-only
 * ([StoreFactory.createOfflineMutableStore] — no-op Updater); the writer/delete are plain DAO writes
 * so the paired [provideWatchlistStore] read collectors re-emit on wasmJs (same `personal_watchlist` table).
 */
@StoreProvider(id = "watchlistMutable", logout = false)
fun provideWatchlistWriteStore(dao: WatchlistDao): MutableStore<String, WatchlistItem> =
    StoreFactory.createOfflineMutableStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { coinId: String ->
                dao.observeById(coinId)
                    .map { it?.let { row -> WatchlistItem(coinId = row.coinId, addedAtMs = row.addedAtMs) } }
            },
            writer = { _: String, item: WatchlistItem ->
                dao.insert(WatchlistEntity(coinId = item.coinId, addedAtMs = item.addedAtMs))
            },
            delete = { coinId: String -> dao.delete(coinId) },
            deleteAll = { dao.deleteAll() },
        ),
    )
