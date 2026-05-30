package org.mifos.feature.loans

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import io.github.mobilebytelabs.worker.workDataOf
import org.mifos.sync.WorkScheduler
import org.mifos.sync.NotificationContent
import org.mifos.sync.WorkMode

/**
 * Cross-module usage demo (D18 + D24).
 *
 * Constructor-injects [WorkScheduler] via Koin. The class lives in feature/loans/
 * (a feature module) and reaches into sync/ via the Koin-bound `WorkScheduler`
 * interface — pure commonMain, no platform conditionals.
 *
 * Calls 3 distinct WorkScheduler entry points:
 *   1. `scheduleDailyDataSync(LocalTime(9, 0))` — refresh exchange rates +
 *       macro indicators every morning at 9 AM (D22 periodic tier).
 *   2. `scheduleDataSyncAtExact(paymentDueInstant)` — exact-alarm sync right
 *       before the payment-due timestamp so the loan view shows fresh data
 *       (D22 opt-in exact tier; falls back to flex window on non-Android).
 *   3. `scheduleNotificationAt(reminderInstant, content)` — fire the actual
 *       reminder notification at the user's preferred hour.
 *
 * Each call passes a [WorkData] payload so DataSyncWorker / NotificationWorker
 * can read targeted keys (D23). The currency code "USD" + the borrower's
 * country code flow through `inputData` into the repos' 2-arg `syncWith`
 * overrides; the worker doesn't need to know about loan-domain semantics.
 */
class LoanReminderUseCase(
    private val workScheduler: WorkScheduler,
) {

    /** Set up the daily morning refresh — called once at app start. */
    fun installDailyRefresh(borrowerCountryCode: String = "US") {
        workScheduler.scheduleDailyDataSync(
            timeOfDay = LocalTime(hour = 9, minute = 0),
            timeZone = TimeZone.currentSystemDefault(),
            payload = workDataOf(
                "currency.base" to "USD",
                "macro.countries" to borrowerCountryCode,
            ),
        )
    }

    /** Schedule a loan-payment reminder for a specific loan + due instant. */
    fun scheduleReminder(loanId: String, dueAt: Instant, currencyBase: String = "USD") {
        // 1. Fire the user-visible reminder at exactly the due instant.
        workScheduler.scheduleNotificationAt(
            instant = dueAt,
            content = NotificationContent(
                title = "Loan payment due",
                body = "Payment for loan #$loanId is due today.",
                channelId = "loan-reminders",
            ),
            mode = WorkMode.Foreground,
        )

        // 2. Sync exchange rates RIGHT BEFORE the due instant so the
        //    loan-details screen renders with fresh data when the user opens it.
        val refreshAt = dueAt.minus(kotlin.time.Duration.parse("PT15M"))
        workScheduler.scheduleDataSyncAtExact(
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
        workScheduler.enqueueDataSync(
            mode = WorkMode.Background,
            payload = workDataOf("currency.base" to "USD"),
        )
    }
}
