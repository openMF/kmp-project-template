/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.watchlist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kpt.core.database.demo.watchlist.dao.WatchlistDao
import kpt.core.database.demo.watchlist.entity.WatchlistEntity

/**
 * In-memory fake of [WatchlistDao] whose reactive reads are **cold snapshots** — each
 * subscription emits the current rows exactly once, then completes.
 *
 * This deliberately models the wasmJs failure mode the invalidation bridge exists to
 * absorb: Room 3 alpha05's `InvalidationTracker` does NOT fan out to a live collector
 * after a write on the single-threaded JS event loop, so the DAO `Flow` never re-emits
 * on its own. A correct repository therefore MUST re-emit via `daoFlow { }` re-subscribing
 * on the `RoomChangeBus` signal published by `notifyingWrite { }`.
 *
 * Contrast with [kpt.core.data.demo.banking.FakeLoanDao], which uses a hot
 * `MutableStateFlow` — that self-emits and RACES the `daoFlow` re-collect, which is why
 * the banking reactive tests are `@Ignore`d. A cold snapshot has exactly one re-emit
 * source (the bridge), so the assertion is deterministic.
 */
internal class FakeWatchlistDao : WatchlistDao {

    private val rows = mutableListOf<WatchlistEntity>()

    override fun observeAll(): Flow<List<WatchlistEntity>> =
        flow { emit(rows.sortedByDescending { it.addedAtMs }) }

    override fun observeContains(coinId: String): Flow<Boolean> =
        flow { emit(rows.any { it.coinId == coinId }) }

    override fun observeById(coinId: String): Flow<WatchlistEntity?> =
        flow { emit(rows.firstOrNull { it.coinId == coinId }) }

    override suspend fun insert(entry: WatchlistEntity) {
        rows.removeAll { it.coinId == entry.coinId }
        rows.add(entry)
    }

    override suspend fun delete(coinId: String) {
        rows.removeAll { it.coinId == coinId }
    }

    override suspend fun deleteAll() {
        rows.clear()
    }
}
