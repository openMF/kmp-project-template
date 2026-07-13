/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.bills.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kpt.core.model.demo.banking.BillCategory
import kpt.core.model.demo.banking.BillReminder
import kpt.core.model.demo.banking.Recurrence
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Pins [BillReminderRecurrence] semantics. These are the rules a real user will hit:
 *  - "Next occurrence" steps to the next month/quarter/year correctly.
 *  - Day-31 bills in February clamp to the last day of February (28 / 29 in leap years).
 *  - `ONCE` bills don't recur — past instants return `null`.
 *  - `reminderTriggerMs` snaps to 09:00 local and lies in the future.
 */
class BillReminderRecurrenceTest {

    @Test
    fun monthlyNextDueIsTodayWhenDueDayMatches() {
        val bill = bill(dueDay = 15, recurrence = Recurrence.MONTHLY)
        val today = LocalDate(2026, 6, 15)
        assertEquals(LocalDate(2026, 6, 15), BillReminderRecurrence.nextDueDate(bill, today))
    }

    @Test
    fun monthlyNextDueIsThisMonthWhenDueDayStillUpcoming() {
        val bill = bill(dueDay = 20, recurrence = Recurrence.MONTHLY)
        val today = LocalDate(2026, 6, 15)
        assertEquals(LocalDate(2026, 6, 20), BillReminderRecurrence.nextDueDate(bill, today))
    }

    @Test
    fun monthlyNextDueRollsToNextMonthWhenDueDayPassed() {
        val bill = bill(dueDay = 10, recurrence = Recurrence.MONTHLY)
        val today = LocalDate(2026, 6, 15)
        assertEquals(LocalDate(2026, 7, 10), BillReminderRecurrence.nextDueDate(bill, today))
    }

    @Test
    fun monthlyDay31ClampsToFebruary28InNonLeapYear() {
        // 2026 is NOT a leap year (2024 was; 2028 next).
        val bill = bill(dueDay = 31, recurrence = Recurrence.MONTHLY)
        val today = LocalDate(2026, 1, 31)
        // After Jan 31, next MONTHLY occurrence should be Feb 28 (clamped).
        val nextAfterJan = BillReminderRecurrence.nextDueDate(bill, LocalDate(2026, 2, 1))
        assertEquals(LocalDate(2026, 2, 28), nextAfterJan)
        // From today=Jan 31 itself, the same-month candidate wins.
        assertEquals(today, BillReminderRecurrence.nextDueDate(bill, today))
    }

    @Test
    fun monthlyDay31ClampsToFebruary29InLeapYear() {
        // 2024 is a leap year.
        val bill = bill(dueDay = 31, recurrence = Recurrence.MONTHLY)
        val nextAfterJan = BillReminderRecurrence.nextDueDate(bill, LocalDate(2024, 2, 1))
        assertEquals(LocalDate(2024, 2, 29), nextAfterJan)
    }

    @Test
    fun quarterlyStepsByThreeMonths() {
        val bill = bill(dueDay = 5, recurrence = Recurrence.QUARTERLY)
        // Today is past Jan 5 → next quarterly should be April 5.
        val next = BillReminderRecurrence.nextDueDate(bill, LocalDate(2026, 1, 20))
        assertEquals(LocalDate(2026, 4, 5), next)
    }

    @Test
    fun annuallyStepsBy12Months() {
        // ANNUALLY with the current `BillReminder` model (which only stores `dueDay`, no
        // anchor month) behaves like a once-per-month-but-with-12-month-cadence reminder:
        //  - When today's day-of-month is still ≤ dueDay, the next occurrence is in the
        //    same month/year (no step needed).
        //  - When today's day-of-month is past dueDay, we walk forward 12 months.
        // Below: today is Aug 16, due-day is 15 → same-month is already past → +12 months
        // → Aug 15 2027.
        val bill = bill(dueDay = 15, recurrence = Recurrence.ANNUALLY)
        val next = BillReminderRecurrence.nextDueDate(bill, LocalDate(2026, 8, 16))
        assertEquals(LocalDate(2027, 8, 15), next)
    }

    @Test
    fun onceReturnsNullWhenAlreadyPassedInTheMonth() {
        val bill = bill(dueDay = 5, recurrence = Recurrence.ONCE)
        val next = BillReminderRecurrence.nextDueDate(bill, LocalDate(2026, 6, 10))
        assertNull(next)
    }

    @Test
    fun onceReturnsTheSingletonOccurrenceWhenStillUpcoming() {
        val bill = bill(dueDay = 20, recurrence = Recurrence.ONCE)
        val next = BillReminderRecurrence.nextDueDate(bill, LocalDate(2026, 6, 10))
        assertEquals(LocalDate(2026, 6, 20), next)
    }

    @Test
    fun daysInMonthHandlesEveryMonth() {
        assertEquals(31, BillReminderRecurrence.daysInMonth(2026, 1))
        assertEquals(28, BillReminderRecurrence.daysInMonth(2026, 2))
        assertEquals(29, BillReminderRecurrence.daysInMonth(2024, 2))
        assertEquals(30, BillReminderRecurrence.daysInMonth(2026, 4))
        assertEquals(31, BillReminderRecurrence.daysInMonth(2026, 12))
    }

    @Test
    fun reminderTriggerSnapsToNineAmAndIsInFuture() {
        // "Now" = Jan 1 2026 00:00 UTC. Next due = Jan 5 2026. ReminderDaysBefore = 1 → fires Jan 4 09:00 UTC.
        // 2026-01-04T09:00:00Z = epoch 1767517200; 2026-01-01T00:00:00Z = epoch 1767225600.
        val nowMs = 1767225600_000L
        val expectedTrigger = 1767517200_000L
        val clock = object : Clock {
            override fun now(): Instant = Instant.fromEpochMilliseconds(nowMs)
        }
        val triggerMs = BillReminderRecurrence.reminderTriggerMs(
            nextDue = LocalDate(2026, 1, 5),
            reminderDaysBefore = 1,
            tz = TimeZone.UTC,
            clock = clock,
        )
        assertNotNull(triggerMs)
        assertEquals(expectedTrigger, triggerMs)
        assertTrue(triggerMs > nowMs)
    }

    @Test
    fun reminderTriggerReturnsNullForPastInstant() {
        // "Now" = far in the future relative to the calculated reminder.
        val clock = object : Clock {
            override fun now(): Instant = Instant.fromEpochMilliseconds(2_000_000_000_000L)
        }
        val triggerMs = BillReminderRecurrence.reminderTriggerMs(
            nextDue = LocalDate(2026, 1, 5),
            reminderDaysBefore = 1,
            tz = TimeZone.UTC,
            clock = clock,
        )
        assertNull(triggerMs)
    }

    private fun bill(
        id: String = "B1",
        dueDay: Int,
        recurrence: Recurrence,
        reminderDaysBefore: Int = 1,
    ): BillReminder = BillReminder(
        id = id,
        name = "Bill $id",
        amount = 100.0,
        dueDay = dueDay,
        recurrence = recurrence,
        category = BillCategory.UTILITIES,
        enabled = true,
        reminderDaysBefore = reminderDaysBefore,
        createdAtMs = 1_700_000_000_000L,
        updatedAtMs = 1_700_000_000_000L,
    )
}
