/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.banking.dao

import androidx.room3.Dao
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import org.mifos.core.database.banking.entity.LoanEntity

/**
 * Data-access object for the `banking_loans` table.
 *
 * Reads are reactive [Flow]s; writes are `suspend`. Standard Room 3 KMP shape.
 * The natural sort order is by [LoanEntity.nextDueDate] ascending so the
 * dashboard surfaces the most pressing payment first.
 */
@Dao
interface LoanDao {

    /** Observe all loans, soonest-due first. Emits on every change. */
    @Query("SELECT * FROM banking_loans ORDER BY nextDueDate ASC, createdAtMs ASC")
    fun observeAll(): Flow<List<LoanEntity>>

    /** Observe a single loan by [id]. Emits `null` if it has been deleted. */
    @Query("SELECT * FROM banking_loans WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<LoanEntity?>

    /** One-shot read for non-reactive callers (mappers, tests, migrations). */
    @Query("SELECT * FROM banking_loans WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LoanEntity?

    /** Reactive row count — used by the dashboard badge. */
    @Query("SELECT COUNT(*) FROM banking_loans")
    fun count(): Flow<Int>

    /** Insert-or-replace. The `id` is the primary key, so re-saving overwrites. */
    @Upsert(entity = LoanEntity::class)
    suspend fun upsert(entity: LoanEntity)

    /** Remove a loan by [id]. No-op if absent. */
    @Query("DELETE FROM banking_loans WHERE id = :id")
    suspend fun deleteById(id: String)

    @Suppress("unused") // surface for future bulk-clear UX (Settings → Reset)
    @Query("DELETE FROM banking_loans")
    suspend fun deleteAll()

    @Suppress("unused") // explicit alias so call-sites can pick the most readable form
    fun observeAllByNextDue(): Flow<List<LoanEntity>> = observeAll()

    companion object {
        // Pinning [OnConflictStrategy] usage to a constant keeps the
        // `@Upsert` semantics discoverable from grep without touching Room internals.
        @Suppress("unused")
        val UPSERT_STRATEGY = OnConflictStrategy.REPLACE
    }
}
