/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AppDatabaseTest {

    private var database: AppDatabase? = null

    @AfterTest
    fun teardown() {
        database?.close()
    }

    @Test
    fun inMemoryDatabase_canBeCreated() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

        assertNotNull(database)
    }

    @Test
    fun database_exposeSampleDao() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

        assertNotNull(database!!.sampleDao)
    }

    @Test
    fun databaseVersion_isOne() {
        assertEquals(1, AppDatabase.VERSION)
    }

    @Test
    fun databaseName_isCorrect() {
        assertEquals("mifos_database.db", AppDatabase.DATABASE_NAME)
    }
}
