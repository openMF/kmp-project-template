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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mifos.core.database.MifosSQLDelightDatabase

actual val testSQLDelightPlatformModule: Module = module {
    single<SqlDriver> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AndroidSqliteDriver(
            MifosSQLDelightDatabase.Schema.synchronous(),
            context,
            null,
        )
    }
}
