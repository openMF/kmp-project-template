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
import kpt.core.data.demo.watchlist.WatchlistItem
import kpt.core.data.demo.watchlist.WatchlistRepository
import kpt.core.database.demo.watchlist.dao.WatchlistDao
import kpt.core.database.demo.watchlist.entity.WatchlistEntity
import kotlin.time.Clock

internal class WatchlistRepositoryImpl(
    private val dao: WatchlistDao,
) : WatchlistRepository {

    override fun watchlist(): Flow<List<WatchlistItem>> = dao.observeAll().map { rows ->
        rows.map { WatchlistItem(coinId = it.coinId, addedAtMs = it.addedAtMs) }
    }

    override fun contains(coinId: String): Flow<Boolean> = dao.observeContains(coinId)

    override suspend fun add(coinId: String) {
        dao.insert(WatchlistEntity(coinId = coinId, addedAtMs = Clock.System.now().toEpochMilliseconds()))
    }

    override suspend fun remove(coinId: String) {
        dao.delete(coinId)
    }
}
