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
import org.koin.dsl.module
import org.mifos.core.database.AppDatabase

val DatabaseModule = module {
    includes(platformModule)
    single { get<AppDatabase>().taskDao }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect val platformModule: Module
