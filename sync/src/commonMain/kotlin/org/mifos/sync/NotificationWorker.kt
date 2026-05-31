package org.mifos.sync

import io.github.mobilebytelabs.worker.CoroutineWorker
import io.github.mobilebytelabs.worker.WorkResult
import io.github.mobilebytelabs.worker.WorkerContext

/**
 * Sample-side worker: reads NotificationContent from inputData and delegates to the
 * expect/actual `renderNotification`. Scheduled via raw
 * `workManager.enqueue(oneTimeWorkRequest<NotificationWorker> { setInputData(...); setInitialDelay(...) })`
 * — see [org.mifos.feature.loans.LoanReminderUseCase] for the canonical use pattern.
 */
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
            WorkResult.failure(t.message ?: t::class.simpleName ?: "render failed")
        }
    }
}
