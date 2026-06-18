/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.banking

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kpt.core.data.banking.impl.BillReminderRepositoryImpl
import kpt.core.model.banking.BillCategory
import kpt.core.model.banking.BillReminder
import kpt.core.model.banking.Recurrence
import kpt.core.store.banking.impl.provideBillRemindersStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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
            billReminderDao = dao,
            clock = fixedClock,
            timeZone = timeZone,
        )

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
    fun upsertThenObserveAllRoundTripsTheDomainObject() = runTest {
        val bill = sampleBill(id = "B1", dueDay = 15)
        repo.upsert(bill)
        assertEquals(listOf(bill), repo.observeAll().first())
    }

    @Test
    fun observeAllSortsByDueDayAscending() = runTest {
        repo.upsert(sampleBill(id = "late", dueDay = 28))
        repo.upsert(sampleBill(id = "early", dueDay = 1))
        repo.upsert(sampleBill(id = "mid", dueDay = 15))

        val ids = repo.observeAll().first().map { it.id }
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
    fun observeByIdEmitsNullForUnknown() = runTest {
        assertNull(repo.observeById("missing").first())
    }

    @Test
    fun deleteRemovesRow() = runTest {
        val bill = sampleBill(id = "B1")
        repo.upsert(bill)
        assertEquals(bill, repo.getById("B1"))
        repo.delete("B1")
        assertNull(repo.getById("B1"))
    }

    @Test
    fun observeCountReflectsInsertsAndDeletes() = runTest {
        assertEquals(0, repo.observeCount().first())
        repo.upsert(sampleBill(id = "B1"))
        assertEquals(1, repo.observeCount().first())
        repo.upsert(sampleBill(id = "B2"))
        assertEquals(2, repo.observeCount().first())
        repo.delete("B1")
        assertEquals(1, repo.observeCount().first())
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
