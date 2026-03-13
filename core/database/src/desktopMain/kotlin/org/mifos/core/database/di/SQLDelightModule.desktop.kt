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

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mifos.core.database.MifosSQLDelightDatabase
import java.io.File
import java.util.Properties

actual val driverModule: Module = module {
    single<SqlDriver> {
        JdbcSqliteDriver(
            "jdbc:sqlite:$DB_FILE_NAME",
            properties = Properties().apply { put("foreign_keys", "true") },
        ).also { driver ->
            if (!File(DB_FILE_NAME).exists() || File(DB_FILE_NAME).length() == 0L) {
                MifosSQLDelightDatabase.Schema.create(driver)
            }
        }
    }
}
