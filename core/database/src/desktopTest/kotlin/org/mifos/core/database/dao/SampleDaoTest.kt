/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.dao

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.mifos.core.database.AppDatabase
import org.mifos.core.database.entity.SampleEntity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SampleDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var sampleDao: SampleDao

    @BeforeTest
    fun setup() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        sampleDao = database.sampleDao
    }

    @AfterTest
    fun teardown() {
        database.close()
    }

    @Test
    fun getAllSamples_emptyDatabase_returnsEmptyList() = runTest {
        sampleDao.getAllSamples().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertSample_singleEntity_returnedByGetAll() = runTest {
        val entity = SampleEntity(id = 1, name = "Test Sample")

        sampleDao.insertSample(listOf(entity))

        sampleDao.getAllSamples().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Test Sample", result.first().name)
            assertEquals(1, result.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertSample_multipleEntities_allReturnedByGetAll() = runTest {
        val entities = listOf(
            SampleEntity(id = 1, name = "First"),
            SampleEntity(id = 2, name = "Second"),
            SampleEntity(id = 3, name = "Third"),
        )

        sampleDao.insertSample(entities)

        sampleDao.getAllSamples().test {
            val result = awaitItem()
            assertEquals(3, result.size)
            assertEquals("First", result[0].name)
            assertEquals("Second", result[1].name)
            assertEquals("Third", result[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertSample_duplicateId_replacesExisting() = runTest {
        val original = SampleEntity(id = 1, name = "Original")
        val replacement = SampleEntity(id = 1, name = "Replaced")

        sampleDao.insertSample(listOf(original))
        sampleDao.insertSample(listOf(replacement))

        sampleDao.getAllSamples().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Replaced", result.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertSample_emptyList_noEffect() = runTest {
        sampleDao.insertSample(emptyList())

        sampleDao.getAllSamples().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllSamples_emitsUpdatesOnInsert() = runTest {
        sampleDao.getAllSamples().test {
            // Initial empty state
            assertEquals(0, awaitItem().size)

            // Insert triggers new emission
            sampleDao.insertSample(listOf(SampleEntity(id = 1, name = "New")))
            assertEquals(1, awaitItem().size)

            // Another insert triggers another emission
            sampleDao.insertSample(listOf(SampleEntity(id = 2, name = "Another")))
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
