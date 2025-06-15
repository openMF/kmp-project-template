/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.di

import cmp.navigation.AppViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.mifos.core.common.di.DispatchersModule
import org.mifos.core.data.di.DataModule
import org.mifos.core.datastore.di.DatastoreModule
import org.mifos.feature.home.di.HomeModule
import org.mifos.feature.settings.SettingsModule

object KoinModules {
    private val dataModule = module {
        includes(DataModule)
    }

    private val dispatcherModule = module {
        includes(DispatchersModule)
    }

    private val AppModule = module {
        viewModelOf(::AppViewModel)
    }

    private val featureModule = module {
        includes(
            HomeModule,
            SettingsModule,
        )
    }

    val allModules = listOf(
        dataModule,
        dispatcherModule,
        DatastoreModule,
        featureModule,
        AppModule,
    )
}
