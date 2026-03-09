/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.mifos.core.database.AppDatabase
import org.mifos.core.database.repository.SQLDelightSampleRepositoryImpl
import org.mifos.core.database.repository.SampleRepository


val DatabaseModule = module {
    includes(
        platformModule,
        sqlDelightModule,
    )
    single { get<AppDatabase>().sampleDao }
    singleOf(::SQLDelightSampleRepositoryImpl).bind<SampleRepository>()
}

expect val platformModule: Module
