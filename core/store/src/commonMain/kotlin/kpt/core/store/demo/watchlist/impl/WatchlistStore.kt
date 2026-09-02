/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.watchlist.impl

import kotlinx.coroutines.flow.map
import kpt.core.base.database.invalidation.daoFlow
import kpt.core.base.database.invalidation.notifyingWrite
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.demo.watchlist.dao.WatchlistDao
import kpt.core.database.demo.watchlist.entity.WatchlistEntity
import kpt.core.model.demo.watchlist.WatchlistItem
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Offline-only [Store] for the personal watchlist (`read_local_list` demo). Backed exclusively by
 * [WatchlistDao] — no remote fetcher. Emits the DOMAIN [WatchlistItem] (entity→domain map in the
 * `SourceOfTruth`, per the read-path contract); `WatchlistRepositoryImpl` builds the
 * `ScreenDataStream` on top and writes via the DAO's `notifyingWrite`.
 *
 * Key: [Unit] — always returns the full watchlist. The DAO reader is wrapped with [daoFlow] so
 * wasmJs collectors re-emit after writes even when Room 3 alpha05's async InvalidationTracker
 * fails to fan out (no-op on Android/Desktop/iOS).
 */
fun provideWatchlistStore(dao: WatchlistDao): Store<Unit, List<WatchlistItem>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit ->
            daoFlow(WATCHLIST_TABLE) { dao.observeAll() }
                .map { rows -> rows.map { WatchlistItem(coinId = it.coinId, addedAtMs = it.addedAtMs) } }
        },
        // Offline-local writes go through the repository's notifyingWrite path, not the Store writer;
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
 * ([StoreFactory.createOfflineMutableStore] — no-op Updater); the writer/delete fire [notifyingWrite]
 * so the paired [provideWatchlistStore] read collectors re-emit on wasmJs (same `personal_watchlist` table).
 */
fun provideWatchlistWriteStore(dao: WatchlistDao): MutableStore<String, WatchlistItem> =
    StoreFactory.createOfflineMutableStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { coinId: String ->
                daoFlow(WATCHLIST_TABLE) { dao.observeById(coinId) }
                    .map { it?.let { row -> WatchlistItem(coinId = row.coinId, addedAtMs = row.addedAtMs) } }
            },
            writer = { _: String, item: WatchlistItem ->
                notifyingWrite(WATCHLIST_TABLE) {
                    dao.insert(WatchlistEntity(coinId = item.coinId, addedAtMs = item.addedAtMs))
                }
            },
            delete = { coinId: String -> notifyingWrite(WATCHLIST_TABLE) { dao.delete(coinId) } },
            deleteAll = { notifyingWrite(WATCHLIST_TABLE) { dao.deleteAll() } },
        ),
    )

/** Room `@Entity(tableName = …)` for the watchlist table. */
private const val WATCHLIST_TABLE = "personal_watchlist"
