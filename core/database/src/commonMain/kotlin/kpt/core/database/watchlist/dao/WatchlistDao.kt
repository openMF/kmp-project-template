/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.watchlist.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.database.watchlist.entity.WatchlistEntity

/**
 * Data-access object for the `personal_watchlist` table.
 *
 * Reads are reactive [Flow]s; writes are `suspend`. Standard Room 3 KMP shape.
 */
@Dao
interface WatchlistDao {

    /** Observe the full watchlist, ordered by addition time (newest first). */
    @Query("SELECT * FROM personal_watchlist ORDER BY addedAtMs DESC")
    fun observeAll(): Flow<List<WatchlistEntity>>

    /** Reactive in-membership check for a given coin — emits whenever the watchlist changes. */
    @Query("SELECT EXISTS(SELECT 1 FROM personal_watchlist WHERE coinId = :coinId)")
    fun observeContains(coinId: String): Flow<Boolean>

    /** Add a coin to the watchlist. If already present, replaces the row (no-op effectively). */
    @Insert(entity = WatchlistEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WatchlistEntity)

    /** Remove a coin from the watchlist. No-op if absent. */
    @Query("DELETE FROM personal_watchlist WHERE coinId = :coinId")
    suspend fun delete(coinId: String)
}
