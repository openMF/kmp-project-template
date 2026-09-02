/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.demo.alerts

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data-access object for the `alerts` table.
 *
 * All reads return reactive [Flow]s; all writes are `suspend` one-shots.
 * Natural sort order is newest-first ([AlertEntity.createdAt] descending).
 */
@Dao
interface AlertDao {

    /** Observe all alerts, ordered newest-first. */
    @Query("SELECT * FROM alerts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AlertEntity>>

    /** Observe a single alert by id — the per-item write store's SourceOfTruth reader. */
    @Query("SELECT * FROM alerts WHERE id = :id")
    fun observeById(id: String): Flow<AlertEntity?>

    /** Insert or replace a single alert. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(alert: AlertEntity)

    /** Insert or replace a batch of alerts. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(alerts: List<AlertEntity>)

    /** Delete a single alert by id. No-op if absent. */
    @Query("DELETE FROM alerts WHERE id = :id")
    suspend fun deleteById(id: String)

    /** Delete all alerts. */
    @Query("DELETE FROM alerts")
    suspend fun deleteAll()
}
