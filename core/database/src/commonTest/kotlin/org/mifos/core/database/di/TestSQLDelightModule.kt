/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.mifos.core.database.repository.SQLDelightSampleRepositoryImpl
import org.mifos.core.database.repository.SampleRepository

val TestSQLDelightModule = module {
    includes(testSQLDelightPlatformModule)
    single<SampleRepository> { SQLDelightSampleRepositoryImpl(get()) }
}

expect val testSQLDelightPlatformModule: Module
