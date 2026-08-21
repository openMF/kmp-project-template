/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.banking.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kpt.core.base.database.invalidation.daoFlow
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.demo.banking.BillReminderRepository
import kpt.core.database.demo.banking.dao.BillReminderDao
import kpt.core.model.demo.banking.BillReminder
import kpt.core.store.demo.banking.impl.toDomain
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import kotlin.time.Clock

/**
 * Local-only impl of [BillReminderRepository].
 *
 * Read-path contract: [billRemindersStream] builds the offline-local [ScreenDataStream] (CACHE_ONLY)
 * over the domain-emitting [kpt.core.store.demo.banking.impl.provideBillRemindersStore]; read screens
 * consume `.state`. The "upcoming" window math runs in the repository; [Clock] + [TimeZone] are
 * injected so tests can fix "today". Direct-DAO `Flow` reads (`observeUpcoming`, `observeById`, the
 * dashboard aggregates) are wrapped with [daoFlow] and writes with [notifyingWrite] so the wasmJs
 * target's long-lived collectors re-emit after writes even when Room 3 alpha05's async
 * InvalidationTracker fails to fan out (no-op on Android/Desktop/iOS).
 */
internal class BillReminderRepositoryImpl(
    private val billRemindersStore: Store<Unit, List<BillReminder>>,
    private val billRemindersWriteStore: MutableStore<String, BillReminder>,
    private val billReminderDao: BillReminderDao,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : BillReminderRepository {

    override fun observeAll(): Flow<List<BillReminder>> =
        billRemindersStore.stream(StoreReadRequest.cached(Unit, refresh = false))
            .filterIsInstance<StoreReadResponse.Data<List<BillReminder>>>()
            .map { response -> response.value }

    override fun billRemindersStream(scope: CoroutineScope): ScreenDataStream<List<BillReminder>> =
        billRemindersStore.asScreenStream(
            key = Unit,
            cacheKey = "billReminders",
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
            isEmpty = { it.isEmpty() },
        )

    override fun observeUpcoming(maxDays: Int): Flow<List<BillReminder>> {
        val window = upcomingDayWindow(maxDays)
        if (window.isEmpty()) return flowOf(emptyList())
        return daoFlow(BILL_REMINDERS_TABLE) { billReminderDao.observeUpcoming(window) }
            .map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeTotalUpcomingAmount(maxDays: Int): Flow<Double> {
        val window = upcomingDayWindow(maxDays)
        if (window.isEmpty()) return flowOf(0.0)
        return daoFlow(BILL_REMINDERS_TABLE) { billReminderDao.observeUpcoming(window) }
            .map { rows -> rows.sumOf { it.amount } }
    }

    override fun observeById(id: String): Flow<BillReminder?> =
        daoFlow(BILL_REMINDERS_TABLE) { billReminderDao.observeById(id) }.map { it?.toDomain() }

    override suspend fun getById(id: String): BillReminder? = billReminderDao.getById(id)?.toDomain()

    override suspend fun upsert(bill: BillReminder) {
        // Write through the store — persists to the Room SoT (via the SoT writer); readers re-emit.
        billRemindersWriteStore.write(
            StoreWriteRequest.of<String, BillReminder, Any>(key = bill.id, value = bill),
        )
    }

    override suspend fun delete(id: String) {
        // Clear through the store — removes the row from the Room SoT (via the SoT delete).
        billRemindersWriteStore.clear(id)
    }

    override fun observeCount(): Flow<Int> = daoFlow(BILL_REMINDERS_TABLE) { billReminderDao.count() }

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

    private companion object {
        /** Room `@Entity(tableName = …)` for the bill-reminders table. */
        const val BILL_REMINDERS_TABLE = "banking_bill_reminders"
    }
}

/** Cheap +1 day without dragging in DatePeriod arithmetic. */
private fun LocalDate.nextDay(): LocalDate {
    // Compose epoch-day directly to avoid the DatePeriod plus-operator overhead
    // and keep behaviour identical across the JS/Wasm/Native targets, where
    // kotlinx-datetime's plus operator is supported but slower than raw arithmetic.
    return LocalDate.fromEpochDays(this.toEpochDays() + 1)
}
