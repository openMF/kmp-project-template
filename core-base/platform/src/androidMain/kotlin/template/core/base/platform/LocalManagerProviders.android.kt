/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.platform

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import template.core.base.platform.context.AppContext
import template.core.base.platform.context.activity
import template.core.base.platform.intent.IntentManagerImpl
import template.core.base.platform.review.AppReviewManagerImpl
import template.core.base.platform.update.AppUpdateManagerImpl

@Composable
actual fun LocalManagerProvider(
    context: AppContext,
    content: @Composable () -> Unit,
) {
    val activity = context.activity as Activity
    CompositionLocalProvider(
        LocalAppReviewManager provides AppReviewManagerImpl(activity),
        LocalIntentManager provides IntentManagerImpl(activity),
        LocalAppUpdateManager provides AppUpdateManagerImpl(activity),
    ) {
        content()
    }
}
