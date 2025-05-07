/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.platform.review

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory
import template.core.base.platform.BuildConfig

/**
 * Default implementation of [AppReviewManager].
 */
class AppReviewManagerImpl(
    private val activity: Activity,
) : AppReviewManager {
    override fun promptForReview() {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                manager.launchReviewFlow(activity, reviewInfo)
            } else {
                Log.e("Failed to launch review flow.", task.exception?.message.toString())
            }
        }

        if (BuildConfig.DEBUG) {
            Log.d("ReviewManager", "Prompting for review")
        }
    }

    override fun promptForCustomReview() {
        // TODO:: Implement custom review flow
    }
}
