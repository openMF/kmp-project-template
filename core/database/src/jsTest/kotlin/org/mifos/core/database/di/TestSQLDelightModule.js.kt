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

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.createDefaultWebWorkerDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.mifos.core.database.MifosSQLDelightDatabase

@OptIn(ExperimentalWasmJsInterop::class)
actual val testSQLDelightPlatformModule = module {
    single<SqlDriver> {
        val scope = get<CoroutineScope>()
        val driver = createDefaultWebWorkerDriver()
        scope.launch {
            MifosSQLDelightDatabase.Schema.awaitCreate(driver)
        }
        driver
    }
}
