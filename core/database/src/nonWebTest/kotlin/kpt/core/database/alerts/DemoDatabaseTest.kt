/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.alerts

import kpt.core.database.AppDatabase
import kpt.core.database.di.TestDatabaseModule
import kpt.core.database.di.testPlatformModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * The template's demo schema: the demo DAOs are reachable from the database and from DI, and the
 * template's own schema version.
 *
 * These pin DEMO tables and the TEMPLATE's version, so they live in the demo-owned `alerts` package
 * that `remove-demo.sh` deletes. A fork has its own entities and its own `ForkDatabaseConfig.VERSION`;
 * the checks that hold for any schema stay in `config/AppDatabaseTest` and `di/DatabaseModuleTest`.
 */
class DemoDatabaseTest {

    private var database: AppDatabase? = null

    @AfterTest
    fun teardown() {
        // stopKoin() must run even when a test failed before building, or the first fault leaks a
        // started Koin and every later test dies with KoinApplicationAlreadyStartedException.
        runCatching { database?.close() }
        stopKoin()
    }

    @Test
    fun databaseExposesTheDemoDaos() {
        startKoin { modules(testPlatformModule) }
        database = KoinPlatform.getKoin().get()

        assertNotNull(database!!.alertDao)
        assertNotNull(database!!.interestRateSeriesDao)
    }

    @Test
    fun theDemoDaoComesFromTheDatabaseModule() {
        startKoin { modules(TestDatabaseModule) }
        database = KoinPlatform.getKoin().get()

        assertNotNull(database!!.alertDao)
    }

    @Test
    fun templateSchemaVersionIsCurrent() {
        // The template's schema is at v13: the v12→v13 change added `framework_submit_drafts.attemptCount`
        // so OfflineSubmitSyncer can apply RetryPolicy backoff + the maxAttempts cap across restarts,
        // with AutoMigration(12→13) + the 13.json export. Every hop up to 13 has an AutoMigration
        // (incl. 10→11 `cloud_todos`), so a v10 device upgrades cleanly. Update this with
        // ForkDatabaseConfig.VERSION.
        assertEquals(13, AppDatabase.VERSION)
    }
}
