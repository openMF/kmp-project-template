package org.mifos.core.database.di

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mifos.core.database.MifosSQLDelightDatabase


actual val testSQLDelightPlatformModule: Module = module {
    factory<SqlDriver>{
        AndroidSqliteDriver(
            MifosSQLDelightDatabase.Schema.synchronous(),
            androidContext(),
            null
        )
    }
}