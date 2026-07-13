/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.banking

import kotlinx.coroutines.flow.Flow
import kpt.core.model.demo.banking.BillReminder

/**
 * User's bill reminders — purely local persistence, no remote sync.
 *
 * Backs the B4 Bill Reminders feature. Reads are reactive [Flow]s; writes are
 * `suspend`. As with [LoanRepository], the UI layer wraps writes in
 * `DraftSubmitHandler` for the offline-resilience polish.
 */
interface BillReminderRepository {

    /** Observe every bill reminder (enabled and disabled), day-of-month order. */
    fun observeAll(): Flow<List<BillReminder>>

    /**
     * Observe enabled bill reminders whose `dueDay` falls within the next
     * [maxDays] starting from today. The window wraps across the month boundary —
     * a reminder on day 3 *is* "upcoming" when today is day 30 and
     * [maxDays] >= 4.
     *
     * @param maxDays Lookahead horizon (inclusive). `0` returns reminders due today.
     */
    fun observeUpcoming(maxDays: Int): Flow<List<BillReminder>>

    /** Observe a single bill reminder. Emits `null` after deletion. */
    fun observeById(id: String): Flow<BillReminder?>

    /** One-shot read. */
    suspend fun getById(id: String): BillReminder?

    /** Insert-or-replace. Idempotent. */
    suspend fun upsert(bill: BillReminder)

    /** Delete by id. No-op if absent. */
    suspend fun delete(id: String)

    /**
     * Sum of [BillReminder.amount] across upcoming reminders within the same
     * window semantics as [observeUpcoming]. Powers the dashboard
     * "Due in next X days" tile.
     */
    fun observeTotalUpcomingAmount(maxDays: Int): Flow<Double>

    /** Reactive row count — used by the dashboard badge. */
    fun observeCount(): Flow<Int>
}
