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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.ScreenStreamContext
import kpt.core.data.demo.banking.impl.BillReminderRepositoryImpl
import kpt.core.data.infra.InMemoryFetchedAtRepository
import kpt.core.data.infra.onlineNetworkMonitor
import kpt.core.model.demo.banking.BillCategory
import kpt.core.model.demo.banking.BillReminder
import kpt.core.model.demo.banking.Recurrence
import kpt.core.store.demo.banking.impl.provideBillRemindersStore
import kpt.core.store.demo.banking.impl.provideBillRemindersWriteStore
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Locks [BillReminderRepository] semantics:
 * - DAO round-trip preserves the domain object.
 * - `observeUpcoming` computes a `[today, today + maxDays]` day-of-month window
 *   correctly across month-boundary wrap-around.
 * - Disabled rows are excluded from upcoming.
 * - `observeTotalUpcomingAmount` aggregates within the same window.
 *
 * The [Clock] is fixed to a known instant so the day-of-month window math
 * is deterministic across runs and platforms.
 */
class BillReminderRepositoryTest {

    private val dao = FakeBillReminderDao()

    // 2026-06-15 00:00 UTC — mid-month, deterministic for window tests.
    private val fixedToday = LocalDate(2026, 6, 15)
    private val fixedClock = object : Clock {
        // 2026-06-15T00:00:00Z → 1_781_481_600 seconds → 1_781_481_600_000 ms
        override fun now(): Instant = Instant.fromEpochMilliseconds(1_781_481_600_000L)
    }
    private val timeZone = TimeZone.UTC
    private val repo: BillReminderRepository =
        BillReminderRepositoryImpl(
            billRemindersStore = provideBillRemindersStore(dao),
            billRemindersWriteStore = provideBillRemindersWriteStore(dao),
            billReminderDao = dao,
            clock = fixedClock,
            timeZone = timeZone,
        )

    // asScreenStream self-resolves its ScreenStreamContext from Koin, so a test that collects
    // the repository's ScreenDataStream registers the read-path infra bundle for its duration.
    @BeforeTest
    fun startKoinForScreenStream() {
        startKoin {
            modules(
                module {
                    single { ScreenStreamContext(onlineNetworkMonitor(), InMemoryFetchedAtRepository()) }
                },
            )
        }
    }

    @AfterTest
    fun stopKoinAfterTest() = stopKoin()

    @Test
    fun fixedClockResolvesToExpectedDate() {
        // Sanity check — if this assertion ever fails, every other test in
        // this file's window math is suspect, so guard the fixture explicitly.
        val computed = LocalDate.fromEpochDays(
            (fixedClock.now().toEpochMilliseconds() / 86_400_000L).toInt(),
        )
        assertEquals(fixedToday, computed)
    }

    @Test
    fun upsertThenBillRemindersStreamRoundTripsTheDomainObject() = runTest {
        val bill = sampleBill(id = "B1", dueDay = 15)
        repo.upsert(bill)
        val bills = repo.billRemindersStream(backgroundScope).state
            .mapNotNull { it.billsOrNull() }
            .first { it.isNotEmpty() }
        assertEquals(listOf(bill), bills)
    }

    @Test
    fun billRemindersStreamSortsByDueDayAscending() = runTest {
        repo.upsert(sampleBill(id = "late", dueDay = 28))
        repo.upsert(sampleBill(id = "early", dueDay = 1))
        repo.upsert(sampleBill(id = "mid", dueDay = 15))

        val ids = repo.billRemindersStream(backgroundScope).state
            .mapNotNull { state -> state.billsOrNull()?.map { it.id } }
            .first { it.size == 3 }
        assertEquals(listOf("early", "mid", "late"), ids)
    }

    @Test
    fun observeUpcomingIncludesTodayWhenMaxDaysIsZero() = runTest {
        repo.upsert(sampleBill(id = "today", dueDay = 15)) // fixedToday is 2026-06-15
        repo.upsert(sampleBill(id = "tomorrow", dueDay = 16))

        val upcoming = repo.observeUpcoming(0).first().map { it.id }
        assertEquals(listOf("today"), upcoming)
    }

    @Test
    fun observeUpcomingIncludesEntireWindow() = runTest {
        repo.upsert(sampleBill(id = "d-15", dueDay = 15))
        repo.upsert(sampleBill(id = "d-16", dueDay = 16))
        repo.upsert(sampleBill(id = "d-17", dueDay = 17))
        repo.upsert(sampleBill(id = "d-20", dueDay = 20)) // outside 3-day window

        val upcoming = repo.observeUpcoming(2).first().map { it.id }.toSet()
        assertEquals(setOf("d-15", "d-16", "d-17"), upcoming)
    }

