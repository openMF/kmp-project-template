// File: samples/kmp-project-template/sync/src/commonMain/kotlin/org/mifos/sync/WorkScheduler.kt
package org.mifos.sync

import io.github.mobilebytelabs.worker.workDataOf
import io.github.mobilebytelabs.worker.WorkData
import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

/** Foreground = setExpedited + persistent notification; Background = default WorkManager scheduling. */
enum class WorkMode { Foreground, Background }

data class NotificationContent(
    val title: String,
    val body: String,
    val channelId: String? = null,
)

@OptIn(ExperimentalUuidApi::class)
data class WorkHandle(
    val id: Uuid,
    val uniqueName: String? = null,
) {
    constructor(uniqueName: String?) : this(id = Uuid.random(), uniqueName = uniqueName)
}

enum class WorkStatus { Pending, Running, Succeeded, Failed, Cancelled }

/**
 * Koin-injectable façade over worker-kmp's WorkManager. Any commonMain module can
 * inject `WorkScheduler` and schedule work without touching WorkManager directly.
 *
 * Usage:
 *   class MyUseCase(private val scheduler: WorkScheduler) {
 *       fun doIt() {
 *           scheduler.scheduleNotification(NotificationContent("Hi", "Hello"))
 *           scheduler.enqueueDataSync(mode = WorkMode.Background)
 *       }
 *   }
 */
interface WorkScheduler {
    /** Existing — one-time, immediate (or expedited if WorkMode.Foreground). */
    fun enqueueDataSync(
        mode: WorkMode = WorkMode.Background,
        payload: WorkData = workDataOf(),
    ): WorkHandle

    /** Existing — one-time notification after [delay]. */
    fun scheduleNotification(
        content: NotificationContent,
        delay: Duration = Duration.ZERO,
        mode: WorkMode = WorkMode.Background,
    ): WorkHandle

    /**
     * NEW (D22) — Periodic. Daily at [timeOfDay] in [timeZone].
     * Uses PeriodicWorkRequest + setInitialDelay(nextOccurrence - now) + setRepeatInterval(24h),
     * enqueued via enqueueUniquePeriodicWork(DAILY_SYNC_WORK_NAME, KEEP, ...).
     * Flex window ~1-15min (WorkManager-managed; battery-friendly).
     */
    fun scheduleDailyDataSync(
        timeOfDay: LocalTime,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
        payload: WorkData = workDataOf(),
    ): WorkHandle

    /**
     * NEW (D22) — Periodic. Repeats every [interval] starting after [initialDelay].
     * Minimum [interval] is 15min per WorkManager rules; smaller values clamp to 15min.
     */
    fun schedulePeriodicDataSync(
        interval: Duration,
        initialDelay: Duration = Duration.ZERO,
        payload: WorkData = workDataOf(),
    ): WorkHandle

    /**
     * NEW (D22) — One-time at [instant]. Uses setInitialDelay(instant - now);
     * subject to WorkManager flex window. Cross-platform.
     */
    fun scheduleDataSyncAt(
        instant: Instant,
        mode: WorkMode = WorkMode.Background,
        payload: WorkData = workDataOf(),
    ): WorkHandle

    /**
     * NEW (D22, opt-in exact tier) — One-time at exact [instant].
     * Android: AlarmManager.setExactAndAllowWhileIdle (needs SCHEDULE_EXACT_ALARM permission).
     * iOS/Desktop/Web: falls back to scheduleDataSyncAt + logs the fallback.
     */
    fun scheduleDataSyncAtExact(
        instant: Instant,
        mode: WorkMode = WorkMode.Background,
        payload: WorkData = workDataOf(),
    ): WorkHandle

    /**
     * NEW (D22) — One-time notification at exact [instant].
     * Same exact-tier rules as scheduleDataSyncAtExact.
     */
    fun scheduleNotificationAt(
        instant: Instant,
        content: NotificationContent,
        mode: WorkMode = WorkMode.Background,
    ): WorkHandle

    fun observeWork(name: String): Flow<WorkStatus>
    fun cancelWork(name: String)
}

const val DAILY_SYNC_WORK_NAME = "data-sync-daily"
const val PERIODIC_SYNC_WORK_NAME = "data-sync-periodic"
