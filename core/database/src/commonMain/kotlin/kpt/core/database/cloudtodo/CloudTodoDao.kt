/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.cloudtodo

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao

/**
 * Room DAO for the cloud-todo demo rows — the MUTABLE (offline-write) archetype's source of truth.
 *
 * Bound into the Store's `SourceOfTruth` — the reader/writer/delete lambdas are the ONLY callers
 * of these members (S5-1). A repository reaching past the Store to a DAO bypasses caching and
 * freshness entirely.
 */
@DbDao
@Dao
interface CloudTodoDao {
    /** Streams one todo, emitting null while it is absent — the Store's local read. */
    @Query("SELECT * FROM cloud_todos WHERE id = :id")
    fun observeById(id: Int): Flow<CloudTodoEntity?>

    /** One-shot read, for the Updater's read-modify-write. */
    @Query("SELECT * FROM cloud_todos WHERE id = :id")
    suspend fun getById(id: Int): CloudTodoEntity?

    /** Inserts or replaces — the Store's local write. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CloudTodoEntity)

    /** Removes one todo. */
    @Query("DELETE FROM cloud_todos WHERE id = :id")
    suspend fun deleteById(id: Int)

    /** Clears every row. Called on logout. */
    @Query("DELETE FROM cloud_todos")
    suspend fun deleteAll()
}
