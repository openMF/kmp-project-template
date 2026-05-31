package org.mifos.feature.loans

import io.github.mobilebytelabs.worker.WorkManager
import io.github.mobilebytelabs.worker.oneTimeWorkRequest
import io.github.mobilebytelabs.worker.scheduler.WorkMode
import io.github.mobilebytelabs.worker.scheduler.WorkScheduler
import io.github.mobilebytelabs.worker.scheduler.enqueueDataSync
import io.github.mobilebytelabs.worker.scheduler.scheduleDailyDataSync
import io.github.mobilebytelabs.worker.scheduler.scheduleDataSyncAtExact
import io.github.mobilebytelabs.worker.workDataOf
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.mifos.sync.DataSyncWorker
import org.mifos.sync.NotificationContent
import org.mifos.sync.NotificationWorker

/**
 * Cross-module usage demo (D18 + D24, updated for worker-kmp v3.1.1-alpha01).
 *
 * Constructor-injects [WorkScheduler] (sync scheduling) AND raw [WorkManager]
 * (notification scheduling). The split mirrors the cmp-worker-scheduler library's
 * v3.1.1 posture: schedule sync via `WorkScheduler` (typed reified API); schedule
 * any non-sync worker (notifications, custom domain workers) via raw `WorkManager`
 * with `oneTimeWorkRequest<MyWorker> { ... }`.
 *
 * Calls:
 *   1. `scheduler.scheduleDailyDataSync<DataSyncWorker>(LocalTime(9, 0))` — refresh
 *      exchange rates + macro indicators every morning at 9 AM. Builder lambda
 *      adds a custom tag for observability.
 *   2. `scheduler.scheduleDataSyncAtExact<DataSyncWorker>(refreshAt)` — exact-alarm
 *      sync right before payment-due timestamp (AlarmManager on Android; flex
 *      window elsewhere).
 *   3. `workManager.enqueue(oneTimeWorkRequest<NotificationWorker> { ... })` —
 *      fire the reminder notification (sample's own [NotificationWorker] +
 *      [renderNotification] expect/actual).
 */
@OptIn(ExperimentalTime::class)
class LoanReminderUseCase(
    private val workScheduler: WorkScheduler,
    private val workManager: WorkManager,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {

    /** Set up the daily morning refresh — called once at app start. */
    fun installDailyRefresh(borrowerCountryCode: String = "US") {
        workScheduler.scheduleDailyDataSync<DataSyncWorker>(
            timeOfDay = LocalTime(hour = 9, minute = 0),
            timeZone = TimeZone.currentSystemDefault(),
            payload = workDataOf(
                "currency.base" to "USD",
                "macro.countries" to borrowerCountryCode,
            ),
        ) {
            // v3.1.1 builder lambda — observability tag for this scheduled call.
            addTag("loans-feature-daily-refresh")
        }
    }

    /** Schedule a loan-payment reminder for a specific loan + due instant. */
    fun scheduleReminder(loanId: String, dueAt: Instant, currencyBase: String = "USD") {
        // 1. Fire the user-visible reminder at dueAt — raw WorkManager enqueue
        //    of the sample's own NotificationWorker (library doesn't ship notification scheduling).
        val notifContent = NotificationContent(
            title = "Loan payment due",
            body = "Payment for loan #$loanId is due today.",
            channelId = "loan-reminders",
        )
        val delayMs = (dueAt - Clock.System.now()).inWholeMilliseconds.coerceAtLeast(0)
        val notifRequest = oneTimeWorkRequest<NotificationWorker> {
            setInputData(
                workDataOf(
                    "title" to notifContent.title,
                    "body" to notifContent.body,
                    "channelId" to (notifContent.channelId ?: ""),
                ),
            )
            setInitialDelay(delayMs.milliseconds)
            addTag("loan-reminder-$loanId")
        }
        scope.launch { workManager.enqueue(notifRequest) }

        // 2. Sync exchange rates 15 min BEFORE the due instant so the loan-details
        //    screen renders with fresh data when the user opens it.
        val refreshAt = dueAt.minus(15.minutes)
        workScheduler.scheduleDataSyncAtExact<DataSyncWorker>(
            instant = refreshAt,
            mode = WorkMode.Background,
            payload = workDataOf(
                "currency.base" to currencyBase,
                "loan.id" to loanId,
            ),
        )
    }

    /** Manual refresh button — pull-to-refresh from the loans list. */
    fun refreshNow() {
        workScheduler.enqueueDataSync<DataSyncWorker>(
            mode = WorkMode.Background,
            payload = workDataOf("currency.base" to "USD"),
        )
    }
}
