/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.w3c.dom.Worker

/**
 * WasmJs factory for creating Room 3 database instances.
 *
 * Room 3 for wasmJs requires an explicit SQLiteDriver — there is no built-in default.
 * WebWorkerSQLiteDriver is used for all cases; the worker itself handles OPFS persistent
 * storage when crossOriginIsolated=true (localhost with COOP/COEP headers) and falls
 * back to in-memory SQLite when OPFS is not available (e.g. GitHub Pages).
 */
class AppDatabaseFactory {

    inline fun <reified T : RoomDatabase> createDatabase(
        databaseName: String,
    ): RoomDatabase.Builder<T> {
        return Room.databaseBuilder<T>(name = databaseName)
            .setDriver(WebWorkerSQLiteDriver(Worker("sqlite-web-worker.js")))
    }

    inline fun <reified T : RoomDatabase> createInMemoryDatabase(): RoomDatabase.Builder<T> {
        return Room.inMemoryDatabaseBuilder<T>()
            .setDriver(WebWorkerSQLiteDriver(Worker("sqlite-web-worker.js")))
    }
}
