/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.mifos.shared.di.KoinModules

class MifosApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MifosApp)
            androidLogger()
            modules(KoinModules.allModules)
        }
    }
}