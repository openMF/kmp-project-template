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

/**
 * WasmJS (Kotlin/Wasm) factory for creating Room 3 database instances.
 *
 * On the browser platform Room requires an explicit SQLite driver via [setDriver].
 * Use [createInMemoryDatabase] for in-browser use (data is not persisted across reloads).
 */
class AppDatabaseFactory {

    inline fun <reified T : RoomDatabase> createDatabase(
        databaseName: String,
    ): RoomDatabase.Builder<T> {
        return Room.databaseBuilder<T>(name = databaseName)
    }

    inline fun <reified T : RoomDatabase> createInMemoryDatabase(): RoomDatabase.Builder<T> {
        return Room.inMemoryDatabaseBuilder<T>()
    }
}
