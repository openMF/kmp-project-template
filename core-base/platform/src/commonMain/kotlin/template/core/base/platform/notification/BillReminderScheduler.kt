/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.platform.notification

/**
 * Cross-platform contract for scheduling a single bill-reminder notification.
 *
 * Each platform's actual implementation owns the OS-specific delivery mechanism:
 *
 * | Platform | Mechanism | Background behaviour |
 * |---|---|---|
 * | Android | `WorkManager` `OneTimeWorkRequest` | Fires when app is killed (subject to Doze). |
 * | iOS | `UNUserNotificationCenter` + calendar trigger | Fires while backgrounded; needs permission. |
 * | Desktop (JVM) | Best-effort stub — no-op | No portable notification API; in-app banner only. |
 * | JS / WasmJS | Best-effort stub — no-op | Needs a Service Worker; deferred. |
 *
 * **Permission-denied behaviour**: on platforms with runtime permission gates (Android 13+,
 * iOS), [schedule] is a no-op if the OS rejects the request; the bill itself is still
 * persisted by the caller, and the upcoming-bills surface acts as the in-app fallback.
 *
 * **Idempotency**: [schedule] replaces any prior request for the same `billId` —
 * callers may safely re-schedule after editing the bill without manual [cancel].
 *
 * **Threading**: implementations are safe to call from any coroutine context; suspending
 * APIs offload to the appropriate dispatcher internally.
 */
expect class BillReminderScheduler {
    /**
     * Schedule (or replace) a single notification for the given bill.
     *
     * The actual fire-time is `triggerAtMs` epoch-millis. If that instant is already past,
     * implementations no-op rather than fire immediately — callers compute the next
     * occurrence by combining `dueDay` + `reminderDaysBefore` themselves.
     */
    suspend fun schedule(bill: BillReminderSchedule)

    /** Cancel a previously-scheduled notification by `billId`. No-op when none is pending. */
    suspend fun cancel(billId: String)

    /** Cancel every scheduled bill-reminder notification this app instance has created. */
    suspend fun cancelAll()
}

/**
 * Platform-neutral payload describing a single bill-reminder notification.
 *
 * Producers (feature ViewModels) build this from a [org.mifos.core.model.banking.BillReminder]
 * by computing the next concrete `triggerAtMs` instant; the scheduler stays domain-agnostic
 * so it can be reused for any future "fire one notification at X time" surface.
 *
 * @property billId Stable identifier for the bill (used as the OS-level request tag, so
 *   re-scheduling the same bill replaces the prior request).
 * @property title Short text shown in the OS notification heading.
 * @property body Longer body text shown in the OS notification.
 * @property triggerAtMs Epoch-millis when the notification should fire. Instants already
 *   in the past produce a silent no-op.
 */
data class BillReminderSchedule(
    val billId: String,
    val title: String,
    val body: String,
    val triggerAtMs: Long,
)
