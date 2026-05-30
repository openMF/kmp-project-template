// File: samples/kmp-project-template/sync/src/commonTest/kotlin/org/mifos/sync/DefaultWorkSchedulerTest.kt
package org.mifos.sync

import io.github.mobilebytelabs.worker.ExistingPeriodicWorkPolicy
import io.github.mobilebytelabs.worker.workDataOf
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultWorkSchedulerTest {
    @Test fun enqueueDataSync_Background_producesNonExpeditedRequest() = runTest {
        val fakeWM = FakeWorkManager()
        val scheduler = DefaultWorkScheduler(fakeWM, FakeSyncStatePersister())
        val handle = scheduler.enqueueDataSync(mode = WorkMode.Background)
        assertEquals(false, fakeWM.lastEnqueued?.isExpedited)
        assertEquals(SYNC_WORK_NAME, handle.uniqueName)
    }

    @Test fun enqueueDataSync_Foreground_setsExpedited() = runTest {
        val fakeWM = FakeWorkManager()
        val scheduler = DefaultWorkScheduler(fakeWM, FakeSyncStatePersister())
        scheduler.enqueueDataSync(mode = WorkMode.Foreground)
        assertEquals(true, fakeWM.lastEnqueued?.isExpedited)
    }

    @Test fun scheduleNotification_setsInputData_andDelay() = runTest {
        val fakeWM = FakeWorkManager()
        val scheduler = DefaultWorkScheduler(fakeWM, FakeSyncStatePersister())
        scheduler.scheduleNotification(
            content = NotificationContent("Hello", "World"),
            delay = 5.seconds,
        )
        val req = fakeWM.lastEnqueued!!
        assertEquals(5.seconds.inWholeMilliseconds, req.initialDelayMs)
        assertEquals("Hello", req.inputData.getString("title"))
    }

    @Test fun scheduleDailyDataSync_at9AM_setsRepeatInterval24h_andInitialDelay() = runTest {
        val fakeWM = FakeWorkManager()
        val scheduler = DefaultWorkScheduler(fakeWM, FakeSyncStatePersister())
        val handle = scheduler.scheduleDailyDataSync(timeOfDay = LocalTime(9, 0))
        val req = fakeWM.lastEnqueuedPeriodic!!
        assertEquals(24.hours.inWholeMilliseconds, req.repeatIntervalMs)
        assertEquals(true, req.initialDelayMs > 0)
        assertEquals(DAILY_SYNC_WORK_NAME, handle.uniqueName)
    }

    @Test fun scheduleDailyDataSync_isIdempotent_underRecall() = runTest {
        val fakeWM = FakeWorkManager()
        val scheduler = DefaultWorkScheduler(fakeWM, FakeSyncStatePersister())
        scheduler.scheduleDailyDataSync(timeOfDay = LocalTime(9, 0))
        scheduler.scheduleDailyDataSync(timeOfDay = LocalTime(9, 0))
        assertEquals(2, fakeWM.enqueueUniqueCalls)
        assertEquals(ExistingPeriodicWorkPolicy.KEEP, fakeWM.lastEnqueuePolicy)
    }

    @Test fun enqueueDataSync_passesPayload_throughInputData() = runTest {
        val fakeWM = FakeWorkManager()
        val scheduler = DefaultWorkScheduler(fakeWM, FakeSyncStatePersister())
        scheduler.enqueueDataSync(
            mode = WorkMode.Background,
            payload = workDataOf("currency.base" to "EUR", "macro.countries" to "US,IN"),
        )
        assertEquals("EUR", fakeWM.lastEnqueued?.inputData?.getString("currency.base"))
        assertEquals("US,IN", fakeWM.lastEnqueued?.inputData?.getString("macro.countries"))
    }
}
