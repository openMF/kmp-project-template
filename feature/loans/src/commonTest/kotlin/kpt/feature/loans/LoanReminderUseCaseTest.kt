/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package kpt.feature.loans

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kpt.core.model.banking.Loan
import kpt.core.model.banking.LoanKind
import kpt.sync.NotificationContent
import kpt.sync.WorkHandle
import kpt.sync.WorkMode
import kpt.sync.WorkScheduler
import kpt.sync.WorkStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * `LoanReminderUseCase` is the cross-module regression test for the whole
 * `sync/` library design: a consumer `feature/loans` class that touches ONLY the
 * public `kpt.sync.WorkScheduler` façade (no worker-kmp / DefaultWorkScheduler
 * internals). If the library's API shape regresses, this test breaks.
 */
class LoanReminderUseCaseTest {

    /** Records the scheduler calls so we can assert the façade contract. */
    private class RecordingScheduler : WorkScheduler {
        val notifications = mutableListOf<Pair<NotificationContent, Duration>>()
        var dataSyncCount = 0
        val fixedId: Uuid = Uuid.random()

        override suspend fun enqueueDataSync(mode: WorkMode): WorkHandle {
            dataSyncCount++
            return WorkHandle(id = fixedId, uniqueName = "kpt.sync.DataSyncWorker")
        }

        override suspend fun scheduleNotification(
            content: NotificationContent,
            delay: Duration,
            mode: WorkMode,
        ): WorkHandle {
            notifications += content to delay
            return WorkHandle(id = fixedId, uniqueName = null)
        }

        override fun observeWork(handle: WorkHandle): Flow<WorkStatus> = flowOf(WorkStatus.Pending)
        override suspend fun cancelWork(handle: WorkHandle) = Unit
    }

    private fun loan(dueDate: LocalDate) = Loan(
        id = "loan-1",
        name = "Car loan",
        kind = LoanKind.AUTO,
        principal = 10_000.0,
        principalRemaining = 8_000.0,
        annualRatePercent = 7.5,
        tenureMonths = 60,
        monthsRemaining = 40,
        monthlyPayment = 199.99,
        nextDueDate = dueDate,
        totalPaid = 2_000.0,
        createdAtMs = 0L,
        updatedAtMs = 0L,
    )

    private val zone = TimeZone.UTC
    // Fixed "now" = 2026-01-01T00:00Z so delay math is deterministic.
    private val fixedNow: Instant = LocalDate(2026, 1, 1).atStartOfDayIn(zone)

    private fun useCase(scheduler: WorkScheduler) =
        LoanReminderUseCase(scheduler = scheduler, now = { fixedNow }, timeZone = zone)

    @Test
    fun scheduleDueDateReminder_schedules_a_foreground_notification_one_day_before_due() = runTest {
        val scheduler = RecordingScheduler()

        // Due 2026-01-10 → reminder fires 1 day before → delay ≈ 8 days from fixedNow.
        val handle = useCase(scheduler).scheduleDueDateReminder(loan(LocalDate(2026, 1, 10)))

        assertTrue(handle != null, "a future due-date must produce a handle")
        assertEquals(1, scheduler.notifications.size)
        val (content, delay) = scheduler.notifications.single()
        assertTrue("Car loan" in content.body, "notification body references the loan")
        assertEquals(8L, delay.inWholeDays, "reminder is scheduled 1 day before the 9-days-out due date")
    }

    @Test
    fun scheduleDueDateReminder_returns_null_when_the_due_date_is_within_the_lead_window() = runTest {
        val scheduler = RecordingScheduler()

        // Due today → 1-day lead window already passed → no reminder.
        val handle = useCase(scheduler).scheduleDueDateReminder(loan(LocalDate(2026, 1, 1)))

        assertNull(handle, "a due date inside the 1-day lead window schedules nothing")
        assertTrue(scheduler.notifications.isEmpty())
    }

    @Test
    fun refreshLoansData_kicks_a_background_data_sync() = runTest {
        val scheduler = RecordingScheduler()

        val handle = useCase(scheduler).refreshLoansData()

        assertEquals(1, scheduler.dataSyncCount, "pull-to-refresh enqueues exactly one data sync")
        assertEquals("kpt.sync.DataSyncWorker", handle.uniqueName)
    }
}
