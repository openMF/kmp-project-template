/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.datastore.di

import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mifos.core.common.di.AppDispatchers
import org.mifos.core.datastore.UserPreferencesDataStore
import org.mifos.core.datastore.UserPreferencesRepository
import org.mifos.core.datastore.UserPreferencesRepositoryImpl

val DatastoreModule = module {
    single { Settings() }
    single {
        UserPreferencesDataStore(
            get(),
            get(named(AppDispatchers.IO.name)),
            get(),
        )
    }
    single<UserPreferencesRepository> {
        UserPreferencesRepositoryImpl(
            dataStore = get(),
            ioDispatcher = get(named(AppDispatchers.IO.name)),
        )
    }
    single<CoroutineScope> { CoroutineScope(Dispatchers.Default) }
}
