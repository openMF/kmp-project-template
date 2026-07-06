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

/**
 * Non-Android [renderNotification] actual — covers desktop (JVM), iOS, and web
 * (js / wasmJs) via the shared `nonAndroidMain` source set.
 *
 * Those platforms have no shared system-notification tray in this template, so
 * the actual logs and returns — mirroring the Android stub in
 * `RenderNotificationAndroid.kt`. Providing a real per-platform notification is
 * a known follow-up.
 */
internal actual fun renderNotification(content: NotificationContent) {
    println(
        "worker-kmp sync renderNotification(title=${content.title}, " +
            "body=${content.body}, channel=${content.channelId})",
    )
}
