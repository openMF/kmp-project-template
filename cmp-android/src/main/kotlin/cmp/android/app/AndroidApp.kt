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

import android.app.Application
import cmp.shared.di.KoinModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

/**
 * Android application class.
 * This class is used to initialize Koin modules for dependency injection in the Android application.
 * It sets up the Koin framework, providing the necessary dependencies for the app.
 *
 * @constructor Create empty Android app
 * @see Application
 */
class AndroidApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@AndroidApp) // Provides the Android app context
            androidLogger() // Enables Koin's logging for debugging
            modules(KoinModules.allModules) // Loads the defined Koin modules
        }
    }
}
