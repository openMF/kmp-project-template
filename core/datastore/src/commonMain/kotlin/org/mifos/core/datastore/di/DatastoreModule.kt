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

import org.koin.dsl.module
import org.mifos.core.datastore.UserPreferencesRepository
import org.mifos.core.datastore.UserPreferencesRepositoryImpl
import template.core.base.datastore.di.CoreDatastoreModule
import template.core.base.datastore.factory.DataStoreFactory

val DatastoreModule = module {
    includes(CoreDatastoreModule)

    single<UserPreferencesRepository> {
        UserPreferencesRepositoryImpl(preferencesStore = DataStoreFactory.create())
    }
}
