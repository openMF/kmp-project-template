package org.mifos.core.database.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mifos.core.database.MifosSQLDelightDatabase
import java.util.Properties

actual val testSQLDelightPlatformModule: Module = module {
    factory<SqlDriver> {
        JdbcSqliteDriver(
            "jdbc:sqlite:$DB_FILE_NAME",
            properties = Properties().apply { put("foreign_keys", "true") }
        ).also { MifosSQLDelightDatabase.Schema.create(it) }
    }
}