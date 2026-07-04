/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loans

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kpt.core.model.banking.Loan
import kpt.sync.NotificationContent
import kpt.sync.WorkHandle
import kpt.sync.WorkMode
import kpt.sync.WorkScheduler
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Cross-module proof-of-concept demonstrating consumer use of [WorkScheduler]
 * (D18 of mifos-template-sync-integration).
 *
 * The `feature/loans` module has ZERO direct dep on `worker-kmp` / `sync/`'s
 * internals — it consumes only `kpt.sync.WorkScheduler` + the helper types
 * (`NotificationContent`, `WorkMode`, `WorkHandle`). The implementation is in
 * the `sync/` module's `DefaultWorkScheduler`.
 *
 * Two demo behaviours:
 *  1. **Due-date reminder** — schedules a foreground notification 1 day before
 *     the loan's `dueAt`. Demonstrates the scheduling + delay semantics.
 *  2. **Pull-to-refresh** — kicks the background data sync. Demonstrates the
 *     unique-named de-duplication (KEEP policy in
 *     [DefaultWorkScheduler.enqueueDataSync]).
 */
@OptIn(ExperimentalTime::class)
public class LoanReminderUseCase(
    private val scheduler: WorkScheduler,
    private val now: () -> Instant = { Clock.System.now() },
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {

    /**
     * Schedule a notification that fires 1 day before [Loan.nextDueDate]
     * (interpreted at start-of-day in the local time zone). Foreground mode
     * requests Android expedited execution (silent fallback on quota exhaustion).
     *
     * @return null if the due-date lands within the 1-day lead window or in the
     *   past; otherwise the [WorkHandle] for the scheduled notification.
     */
    public suspend fun scheduleDueDateReminder(loan: Loan): WorkHandle? {
        val dueInstant = loan.nextDueDate.atStartOfDayIn(timeZone)
        val delayFromNow = dueInstant - now() - 1.days
        if (delayFromNow.isNegative()) return null
        return scheduler.scheduleNotification(
            content = NotificationContent(
                title = "Loan payment due tomorrow",
                body = "${loan.name} — \$${formatAmount(loan.monthlyPayment)} due ${loan.nextDueDate}",
            ),
            delay = delayFromNow,
            mode = WorkMode.Foreground,
        )
    }

    /** Pull-to-refresh handler. Returns the in-flight (or just-enqueued) data sync handle. */
    public suspend fun refreshLoansData(): WorkHandle =
        scheduler.enqueueDataSync(mode = WorkMode.Background)

    private fun formatAmount(value: Double): String {
        val rounded = (value * 100).toLong() / 100.0
        return rounded.toString()
    }
}
