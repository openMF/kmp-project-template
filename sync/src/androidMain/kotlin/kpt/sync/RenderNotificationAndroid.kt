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

import android.util.Log

private const val TAG = "kpt.sync.NotificationWorker"

/**
 * Android notification rendering — v1 stub. Logs at INFO and returns.
 *
 * **Follow-up:** wire `NotificationManagerCompat` + `NotificationCompat.Builder`
 * for real notification posting. Requires:
 *  - Application context via Koin
 *  - Notification channel created at App.onCreate
 *  - Optional `POST_NOTIFICATIONS` runtime permission on API 33+
 *
 * Kept as a stub at v1 so Phase 4 ships without dragging in the permission +
 * channel boilerplate. Sub-plan 06 / Phase 6 docs call this out as a known
 * follow-up in the upstream PR narrative.
 */
internal actual fun renderNotification(content: NotificationContent) {
    Log.i(TAG, "renderNotification(title=${content.title}, body=${content.body}, channel=${content.channelId})")
}
