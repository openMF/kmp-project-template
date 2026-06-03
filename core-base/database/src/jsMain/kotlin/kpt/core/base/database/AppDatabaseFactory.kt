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
 * JS (Kotlin/JS) factory for creating Room 3 database instances.
 *
 * [createDatabase] auto-detects the runtime environment:
 *  - crossOriginIsolated = true  (localhost with COOP/COEP headers) →
 *    WebWorkerSQLiteDriver backed by a dedicated SQLite web worker.
 *    The worker uses OPFS for persistent storage when available.
 *  - crossOriginIsolated = false (GitHub Pages or any host without the headers) →
 *    Room.inMemoryDatabaseBuilder — data lives only for the current page session.
 */
@PublishedApi
internal fun isCrossOriginIsolated(): Boolean =
    js("self.crossOriginIsolated === true").unsafeCast<Boolean>()

class AppDatabaseFactory {

    inline fun <reified T : RoomDatabase> createDatabase(
        databaseName: String,
    ): RoomDatabase.Builder<T> {
        return if (isCrossOriginIsolated()) {
            Room.databaseBuilder<T>(name = databaseName)
                .setDriver(WebWorkerSQLiteDriver(Worker("sqlite-web-worker.js")))
        } else {
            Room.inMemoryDatabaseBuilder<T>()
        }
    }

    inline fun <reified T : RoomDatabase> createInMemoryDatabase(): RoomDatabase.Builder<T> {
        return Room.inMemoryDatabaseBuilder<T>()
    }
}
