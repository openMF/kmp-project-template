// File: samples/kmp-project-template/sync/src/androidMain/kotlin/org/mifos/sync/ExactAlarmScheduler.kt
package org.mifos.sync

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import io.github.mobilebytelabs.worker.WorkData
import io.github.mobilebytelabs.worker.WorkManager
import io.github.aakira.napier.Napier
import kotlinx.datetime.Instant
import org.koin.mp.KoinPlatform
import org.mifos.core.datastore.SyncStatePersister
import kotlin.uuid.ExperimentalUuidApi

/**
 * Android actual that overrides scheduleDataSyncAtExact to use
 * AlarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, ...) for battery-respecting exact alarms.
 * Requires SCHEDULE_EXACT_ALARM permission (declared in AndroidManifest.xml).
 */
@OptIn(ExperimentalUuidApi::class)
actual class DefaultWorkScheduler actual constructor(
    workManager: WorkManager,
    persister: SyncStatePersister,
) : CommonDefaultWorkScheduler(workManager, persister) {

    override fun scheduleDataSyncAtExact(
        instant: Instant,
        mode: WorkMode,
        payload: WorkData,
    ): WorkHandle {
        val context: Context = KoinPlatform.getKoin().get()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildDataSyncPendingIntent(context, instant, payload)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Napier.w("SCHEDULE_EXACT_ALARM permission not granted; falling back to flex-window delay")
            super.scheduleDataSyncAt(instant, mode, payload)
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                instant.toEpochMilliseconds(),
                pendingIntent,
            )
            WorkHandle(uniqueName = "data-sync-exact-${instant.toEpochMilliseconds()}")
        }
    }

    private fun buildDataSyncPendingIntent(
        context: Context,
        instant: Instant,
        payload: WorkData,
    ): PendingIntent {
        val intent = Intent(context, DataSyncAlarmReceiver::class.java).apply {
            putExtra("trigger_ms", instant.toEpochMilliseconds())
        }
        return PendingIntent.getBroadcast(
            context,
            instant.toEpochMilliseconds().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
