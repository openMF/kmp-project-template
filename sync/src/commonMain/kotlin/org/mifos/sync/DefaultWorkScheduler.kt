// File: samples/kmp-project-template/sync/src/commonMain/kotlin/org/mifos/sync/DefaultWorkScheduler.kt
package org.mifos.sync

import io.github.mobilebytelabs.worker.ExistingPeriodicWorkPolicy
import io.github.mobilebytelabs.worker.ExistingWorkPolicy
import io.github.mobilebytelabs.worker.OutOfQuotaPolicy
import io.github.mobilebytelabs.worker.WorkData
import io.github.mobilebytelabs.worker.WorkInfo
import io.github.mobilebytelabs.worker.WorkManager
import io.github.mobilebytelabs.worker.oneTimeWorkRequest
import io.github.mobilebytelabs.worker.periodicWorkRequest
import io.github.mobilebytelabs.worker.workDataOf
import io.github.aakira.napier.Napier
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.mifos.core.datastore.SyncStatePersister
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class DefaultWorkScheduler(
    private val workManager: WorkManager,
    @Suppress("UNUSED_PARAMETER") private val persister: SyncStatePersister,
) : WorkScheduler {

    override fun enqueueDataSync(
        mode: WorkMode,
        payload: WorkData,
    ): WorkHandle {
        val request = oneTimeWorkRequest<DataSyncWorker> {
            setConstraints(SyncConstraints)
            setInputData(payload)
            if (mode == WorkMode.Foreground) {
                setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }
        }
        workManager.enqueueUniqueWork(
            uniqueWorkName = SYNC_WORK_NAME,
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = request,
        )
        return WorkHandle(id = request.id, uniqueName = SYNC_WORK_NAME)
    }

    override fun scheduleNotification(
        content: NotificationContent,
        delay: Duration,
        mode: WorkMode,
    ): WorkHandle {
        val uniqueName = "${NOTIFICATION_WORK_PREFIX}-${content.title.hashCode()}"
        val request = oneTimeWorkRequest<NotificationWorker> {
            setInputData(
                workDataOf(
                    "title" to content.title,
                    "body" to content.body,
                    "channelId" to (content.channelId ?: ""),
                ),
            )
            if (delay > Duration.ZERO) setInitialDelay(delay)
            if (mode == WorkMode.Foreground) {
                setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }
        }
        workManager.enqueueUniqueWork(
            uniqueWorkName = uniqueName,
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,  // newer notification wins
            request = request,
        )
        return WorkHandle(id = request.id, uniqueName = uniqueName)
    }

    override fun scheduleDailyDataSync(
        timeOfDay: LocalTime,
        timeZone: TimeZone,
        payload: WorkData,
    ): WorkHandle {
        val nowInstant = Clock.System.now()
        val nextOccurrence = nowInstant.nextOccurrenceOf(timeOfDay, timeZone)
        val initialDelay = nextOccurrence - nowInstant
        val request = periodicWorkRequest<DataSyncWorker>(repeatInterval = 24.hours) {
            setInitialDelay(initialDelay)
            setInputData(payload)
            setConstraints(SyncConstraints)
        }
        workManager.enqueueUniquePeriodicWork(DAILY_SYNC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        return WorkHandle(uniqueName = DAILY_SYNC_WORK_NAME)
    }

    override fun schedulePeriodicDataSync(
        interval: Duration,
        initialDelay: Duration,
        payload: WorkData,
    ): WorkHandle {
        val clampedInterval = maxOf(interval, 15.minutes)
        val request = periodicWorkRequest<DataSyncWorker>(repeatInterval = clampedInterval) {
            setInitialDelay(initialDelay)
            setInputData(payload)
            setConstraints(SyncConstraints)
        }
        workManager.enqueueUniquePeriodicWork(PERIODIC_SYNC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        return WorkHandle(uniqueName = PERIODIC_SYNC_WORK_NAME)
    }

    override fun scheduleDataSyncAt(
        instant: Instant,
        mode: WorkMode,
        payload: WorkData,
    ): WorkHandle {
        val delay = (instant - Clock.System.now()).coerceAtLeast(Duration.ZERO)
        val request = oneTimeWorkRequest<DataSyncWorker> {
            setInitialDelay(delay)
            setInputData(payload)
            setConstraints(SyncConstraints)
            if (mode == WorkMode.Foreground) setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        }
        val uniqueName = "data-sync-at-${instant.toEpochMilliseconds()}"
        workManager.enqueueUniqueWork(uniqueName, ExistingWorkPolicy.KEEP, request)
        return WorkHandle(uniqueName)
    }

    /**
     * Default impl in commonMain delegates to scheduleDataSyncAt (flex-window).
     * Android's actual class overrides this method to call AlarmManager.setExactAndAllowWhileIdle.
     */
    override fun scheduleDataSyncAtExact(
        instant: Instant,
        mode: WorkMode,
        payload: WorkData,
    ): WorkHandle = scheduleDataSyncAt(instant, mode, payload).also {
        Napier.w("scheduleDataSyncAtExact falling back to flex-window periodic delay on common path; Android impl overrides for exact alarm")
    }

    override fun scheduleNotificationAt(
        instant: Instant,
        content: NotificationContent,
        mode: WorkMode,
    ): WorkHandle {
        val delay = (instant - Clock.System.now()).coerceAtLeast(Duration.ZERO)
        return scheduleNotification(content = content, delay = delay, mode = mode)
    }

    override fun observeWork(name: String): Flow<WorkStatus> =
        workManager.getWorkInfosByUniqueWorkNameFlow(name).map { infos ->
            when (infos.firstOrNull()?.state) {
                WorkInfo.State.ENQUEUED -> WorkStatus.Pending
                WorkInfo.State.RUNNING -> WorkStatus.Running
                WorkInfo.State.SUCCEEDED -> WorkStatus.Succeeded
                WorkInfo.State.FAILED -> WorkStatus.Failed
                WorkInfo.State.CANCELLED -> WorkStatus.Cancelled
                WorkInfo.State.BLOCKED -> WorkStatus.Pending
                null -> WorkStatus.Pending
            }
        }

    override fun cancelWork(name: String) {
        workManager.cancelUniqueWork(name)
    }

    private fun Instant.nextOccurrenceOf(timeOfDay: LocalTime, tz: TimeZone): Instant {
        val today = toLocalDateTime(tz).date
        val candidate = LocalDateTime(today, timeOfDay).toInstant(tz)
        return if (candidate > this) candidate else candidate + 1.days
    }
}
