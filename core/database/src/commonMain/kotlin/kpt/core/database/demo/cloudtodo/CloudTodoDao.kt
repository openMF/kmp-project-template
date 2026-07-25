/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.demo.cloudtodo

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CloudTodoDao {
    @Query("SELECT * FROM cloud_todos WHERE id = :id")
    fun observeById(id: Int): Flow<CloudTodoEntity?>

    @Query("SELECT * FROM cloud_todos WHERE id = :id")
    suspend fun getById(id: Int): CloudTodoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CloudTodoEntity)

    @Query("DELETE FROM cloud_todos WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM cloud_todos")
    suspend fun deleteAll()
}
