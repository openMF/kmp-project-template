package org.mifos.core.database.di

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.dsl.module
import org.mifos.core.database.MifosSQLDelightDatabase

actual val driverModule: org.koin.core.module.Module = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            MifosSQLDelightDatabase.Schema.synchronous(),
            get(),
            DB_FILE_NAME
        )
    }
}