/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.sync

import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.local.LocalNotifications
import com.mmk.kmpnotifier.local.localNotifier
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration

/**
 * Local-notification support for the `sync/` module, backed by
 * [KMPNotifier](https://github.com/mirzemehdi/KMPNotifier) — a Kotlin Multiplatform
 * notification library covering Android, iOS, Desktop and Web from commonMain.
 *
 * [NotificationWorker] posts through [renderNotification], which is pure commonMain
 * (KMPNotifier is multiplatform, so no per-platform rendering code is needed). Only the
 * one-time [initSyncNotifier] setup varies per platform — see the `syncNotifierConfiguration`
 * actuals — and it is invoked once at app start from the shared commonMain init in
 * `cmp-shared/utils/KoinExt.kt`.
 */
public fun initSyncNotifier() {
    KMPNotifier.initialize(syncNotifierConfiguration(), LocalNotifications)
}

/**
 * Posts a local notification via KMPNotifier's multiplatform `localNotifier`.
 * Requires [initSyncNotifier] to have run once at app start.
 */
internal fun renderNotification(content: NotificationContent) {
    KMPNotifier.localNotifier.notify(title = content.title, body = content.body)
}

/**
 * Per-platform KMPNotifier configuration. Android supplies the status-bar icon +
 * notification channel; iOS / Desktop / Web use the library defaults.
 */
internal expect fun syncNotifierConfiguration(): NotificationPlatformConfiguration
