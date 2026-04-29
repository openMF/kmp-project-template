/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.di

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.mifos.core.database.AppDatabase
import org.mifos.core.database.dao.SampleDao
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class DatabaseModuleTest : KoinTest {

    @BeforeTest
    fun setup() {
        startKoin {
            modules(TestDatabaseModule)
        }
    }

    @AfterTest
    fun teardown() {
        stopKoin()
    }

    @Test
    fun testDatabaseModule_providesAppDatabase() {
        val database: AppDatabase = get()
        assertNotNull(database)
    }

    @Test
    fun testDatabaseModule_providesSampleDao() {
        val dao: SampleDao = get()
        assertNotNull(dao)
    }

    @Test
    fun testDatabaseModule_sampleDaoComesFromDatabase() {
        val database: AppDatabase = get()
        val dao: SampleDao = get()
        assertNotNull(database.sampleDao)
        assertNotNull(dao)
    }
}
