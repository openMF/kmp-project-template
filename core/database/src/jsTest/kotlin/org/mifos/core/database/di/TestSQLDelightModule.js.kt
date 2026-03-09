package org.mifos.core.database.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.createDefaultWebWorkerDriver
import org.koin.dsl.module

actual val testSQLDelightPlatformModule =   module {
    factory<SqlDriver> {
        createDefaultWebWorkerDriver()
    }
}