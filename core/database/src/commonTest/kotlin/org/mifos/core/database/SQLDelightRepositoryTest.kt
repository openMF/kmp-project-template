/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.mifos.core.database.di.TestSQLDelightModule
import org.mifos.core.database.repository.SampleRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class SQLDelightRepositoryTest {

    protected lateinit var repository: SampleRepository
    protected var koinApp: KoinApplication? = null

    @BeforeTest
    open fun setup() {
        koinApp = startKoin { modules(TestSQLDelightModule) }
        repository = koinApp!!.koin.get()
    }

    @AfterTest
    open fun teardown() {
        koinApp?.koin?.getOrNull<SqlDriver>()?.close()
        stopKoin()
        koinApp = null
    }

    @Test
    fun emptyDatabaseReturnsEmptyList() = runTest {
        repository.getAllSamples().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertSampleAndRetrieveIt() = runTest {
        repository.insertSample("Alice")

        repository.getAllSamples().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Alice", items[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertMultipleSamplesAndRetrieveAll() = runTest {
        repository.insertSample("Alice")
        repository.insertSample("Bob")
        repository.insertSample("Charlie")

        repository.getAllSamples().test {
            val items = awaitItem()
            assertEquals(3, items.size)
            assertTrue(items.any { it.name == "Alice" })
            assertTrue(items.any { it.name == "Bob" })
            assertTrue(items.any { it.name == "Charlie" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteSampleRemovesItFromDatabase() = runTest {
        repository.insertSample("Alice")
        repository.insertSample("Bob")

        var aliceId = 0L
        repository.getAllSamples().test {
            aliceId = awaitItem().first { it.name == "Alice" }.id
            cancelAndIgnoreRemainingEvents()
        }

        repository.deleteById(aliceId)

        repository.getAllSamples().test {
            val remaining = awaitItem()
            assertEquals(1, remaining.size)
            assertEquals("Bob", remaining[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertSampleWithSameNameCreatesSeparateEntries() = runTest {
        repository.insertSample("Alice")
        repository.insertSample("Alice")

        repository.getAllSamples().test {
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
