package org.mifos.core.database.di

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mifos.core.database.MifosSQLDelightDatabase


actual val testSQLDelightPlatformModule: Module = module {
    factory<SqlDriver> {
        NativeSqliteDriver(
            MifosSQLDelightDatabase.Schema.synchronous(),
            DB_FILE_NAME
        )
    }
}