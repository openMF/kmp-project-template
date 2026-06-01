/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.banking.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.database.banking.entity.BillReminderEntity

/**
 * Data-access object for the `banking_bill_reminders` table.
 *
 * Reads are reactive [Flow]s; writes are `suspend`. Natural sort order is by
 * [BillReminderEntity.dueDay] ascending — bills due earliest in the month
 * appear first in the dashboard.
 *
 * The `observeUpcoming` query is intentionally simple: it filters by a numeric
 * day-of-month window. The repository layer is responsible for the calendar
 * math (today + N days → window of acceptable `dueDay` values) so the DAO
 * stays portable across SQLite engines that lack a `DATE` function.
 */
@Dao
interface BillReminderDao {

    /** Observe all bill reminders, ordered by day-of-month then creation. */
    @Query(
        "SELECT * FROM banking_bill_reminders " +
            "ORDER BY dueDay ASC, createdAtMs ASC",
    )
    fun observeAll(): Flow<List<BillReminderEntity>>

    /**
     * Observe reminders whose `dueDay` falls within the supplied set of
     * day-of-month integers. The repository computes the set from today's
     * date + `maxDays`. Returns only enabled reminders by default.
     *
     * @param dueDays Inclusive set of `dueDay` values to match.
     */
    @Query(
        "SELECT * FROM banking_bill_reminders " +
            "WHERE enabled = 1 AND dueDay IN (:dueDays) " +
            "ORDER BY dueDay ASC, createdAtMs ASC",
    )
    fun observeUpcoming(dueDays: Set<Int>): Flow<List<BillReminderEntity>>

    /** Observe a single bill reminder. Emits `null` after deletion. */
    @Query("SELECT * FROM banking_bill_reminders WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<BillReminderEntity?>

    /** One-shot read for non-reactive callers. */
    @Query("SELECT * FROM banking_bill_reminders WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): BillReminderEntity?

    /** Reactive row count — used by the dashboard badge. */
    @Query("SELECT COUNT(*) FROM banking_bill_reminders")
    fun count(): Flow<Int>

    /** Insert-or-replace by primary key. */
    @Upsert(entity = BillReminderEntity::class)
    suspend fun upsert(entity: BillReminderEntity)

    /** Delete a bill reminder by id. No-op if absent. */
    @Query("DELETE FROM banking_bill_reminders WHERE id = :id")
    suspend fun deleteById(id: String)

    @Suppress("unused") // future surface for Settings → Reset
    @Query("DELETE FROM banking_bill_reminders")
    suspend fun deleteAll()
}
