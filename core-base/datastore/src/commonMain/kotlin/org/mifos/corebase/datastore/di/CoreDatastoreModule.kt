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

import com.russhwolf.settings.Settings
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mifos.core.common.di.AppDispatchers
import org.mifos.corebase.datastore.UserPreferencesDataStore

val CoreDatastoreModule = module {
    single { Settings() }
    single {
        UserPreferencesDataStore(
            get(),
            get(named(AppDispatchers.IO.name)),
        )
    }
}
