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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mifos.core.database.MifosSQLDelightDatabase

const val DB_FILE_NAME = "mifos-sqldelight-database.db"

val sqlDelightModule: Module = module {
    includes(driverModule)
    single<CoroutineScope> {CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single<MifosSQLDelightDatabase> {
        MifosSQLDelightDatabase(get())
    }
}
expect val driverModule: Module
