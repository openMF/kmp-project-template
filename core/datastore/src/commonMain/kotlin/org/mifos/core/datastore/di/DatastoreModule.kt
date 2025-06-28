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
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.mifos.core.datastore.UserPreferencesRepository
import org.mifos.core.datastore.UserPreferencesRepositoryImpl
import template.core.base.common.di.CommonModule

val DatastoreModule = module {
    includes(CommonModule)

    single<Settings> { Settings() }

    singleOf(::UserPreferencesRepositoryImpl) bind UserPreferencesRepository::class
}
