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
import org.koin.android.ext.android.inject
import org.mifos.core.data.utils.NetworkMonitor
import org.mifos.core.data.utils.TimeZoneMonitor
import org.mifos.core.ui.utils.ShareUtils

class MainActivity : ComponentActivity() {
    private val networkMonitor: NetworkMonitor by inject()
    private val timeZoneMonitor: TimeZoneMonitor by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        ShareUtils.setActivityProvider { return@setActivityProvider this }

        setContent {
            SharedApp(
                networkMonitor = networkMonitor,
                timeZoneMonitor = timeZoneMonitor,
            )
        }
    }
}
