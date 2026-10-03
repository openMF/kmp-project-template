/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.currency.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.currency.entity.RateHistoryEntity

/**
 * Room DAO for cached historical FX series, keyed by pair and window.
 *
 * Bound into the Store's `SourceOfTruth` — the reader/writer/delete lambdas are the ONLY callers
 * of these members (S5-1). A repository reaching past the Store to a DAO bypasses caching and
 * freshness entirely.
 */
@DbDao
@Dao
interface RateHistoryDao {

    /** Inserts or replaces one cached series. */
    @Upsert
    suspend fun upsert(entity: RateHistoryEntity)

    /**
     * Streams one exact series. The date range is part of the lookup because a widened window is a different key, not
     * a page append.
     */
    @Query(
        """
        SELECT * FROM rate_history
        WHERE fromCurrency = :from AND toCurrency = :to
        AND startDate = :startDate AND endDate = :endDate
        LIMIT 1
        """,
    )
    fun get(from: String, to: String, startDate: String, endDate: String): Flow<RateHistoryEntity?>

    /** Evicts every series for one pair. */
    @Query("DELETE FROM rate_history WHERE fromCurrency = :from AND toCurrency = :to")
    suspend fun delete(from: String, to: String)

    /** Clears the cache. Called on logout. */
    @Query("DELETE FROM rate_history")
    suspend fun deleteAll()
}