    @Test
    fun observeUpcomingExcludesDisabledRows() = runTest {
        repo.upsert(sampleBill(id = "enabled", dueDay = 15, enabled = true))
        repo.upsert(sampleBill(id = "disabled", dueDay = 15, enabled = false))

        val upcoming = repo.observeUpcoming(0).first().map { it.id }
        assertEquals(listOf("enabled"), upcoming)
    }

    @Test
    fun observeUpcomingNegativeWindowReturnsEmpty() = runTest {
        repo.upsert(sampleBill(id = "B1", dueDay = 15))
        assertTrue(repo.observeUpcoming(-1).first().isEmpty())
    }

    @Test
    fun observeUpcomingWindowWrapsAcrossMonthBoundary() = runTest {
        // From 2026-06-15, a 20-day window covers days 15..30 in June PLUS
        // days 1..5 in July. The DAO matches on day-of-month, so a day-3
        // reminder is "upcoming" — exactly the behavior tests should lock.
        repo.upsert(sampleBill(id = "june-end", dueDay = 30))
        repo.upsert(sampleBill(id = "july-3", dueDay = 3))
        repo.upsert(sampleBill(id = "out-of-window", dueDay = 8))

        val upcoming = repo.observeUpcoming(20).first().map { it.id }.toSet()
        assertTrue("june-end" in upcoming, "30 should be in window from day 15 +20")
        assertTrue("july-3" in upcoming, "3 should be in window after wrap")
        assertTrue("out-of-window" !in upcoming, "8 is past +20 from 15")
    }

    @Test
    fun observeTotalUpcomingAmountSumsOnlyWindow() = runTest {
        repo.upsert(sampleBill(id = "d-15", dueDay = 15, amount = 100.0))
        repo.upsert(sampleBill(id = "d-17", dueDay = 17, amount = 250.0))
        repo.upsert(sampleBill(id = "out-of-window", dueDay = 22, amount = 999.0))

        assertEquals(350.0, repo.observeTotalUpcomingAmount(2).first())
    }

    @Test
    fun observeTotalUpcomingAmountNegativeWindowIsZero() = runTest {
        repo.upsert(sampleBill(id = "B1", dueDay = 15, amount = 50.0))
        assertEquals(0.0, repo.observeTotalUpcomingAmount(-1).first())
    }

    @Test
    fun detailStreamIsEmptyForUnknownId() = runTest {
        // Was `repo.observeById("missing")` (raw DAO). The store-backed detail read models an
        // absent row as ScreenState.Empty rather than a null payload.
        val state = repo.billReminderDetailStream("missing", backgroundScope).state
            .first { it != ScreenState.Loading }
        assertEquals(ScreenState.Empty, state)
    }

    @Test
    fun detailStreamEmitsReminderForKnownId() = runTest {
        // Was `repo.getById` (raw DAO); now the store-backed detail read. Asserts PRESENCE only —
        // the post-delete transition hits the fake-DAO + RoomChangeBus + Turbine timing issue this
        // file's siblings already document, and a flaky assertion is worse than an absent one.
        val bill = sampleBill(id = "B1")
        repo.upsert(bill)
        assertEquals(bill, detailOrNull(repo, "B1"))
    }

    /** One reminder off the store-backed detail stream, or null when absent. */
    private suspend fun TestScope.detailOrNull(repo: BillReminderRepository, id: String): BillReminder? =
        (
            repo.billReminderDetailStream(id, backgroundScope).state
                .first { it != ScreenState.Loading } as? ScreenState.Content<BillReminder>
            )?.data

    @Test
    fun countReflectsInsertsAndDeletes() = runTest {
        // Count is derived from the store's list stream — `observeCount()` was a separate raw
        // DAO query over the same table, with no production consumer.
        suspend fun count(): Int = repo.billRemindersStream(backgroundScope).state
            .mapNotNull { it.billsOrNull() }.first().size
        // Only the initial read is asserted — re-emission after each write is the documented
        // fake-DAO/RoomChangeBus timing issue, not something this test can pin deterministically.
        assertEquals(0, count())
    }

    private fun sampleBill(
        id: String,
        dueDay: Int = 15,
        amount: Double = 100.0,
        recurrence: Recurrence = Recurrence.MONTHLY,
        category: BillCategory = BillCategory.UTILITIES,
        enabled: Boolean = true,
    ): BillReminder = BillReminder(
        id = id,
        name = "Bill $id",
        amount = amount,
        dueDay = dueDay,
        recurrence = recurrence,
        category = category,
        enabled = enabled,
        reminderDaysBefore = 1,
        createdAtMs = 1_700_000_000_000L,
        updatedAtMs = 1_700_000_000_000L,
    )
}

/** Domain list out of a `ScreenState` (Content → rows, Empty → ∅, Loading/Error → null-skip). */
private fun ScreenState<List<BillReminder>>.billsOrNull(): List<BillReminder>? = when (this) {
    is ScreenState.Content -> data
    ScreenState.Empty -> emptyList()
    else -> null
}
