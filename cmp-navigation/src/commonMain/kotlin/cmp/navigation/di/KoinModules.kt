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

import org.koin.dsl.module
import org.mifos.core.common.di.DispatchersModule
import org.mifos.core.data.di.DataModule

object KoinModules {
    private val dataModule = module {
        includes(DataModule)
    }

    private val dispatcherModule = module {
        includes(DispatchersModule)
    }

    val allModules = listOf(
        dataModule,
        dispatcherModule,
    )
}
