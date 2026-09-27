/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.crypto.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.crypto.entity.CoinDetailEntity

/**
 * Room DAO for cached coin detail — the offline fallback behind `NETWORK_WITH_CACHE`.
 *
 * Bound into the Store's `SourceOfTruth` — the reader/writer/delete lambdas are the ONLY callers
 * of these members (S5-1). A repository reaching past the Store to a DAO bypasses caching and
 * freshness entirely.
 */
@DbDao
@Dao
interface CoinDetailDao {

    /** Inserts or replaces the cached detail row. */
    @Upsert
    suspend fun upsert(entity: CoinDetailEntity)

    /** Streams one coin's detail, emitting null when nothing is cached. */
    @Query("SELECT * FROM coin_detail WHERE id = :coinId LIMIT 1")
    fun getById(coinId: String): Flow<CoinDetailEntity?>

    /**
     * Evicts one coin's cached detail. Used for a targeted refresh; the list cache in `coin_market` is unaffected, so
     * the row stays visible while its detail re-fetches.
     */
    @Query("DELETE FROM coin_detail WHERE id = :coinId")
    suspend fun delete(coinId: String)

    /** Clears the cache. Called on logout. */
    @Query("DELETE FROM coin_detail")
    suspend fun deleteAll()
}
