/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.corebase.datastore.di

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mifos.core.common.di.AppDispatchers
import org.mifos.corebase.datastore.SettingsFactory
import org.mifos.corebase.datastore.UserPreferencesDataStore

@OptIn(ExperimentalSettingsApi::class)
val CoreDatastoreModule = module {
    single { Settings() }
    single {
        SettingsFactory.createSuspendSettings(
            settings = get(),
            dispatcher = get(named(AppDispatchers.IO.name)),
        )
    }

    single {
        SettingsFactory.createFlowSettings(
            settings = get(),
            dispatcher = get(named(AppDispatchers.IO.name)),
        )
    }
    single {
        UserPreferencesDataStore(
            get(),
            get(),
        )
    }

    single<CoroutineScope> { CoroutineScope(Dispatchers.Default) }
//    single<CoroutineScope> { CoroutineScope(named(AppDispatchers.Default.name)) }
}
