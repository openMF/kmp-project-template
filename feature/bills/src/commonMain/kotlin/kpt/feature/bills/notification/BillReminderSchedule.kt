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

/**
 * Feature-local payload describing a single bill-reminder notification.
 *
 * Producers (the bill ViewModels) build this from a [kpt.core.model.demo.banking.BillReminder] by
 * computing the next concrete `triggerAtMs` instant; [BillNotificationGateway] maps it onto the
 * cross-platform `sync` scheduling infra (worker-kmp + KMPNotifier).
 *
 * @property billId Stable identifier for the bill — used as the unique-work name (re-scheduling the
 *   same bill replaces the prior request) and as a per-bill cancel tag.
 * @property title Short text shown in the OS notification heading.
 * @property body Longer body text shown in the OS notification.
 * @property triggerAtMs Epoch-millis when the notification should fire. Instants already in the past
 *   produce a silent no-op (callers compute the next occurrence themselves).
 */
data class BillReminderSchedule(
    val billId: String,
    val title: String,
    val body: String,
    val triggerAtMs: Long,
)
