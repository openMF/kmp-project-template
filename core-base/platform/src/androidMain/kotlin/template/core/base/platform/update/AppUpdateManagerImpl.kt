/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.platform.update

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import template.core.base.platform.BuildConfig

private const val UPDATE_MANAGER_REQUEST_CODE: Int = 9900

class AppUpdateManagerImpl(
    private val activity: Activity,
) : AppUpdateManager {
    private val manager = AppUpdateManagerFactory.create(activity)
    private val updateOptions = AppUpdateOptions
        .newBuilder(AppUpdateType.IMMEDIATE)
        .setAllowAssetPackDeletion(false)
        .build()

    override fun checkForAppUpdate() {
        if (!BuildConfig.DEBUG) {
            manager
                .appUpdateInfo
                .addOnSuccessListener { info ->
                    val isUpdateAvailable =
                        info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

                    val isUpdateAllowed = when (updateOptions.appUpdateType()) {
                        AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                        else -> false
                    }

                    if (isUpdateAvailable && isUpdateAllowed) {
                        manager.startUpdateFlowForResult(
                            /* p0 = */
                            info,
                            /* p1 = */
                            activity,
                            /* p2 = */
                            updateOptions,
                            /* p3 = */
                            UPDATE_MANAGER_REQUEST_CODE,
                        )
                    }
                }.addOnFailureListener {
                    Log.d("Unable to update app!", "UpdateManager", it)
                }
        } else {
            Log.d("UpdateManager", "Skipping update check in debug mode")
        }
    }

    override fun checkForResumeUpdateState() {
        manager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    manager.startUpdateFlowForResult(
                        /* p0 = */
                        appUpdateInfo,
                        /* p1 = */
                        activity,
                        /* p2 = */
                        updateOptions,
                        /* p3 = */
                        UPDATE_MANAGER_REQUEST_CODE,
                    )
                }
            }
    }
}
