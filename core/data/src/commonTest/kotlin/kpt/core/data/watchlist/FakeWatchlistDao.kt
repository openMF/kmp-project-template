/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.watchlist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kpt.core.database.watchlist.dao.WatchlistDao
import kpt.core.database.watchlist.entity.WatchlistEntity

/**
 * In-memory fake of [WatchlistDao] whose reactive reads are backed by a [MutableStateFlow], so a
 * live collector re-emits after every write.
 *
 * That is what a real Room DAO `Flow` does: `InvalidationTracker` fans a write out to existing
 * collectors. Until 2026-09-17 these reads were deliberately COLD (one emission per subscription)
 * to model a wasmJs invalidation gap, which made re-emission the exclusive job of a
 * `RoomChangeBus`/`daoFlow`/`notifyingWrite` bridge. That bridge has been removed — Room
 * 3.1.0-alpha01 was measured re-emitting correctly on js and wasmJs (see
 * `core/database/src/{js,wasmJs}Test/.../WebInvalidationProbeTest.kt`) — so a cold fake now models
 * nothing real and would assert the absence of a mechanism the app relies on.
 */
internal class FakeWatchlistDao : WatchlistDao {

    private val rows = MutableStateFlow<List<WatchlistEntity>>(emptyList())

    override fun observeAll(): Flow<List<WatchlistEntity>> =
        rows.map { list -> list.sortedByDescending { it.addedAtMs } }

    override fun observeContains(coinId: String): Flow<Boolean> =
        rows.map { list -> list.any { it.coinId == coinId } }

    override fun observeById(coinId: String): Flow<WatchlistEntity?> =
        rows.map { list -> list.firstOrNull { it.coinId == coinId } }

    override suspend fun insert(entry: WatchlistEntity) {
        rows.update { list -> list.filterNot { it.coinId == entry.coinId } + entry }
    }

    override suspend fun delete(coinId: String) {
        rows.update { list -> list.filterNot { it.coinId == coinId } }
    }

    override suspend fun deleteAll() {
        rows.update { emptyList() }
    }
}
