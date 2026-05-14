/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import org.mifos.core.database.entity.DraftEntity

/**
 * DAO for the framework-owned `framework_submit_drafts` table.
 *
 * Consumed by [org.mifos.core.data.store.RoomSubmitOutbox]. The framework uses this table to
 * persist form payloads that failed to reach the server so users can resume or retry later.
 */
@Dao
interface DraftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DraftEntity): Long

    @Query("SELECT * FROM framework_submit_drafts WHERE id = :id")
    suspend fun getById(id: Long): DraftEntity?

    @Query("SELECT * FROM framework_submit_drafts WHERE formKey = :formKey AND status = 'PENDING' LIMIT 1")
    suspend fun getPendingByFormKey(formKey: String): DraftEntity?

    @Query("SELECT * FROM framework_submit_drafts WHERE formKey = :formKey AND status = 'PENDING' LIMIT 1")
    fun observePendingByFormKey(formKey: String): Flow<DraftEntity?>

    @Query("SELECT * FROM framework_submit_drafts WHERE status = 'PENDING'")
    suspend fun getAllPending(): List<DraftEntity>

    @Query("UPDATE framework_submit_drafts SET status = 'RETRYING', updatedAtMs = :nowMs WHERE id = :id")
    suspend fun markRetrying(id: Long, nowMs: Long)

    @Query("UPDATE framework_submit_drafts SET status = 'SUBMITTED', updatedAtMs = :nowMs WHERE id = :id")
    suspend fun markSubmitted(id: Long, nowMs: Long)

    @Query(
        "UPDATE framework_submit_drafts SET status = 'FAILED', " +
            "updatedAtMs = :nowMs, errorMessage = :error WHERE id = :id",
    )
    suspend fun markFailed(id: Long, nowMs: Long, error: String?)

    @Query("UPDATE framework_submit_drafts SET payloadJson = :payloadJson, updatedAtMs = :nowMs WHERE id = :id")
    suspend fun updatePayload(id: Long, payloadJson: String, nowMs: Long)

    @Query("DELETE FROM framework_submit_drafts WHERE formKey = :formKey")
    suspend fun deleteByFormKey(formKey: String)

    @Query("DELETE FROM framework_submit_drafts")
    suspend fun deleteAll()
}
