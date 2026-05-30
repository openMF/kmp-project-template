// File: samples/kmp-project-template/sync/src/commonMain/kotlin/org/mifos/sync/NotificationWorker.kt
package org.mifos.sync

import io.github.mobilebytelabs.worker.CoroutineWorker
import io.github.mobilebytelabs.worker.ForegroundInfo
import io.github.mobilebytelabs.worker.WorkResult
import io.github.mobilebytelabs.worker.WorkerContext

/** Reads NotificationContent from inputData and delegates to expect/actual renderNotification. */
class NotificationWorker(ctx: WorkerContext) : CoroutineWorker(ctx) {

    override suspend fun doWork(): WorkResult {
        val title = inputData.getString("title") ?: return WorkResult.failure("missing title")
        val body = inputData.getString("body") ?: return WorkResult.failure("missing body")
        val channelId = inputData.getString("channelId")?.takeIf { it.isNotEmpty() }
        val content = NotificationContent(title, body, channelId)
        return try {
            renderNotification(content)
            WorkResult.success()
        } catch (t: Throwable) {
            WorkResult.failure(t.message)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val title = inputData.getString("title").orEmpty()
        return ForegroundInfo(
            notificationId = FOREGROUND_NOTIFICATION_ID_NOTIFICATION,
            title = title,
            body = "Preparing notification…",
        )
    }
}
