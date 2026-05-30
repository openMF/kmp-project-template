// File: samples/kmp-project-template/sync/src/androidMain/kotlin/org/mifos/sync/NotificationRender.kt
package org.mifos.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.koin.mp.KoinPlatform

private const val DEFAULT_CHANNEL_ID = "worker_kmp_sync"

actual fun renderNotification(content: NotificationContent) {
    val context: Context = KoinPlatform.getKoin().get()
    ensureChannel(context, content.channelId ?: DEFAULT_CHANNEL_ID)
    val notif = NotificationCompat.Builder(context, content.channelId ?: DEFAULT_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_notify_sync)
        .setContentTitle(content.title)
        .setContentText(content.body)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
    NotificationManagerCompat.from(context).notify(content.title.hashCode(), notif)
}

private fun ensureChannel(context: Context, channelId: String) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(channelId) == null) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "Worker-KMP sync", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
    }
}
