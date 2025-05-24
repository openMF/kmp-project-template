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
import template.core.base.database.Dao
import template.core.base.database.Insert
import template.core.base.database.OnConflictStrategy
import template.core.base.database.Query

@Dao
interface SampleDao {

    @Query("SELECT * FROM samples")
    fun getAllSamples(): Flow<List<SampleEntity>>

    @Insert(entity = SampleEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(charge: List<SampleEntity>)
}
