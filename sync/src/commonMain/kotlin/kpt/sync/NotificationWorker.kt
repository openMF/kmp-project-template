/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(io.github.mobilebytelabs.worker.ExperimentalWorkerApi::class)

package kpt.sync

import io.github.mobilebytelabs.worker.CoroutineWorker
import io.github.mobilebytelabs.worker.WorkResult
import io.github.mobilebytelabs.worker.WorkerContext

/**
 * One-shot notification worker. Reads [NotificationContent] from `inputData`
 * + calls the per-platform [renderNotification] expect/actual.
 *
 * On Android the actual posts a `NotificationCompat` notification via
 * `NotificationManagerCompat`. On iOS/Desktop/Web the actuals log + return
 * (real per-platform notification rendering is a follow-up).
 */
public class NotificationWorker(
    context: WorkerContext,
) : CoroutineWorker(context) {

    public companion object {
        public const val KEY_TITLE: String = "kpt.sync.notification.title"
        public const val KEY_BODY: String = "kpt.sync.notification.body"
        public const val KEY_CHANNEL_ID: String = "kpt.sync.notification.channelId"
    }

    override suspend fun doWork(): WorkResult {
        val title = inputData.getString(KEY_TITLE) ?: return WorkResult.failure("missing title")
        val body = inputData.getString(KEY_BODY) ?: return WorkResult.failure("missing body")
        val channelId = inputData.getString(KEY_CHANNEL_ID)
        return runCatching {
            renderNotification(
                NotificationContent(title = title, body = body, channelId = channelId),
            )
            WorkResult.success()
        }.getOrElse { WorkResult.failure("renderNotification threw: ${it.message}") }
    }
}

/**
 * Per-platform notification rendering hook. Android actual posts a real
 * NotificationCompat notification; other platforms log + no-op at v1.
 */
internal expect fun renderNotification(content: NotificationContent)
