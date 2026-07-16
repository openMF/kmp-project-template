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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kpt.core.base.database.invalidation.daoFlow
import kpt.core.base.database.invalidation.notifyingWrite
import kpt.core.data.demo.watchlist.WatchlistItem
import kpt.core.data.demo.watchlist.WatchlistRepository
import kpt.core.database.demo.watchlist.dao.WatchlistDao
import kpt.core.database.demo.watchlist.entity.WatchlistEntity
import kotlin.time.Clock

/**
 * Local-only impl of [WatchlistRepository].
 *
 * Direct-DAO `Flow` reads are wrapped with [daoFlow] and writes with [notifyingWrite] so the
 * wasmJs target's long-lived collectors re-emit after add/remove even when Room 3 alpha05's
 * async InvalidationTracker fails to fan out. On Android/Desktop/iOS the wraps are a
 * microsecond-cost no-op alongside Room's native invalidation. See
 * `core-base/database/.../invalidation/README.md`.
 */
internal class WatchlistRepositoryImpl(
    private val dao: WatchlistDao,
) : WatchlistRepository {

    override fun watchlist(): Flow<List<WatchlistItem>> = daoFlow(WATCHLIST_TABLE) { dao.observeAll() }.map { rows ->
        rows.map { WatchlistItem(coinId = it.coinId, addedAtMs = it.addedAtMs) }
    }

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
