/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.dao

import kotlinx.coroutines.flow.Flow
import org.mifos.core.database.entity.SampleEntity
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query

/**
 * Data-access object for the `samples` table.
 *
 * All functions are either `suspend` (one-shot write operations) or return a [Flow]
 * (observable reads), as required by Room 3 for multiplatform compatibility.
 */
@Dao
interface SampleDao {

    /**
     * Observes all rows in the `samples` table.
     *
     * @return A [Flow] that emits the full list of [SampleEntity] whenever the table changes.
     */
    @Query("SELECT * FROM samples")
    fun getAllSamples(): Flow<List<SampleEntity>>

    /**
     * Inserts or replaces a batch of [SampleEntity] rows.
     *
     * Uses [OnConflictStrategy.REPLACE] so that rows with duplicate primary keys are
     * overwritten rather than causing a constraint violation.
     *
     * @param charge The list of entities to upsert.
     */
    @Insert(entity = SampleEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(charge: List<SampleEntity>)
}
