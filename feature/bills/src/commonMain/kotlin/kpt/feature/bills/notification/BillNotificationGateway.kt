/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.bills.notification

import kpt.sync.NotificationContent
import kpt.sync.WorkScheduler
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

/**
 * Feature-local boundary over bill-reminder scheduling.
 *
 * The ViewModel layer depends on this small `interface` (mockable in tests) while production wiring
 * routes to [BillNotificationGatewayImpl], which schedules through the cross-platform `sync`
 * [WorkScheduler] (worker-kmp for delivery + KMPNotifier for the actual notification) — so there is
 * NO per-platform `expect/actual` scheduler and NO WorkManager/UNUserNotificationCenter plumbing in
 * this module. Keeping the seam in `feature/bills` (banking-domain-specific) keeps the framework lean.
 */
interface BillNotificationGateway {

    /** Schedule (or replace) a notification for the given bill. */
    suspend fun schedule(bill: BillReminderSchedule)

    /** Cancel a single bill's pending notification. */
    suspend fun cancel(billId: String)

    /** Cancel every pending bill-reminder notification. */
    suspend fun cancelAll()
}

/**
 * Production gateway — maps [BillReminderSchedule] onto the `sync` [WorkScheduler]:
 *  - **schedule** enqueues a unique-named notification (REPLACE on re-schedule) tagged both with a
 *    per-bill tag and the shared [TAG_BILL_REMINDER], with `initialDelay = triggerAt - now`.
 *  - **cancel** cancels the per-bill tag; **cancelAll** cancels the shared tag.
 *
 * Wired in [kpt.feature.bills.di.BillsModule].
 */
class BillNotificationGatewayImpl(
    private val workScheduler: WorkScheduler,
) : BillNotificationGateway {

    @OptIn(ExperimentalTime::class)
    override suspend fun schedule(bill: BillReminderSchedule) {
        val delayMs = bill.triggerAtMs - Clock.System.now().toEpochMilliseconds()
        // Past instant — silently drop; callers re-compute the next occurrence.
        if (delayMs <= 0L) return
        workScheduler.scheduleNotification(
            content = NotificationContent(
                title = bill.title,
                body = bill.body,
                channelId = CHANNEL_ID,
            ),
            delay = delayMs.milliseconds,
            uniqueName = uniqueName(bill.billId),
            tags = listOf(TAG_BILL_REMINDER, tagFor(bill.billId)),
        )
    }

    override suspend fun cancel(billId: String) = workScheduler.cancelWorkByTag(tagFor(billId))

    override suspend fun cancelAll() = workScheduler.cancelWorkByTag(TAG_BILL_REMINDER)

    companion object {
        /** Android notification channel for bill reminders (created by the host app's `App.onCreate`). */
        const val CHANNEL_ID: String = "bill_reminders"

        /** Shared tag on every bill-reminder request — lets [cancelAll] cancel them all at once. */
        const val TAG_BILL_REMINDER: String = "bill_reminder"

        /** Per-bill unique-work name — re-scheduling the same id REPLACEs the prior request. */
        fun uniqueName(billId: String): String = "bill_reminder_$billId"

        /** Per-bill cancel tag — [cancel] targets just this bill's pending request. */
        fun tagFor(billId: String): String = "bill_reminder_$billId"
    }
}
