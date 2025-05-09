/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.android.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import cmp.shared.SharedApp
import template.core.base.platform.update.AppUpdateManager
import template.core.base.platform.update.AppUpdateManagerImpl
import template.core.base.ui.ShareUtils

/**
 * Main activity class.
 * This class is used to set the content view of the activity.
 *
 * @constructor Create empty Main activity
 * @see ComponentActivity
 */
class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager: AppUpdateManager

    /**
     * Called when the activity is starting.
     * This is where most initialization should go: calling [setContentView(int)] to inflate the activity's UI,
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerImpl(this)

        installSplashScreen()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        template.core.base.ui.ShareUtils.setActivityProvider { return@setActivityProvider this }

        /**
         * Set the content view of the activity.
         * @see setContent
         */
        setContent {
//            val status = viewModel.networkStatus.collectAsStateWithLifecycle().value
//
//            if (status) {
//                appUpdateManager.checkForAppUpdate()
//            }

            SharedApp()
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.checkForResumeUpdateState()
    }
}
