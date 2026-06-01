/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.economic

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data-access object for the `interest_rate_series` table.
 *
 * All reads return reactive [Flow]s; all writes are `suspend` one-shots.
 * Default sort order within each series is newest-first ([InterestRateSeriesEntity.date]
 * descending) so the most recent observation is always the first element.
 */
@Dao
interface InterestRateSeriesDao {

    /**
     * Observe all data-points for a given [seriesId], ordered newest-first.
     *
     * @param seriesId FRED series identifier (e.g. "FEDFUNDS").
     */
    @Query("SELECT * FROM interest_rate_series WHERE seriesId = :seriesId ORDER BY date DESC")
    fun observeBySeriesId(seriesId: String): Flow<List<InterestRateSeriesEntity>>

    /** Observe all cached data-points across all series, ordered newest-first. */
    @Query("SELECT * FROM interest_rate_series ORDER BY date DESC")
    fun observeAll(): Flow<List<InterestRateSeriesEntity>>

    /** Insert or replace a batch of data-points (used during cache refresh). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<InterestRateSeriesEntity>)

    /** Delete all cached data-points for a given series. */
    @Query("DELETE FROM interest_rate_series WHERE seriesId = :seriesId")
    suspend fun deleteBySeriesId(seriesId: String)

    /** Insert or replace a single data-point. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: InterestRateSeriesEntity)

    /** Delete all cached data-points across all series. */
    @Query("DELETE FROM interest_rate_series")
    suspend fun deleteAll()
}
