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
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.demo.banking.BillReminderRepository
import kpt.core.database.demo.banking.dao.BillReminderDao
import kpt.core.model.demo.banking.BillReminder
import kpt.core.store.AppCacheKeys
import kpt.core.store.demo.banking.impl.provideBillReminderDetailStore
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import kotlin.time.Clock

/**
 * Local-only impl of [BillReminderRepository].
 *
 * Writes flow through the single write door [billRemindersWriteStore] (`store.write` / `store.clear`);
 * the read `Store` re-projects via [billRemindersStream]'s `asScreenStream`. Aggregate / filter /
 * derived projections (`observeUpcoming` / `observeTotalUpcomingAmount`) are computed from that
 * same store list rather than issuing their own DAO queries, so there is exactly ONE read path.
 * [Clock] + [TimeZone] are injected so the "upcoming" window math is deterministic under test.
 */
internal class BillReminderRepositoryImpl(
    private val billRemindersStore: Store<Unit, List<BillReminder>>,
    private val billRemindersWriteStore: MutableStore<String, BillReminder>,
    private val billReminderDao: BillReminderDao,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : BillReminderRepository {

    override fun billRemindersStream(scope: CoroutineScope): ScreenDataStream<List<BillReminder>> =
        billRemindersStore.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.BILL_REMINDERS,
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
            isEmpty = { it.isEmpty() },
        )

    override fun observeUpcoming(maxDays: Int): Flow<List<BillReminder>> {
        val window = upcomingDayWindow(maxDays)
        if (window.isEmpty()) return flowOf(emptyList())
        // Derived from the STORE's list, not a second `dao.observeUpcoming(window)` query.
        // The DAO query was `WHERE enabled = 1 AND dueDay IN (:dueDays) ORDER BY dueDay ASC,
        // createdAtMs ASC` — exactly this filter over the rows the read store already carries with
        // that same ordering, so the projection is behaviour-identical while leaving ONE read path
        // (the last S5-2 split-read in the codebase). The window math stays here rather than moving
        // into the two calling ViewModels, which pass different windows and must not each own a
        // copy of the calendar logic.
        return allRemindersFlow()
            .map { rows -> rows.filter { it.enabled && it.dueDay in window } }
    }

    /**
     * Every reminder, straight off the read store's source of truth.
     *
     * `localOnly` is a pure SoT read — no fetcher leg, no refresh flag — so this is the store's own
     * data, not a parallel query that could disagree with it.
     *
     * `mapNotNull` is load-bearing, not defensive noise: Store5 can emit `Data` with a NULL value
     * straight from the SourceOfTruth reader despite the `Output : Any` bound (the platform-type
     * leak `StoreDataMapper` guards with its own `as? Output ?: return@transform`). An empty
     * `banking_bill_reminders` table — a fresh install — is exactly that case, so a plain
     * `map { it.value }` would push null into this non-null `Flow<List<…>>` and NPE in the
     * caller's `filter`.
     */
    private fun allRemindersFlow(): Flow<List<BillReminder>> =
        billRemindersStore.stream(StoreReadRequest.localOnly(Unit))
            .filterIsInstance<StoreReadResponse.Data<List<BillReminder>>>()
            .mapNotNull { it.value }

    // Derived from [observeUpcoming] over the SAME window rather than issuing a second identical
    // `dao.observeUpcoming(window)` query. The two were literally the same read — one returning the
    // rows, one summing them — so they ran as two concurrent collectors on one table whose totals
    // could momentarily disagree with the list rendered beside them.
    override fun observeTotalUpcomingAmount(maxDays: Int): Flow<Double> =
        observeUpcoming(maxDays).map { rows -> rows.sumOf { it.amount } }

    // Repository-internal keyed detail store — one reminder as a ScreenDataStream (absent id → Empty).
    private val detailStore = provideBillReminderDetailStore(billReminderDao)

    override fun billReminderDetailStream(id: String, scope: CoroutineScope): ScreenDataStream<BillReminder> =
        detailStore.asScreenStream(
            key = id,
            cacheKey = AppCacheKeys.billReminder(id),
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
        )

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
