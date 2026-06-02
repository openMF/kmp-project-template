/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.banking.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kpt.core.data.banking.BillReminderRepository
import kpt.core.database.banking.dao.BillReminderDao
import kpt.core.database.banking.entity.BillReminderEntity
import kpt.core.model.banking.BillReminder
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import kotlin.time.Clock

/**
 * Local-only impl of [BillReminderRepository].
 *
 * The "upcoming" window math (today + N days → set of day-of-month integers)
 * runs in the repository so the DAO stays trivial and portable across SQLite
 * engines. [Clock] + [TimeZone] are injected so tests can fix "today".
 *
 * [observeAll] delegates to [billRemindersStore] so any write through the Store's
 * SourceOfTruth is reflected here reactively. All other reads and all writes go
 * directly to [billReminderDao] (filtered reads, per-id lookup, upsert, delete).
 */
internal class BillReminderRepositoryImpl(
    private val billRemindersStore: Store<Unit, List<BillReminderEntity>>,
    private val billReminderDao: BillReminderDao,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : BillReminderRepository {

    override fun observeAll(): Flow<List<BillReminder>> =
        billRemindersStore.stream(StoreReadRequest.cached(Unit, refresh = false))
            .filterIsInstance<StoreReadResponse.Data<List<BillReminderEntity>>>()
            .map { response -> response.value.map { it.toDomain() } }

    override fun observeUpcoming(maxDays: Int): Flow<List<BillReminder>> {
        val window = upcomingDayWindow(maxDays)
        if (window.isEmpty()) return flowOf(emptyList())
        return billReminderDao.observeUpcoming(window).map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeTotalUpcomingAmount(maxDays: Int): Flow<Double> {
        val window = upcomingDayWindow(maxDays)
        if (window.isEmpty()) return flowOf(0.0)
        return billReminderDao.observeUpcoming(window).map { rows -> rows.sumOf { it.amount } }
    }

    override fun observeById(id: String): Flow<BillReminder?> =
        billReminderDao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): BillReminder? = billReminderDao.getById(id)?.toDomain()

    override suspend fun upsert(bill: BillReminder) {
        billReminderDao.upsert(bill.toEntity())
    }

    override suspend fun delete(id: String) {
        billReminderDao.deleteById(id)
    }

    override fun observeCount(): Flow<Int> = billReminderDao.count()

    /**
     * Returns the set of day-of-month integers covered by `[today, today + maxDays]`,
     * wrapping across month boundaries. Returned values are clamped to 1..31; the
     * downstream UI is responsible for skipping non-existent calendar days
     * (e.g. day 31 in February).
     *
     * @param maxDays Inclusive window length in days. Negative → empty set.
     */
    private fun upcomingDayWindow(maxDays: Int): Set<Int> {
        if (maxDays < 0) return emptySet()
        val today: LocalDate = clock.todayIn(timeZone)
        // Build (maxDays + 1) consecutive day-of-month values starting today.
        return buildSet {
            var date = today
            repeat(maxDays + 1) {
                add(date.day)
                date = date.nextDay()
            }
        }
    }
}

/** Cheap +1 day without dragging in DatePeriod arithmetic. */
private fun LocalDate.nextDay(): LocalDate {
    // Compose epoch-day directly to avoid the DatePeriod plus-operator overhead
    // and keep behaviour identical across the JS/Wasm/Native targets, where
    // kotlinx-datetime's plus operator is supported but slower than raw arithmetic.
    return LocalDate.fromEpochDays(this.toEpochDays() + 1)
}

private fun BillReminderEntity.toDomain(): BillReminder = BillReminder(
    id = id,
    name = name,
    amount = amount,
    dueDay = dueDay,
    recurrence = recurrence,
    category = category,
    enabled = enabled,
    reminderDaysBefore = reminderDaysBefore,
    createdAtMs = createdAtMs,
    updatedAtMs = updatedAtMs,
)

private fun BillReminder.toEntity(): BillReminderEntity = BillReminderEntity(
    id = id,
    name = name,
    amount = amount,
    dueDay = dueDay,
    recurrence = recurrence,
    category = category,
    enabled = enabled,
    reminderDaysBefore = reminderDaysBefore,
    createdAtMs = createdAtMs,
    updatedAtMs = updatedAtMs,
)
