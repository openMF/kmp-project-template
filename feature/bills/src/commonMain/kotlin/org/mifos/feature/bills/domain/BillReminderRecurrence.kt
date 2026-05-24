/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.bills.domain

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.mifos.core.model.banking.BillReminder
import org.mifos.core.model.banking.Recurrence
import kotlin.time.Clock

/**
 * Pure-functional recurrence math for [BillReminder] — kept feature-local so the math
 * isn't paid for by every consumer of `core/model`.
 *
 * Two operations live here:
 *  - [nextDueDate] — compute the next concrete [LocalDate] a bill is "due", anchored at
 *    a reference `today`. Handles MONTHLY / QUARTERLY / ANNUALLY / ONCE plus the
 *    month-end-clamp policy for `dueDay > daysInMonth` (e.g. day 31 in February → Feb 28/29).
 *  - [reminderTriggerMs] — convert the next-due [LocalDate] back to an epoch-millis
 *    instant `reminderDaysBefore` days earlier, snapped to local 9:00 AM in the supplied
 *    [TimeZone].
 *
 * Tests pin both behaviours; the schedule produced here flows into
 * [template.core.base.platform.notification.BillReminderScheduler.schedule].
 */
internal object BillReminderRecurrence {

    /** Local-time-of-day at which the reminder fires (09:00 by default). */
    private const val REMINDER_HOUR: Int = 9

    /**
     * Returns the next concrete `LocalDate` on which [bill] is "due", starting from [today].
     *
     * - `ONCE`: returns the first occurrence at-or-after [today], or `null` if it has already
     *   passed (single-shot bills don't recur).
     * - `MONTHLY` / `QUARTERLY` / `ANNUALLY`: returns the first occurrence in the recurrence
     *   sequence whose date is at-or-after [today]. The sequence is anchored by `dueDay`
     *   (and, for ANNUALLY, by the month of [today] at creation time — but we use
     *   [today].month as the reference here for determinism; production bills track an
     *   anchor month via the upcoming `dueMonth` extension. See KDoc note below.)
     * - Month-end clamp: if [BillReminder.dueDay] exceeds the days-in-month of the target
     *   month, the date clamps to the month's last day.
     */
    fun nextDueDate(bill: BillReminder, today: LocalDate): LocalDate? {
        val anchorMonth = today.month
        val anchorYear = today.year
        return when (bill.recurrence) {
            Recurrence.ONCE -> {
                // First viable date >= today using current month/year as the anchor.
                val candidate = clampedDate(anchorYear, anchorMonth.number, bill.dueDay)
                if (candidate >= today) candidate else null
            }
            Recurrence.MONTHLY -> nextOccurrence(today, bill.dueDay, stepMonths = 1)
            Recurrence.QUARTERLY -> nextOccurrence(today, bill.dueDay, stepMonths = 3)
            Recurrence.ANNUALLY -> nextOccurrence(today, bill.dueDay, stepMonths = 12)
        }
    }

    /**
     * Step forward `stepMonths` at a time from [today] until we land on a date >= [today]
     * whose day-of-month is [dueDay] (clamped to month-end where necessary).
     *
     * For MONTHLY (`stepMonths = 1`), this collapses to either "this month" or "next month".
     * For QUARTERLY / ANNUALLY, we walk forward by the cadence.
     */
    private fun nextOccurrence(today: LocalDate, dueDay: Int, stepMonths: Int): LocalDate {
        // Build the candidate for the current month/year; if not yet passed, return it.
        val sameMonth = clampedDate(today.year, today.month.number, dueDay)
        if (sameMonth >= today) return sameMonth
        // Otherwise step forward `stepMonths` months and re-clamp. Loop in case `stepMonths`
        // skips us past several months (e.g. QUARTERLY when today is in the same month as the
        // anchor — one quarter forward is enough; ANNUALLY similarly).
        var probe = today
        while (true) {
            probe = probe.plus(DatePeriod(months = stepMonths))
            val clamped = clampedDate(probe.year, probe.month.number, dueDay)
            if (clamped >= today) return clamped
        }
    }

    /** Clamp `dueDay` to the days-in-month for `[year, month]`. */
    private fun clampedDate(year: Int, month: Int, dueDay: Int): LocalDate {
        val lastDay = daysInMonth(year, month)
        val day = if (dueDay > lastDay) lastDay else dueDay.coerceAtLeast(1)
        return LocalDate(year, month, day)
    }

    /**
     * Days-in-month for a (year, 1..12) pair. Pure — no platform calendar APIs.
     * Encodes leap-year logic for February so the math is deterministic across targets.
     */
    fun daysInMonth(year: Int, month: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> error("Invalid month: $month")
    }

    private fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0)

    /**
     * Convert the next-due [LocalDate] into the epoch-millis instant at which the OS
     * notification should fire — `reminderDaysBefore` days earlier, snapped to local
     * [REMINDER_HOUR] AM in [tz].
     *
     * @return `null` when the resulting instant is already in the past relative to [clock].
     */
    fun reminderTriggerMs(
        nextDue: LocalDate,
        reminderDaysBefore: Int,
        tz: TimeZone,
        clock: Clock = Clock.System,
    ): Long? {
        val reminderDate = nextDue.plus(DatePeriod(days = -reminderDaysBefore))
        val triggerDateTime = LocalDateTime(
            year = reminderDate.year,
            month = reminderDate.month,
            day = reminderDate.day,
            hour = REMINDER_HOUR,
            minute = 0,
            second = 0,
        )
        val instant = triggerDateTime.toInstant(tz)
        val nowMs = clock.now().toEpochMilliseconds()
        val triggerMs = instant.toEpochMilliseconds()
        return if (triggerMs > nowMs) triggerMs else null
    }
}
