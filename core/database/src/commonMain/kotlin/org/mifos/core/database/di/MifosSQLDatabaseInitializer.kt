package org.mifos.core.database.di

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.mifos.core.database.MifosSQLDelightDatabase

class MifosSQLDatabaseInitializer(
    private val driver: SqlDriver,
    private val scope: CoroutineScope
) {
    fun initialize() {
        scope.launch {
            MifosSQLDelightDatabase.Schema.awaitCreate(driver)
        }
    }
}