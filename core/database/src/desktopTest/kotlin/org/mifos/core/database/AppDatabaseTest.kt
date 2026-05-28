/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
    fun inMemoryDatabaseCanBeCreated() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

        assertNotNull(database)
    }

    @Test
    fun databaseExposeAlertDao() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

        assertNotNull(database!!.alertDao)
    }

    @Test
    fun databaseExposeInterestRateSeriesDao() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

        assertNotNull(database!!.interestRateSeriesDao)
    }

    @Test
    fun databaseVersionIsCurrent() {
        // Bumped to 10 in the v9→v10 migration that added `interest_rate_series`.
        // Update this constant when bumping AppDatabase.VERSION so the guardrail
        // stays meaningful.
        assertEquals(10, AppDatabase.VERSION)
    }

    @Test
    fun databaseNameIsCorrect() {
        assertEquals("mifos_database.db", AppDatabase.DATABASE_NAME)
    }
}
