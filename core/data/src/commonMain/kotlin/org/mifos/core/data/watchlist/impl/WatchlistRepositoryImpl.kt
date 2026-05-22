/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.watchlist.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import org.mifos.core.data.watchlist.WatchlistItem
import org.mifos.core.data.watchlist.WatchlistRepository
import org.mifos.core.database.watchlist.dao.WatchlistDao
import org.mifos.core.database.watchlist.entity.WatchlistEntity

internal class WatchlistRepositoryImpl(
    private val dao: WatchlistDao,
) : WatchlistRepository {

    override fun watchlist(): Flow<List<WatchlistItem>> =
        dao.observeAll().map { rows ->
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
