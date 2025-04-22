/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.corebase.datastore

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.Serializable
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * A test class for verifying the functionality of [UserPreferencesDataStore] in a Kotlin Multiplatform project.
 * This class uses Koin for dependency injection, [MapSettings] for in-memory key-value storage, and
 * [StandardTestDispatcher] for controlling coroutine execution during tests. It tests various operations
 * such as storing, retrieving, and managing key-value pairs, including primitive types and custom serialized types.
 */
@ExperimentalCoroutinesApi
class UserPreferencesDatastoreTest : KoinTest {

    private val userPreferencesDataStore: UserPreferencesDataStore by inject()
    private val testDispatcher = StandardTestDispatcher()

    private val testModule = module {
        single<Settings> { MapSettings() }
        single<CoroutineDispatcher> { testDispatcher }
        single {
            UserPreferencesDataStore(
                get(),
                get(),
            )
        }
    }

    /**
     * A data class representing a user with an ID, name, and age, used for testing serialization
     * in [UserPreferencesDataStore].
     * This class is marked as [Serializable] to enable serialization with Kotlinx Serialization.
     *
     * @property id The unique identifier of the user.
     * @property name The name of the user.
     * @property age The age of the user.
     */
    @Serializable
    data class User(
        val id: Long,
        val name: String,
        val age: Int,
    ) {
        companion object {
            /** A default user instance used as a fallback value in tests. */
            val defaultUser = User(
                id = 1,
                name = "Mifos",
                age = 15,
            )
        }
    }

    /**
     * Sets up the test environment before each test.
     * Initializes Koin with the [testModule] to provide dependencies and sets the main dispatcher to [testDispatcher]
     * for controlling coroutine execution.
     */
    @BeforeTest
    fun setup() {
        startKoin {
            modules(testModule)
        }
        Dispatchers.setMain(testDispatcher)
    }

    /**
     * Tears down the test environment after each test.
     * Stops Koin to clean up the dependency injection container and resets the main dispatcher.
     */
    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    /**
     * Tests that [UserPreferencesDataStore.putValue] successfully stores an integer value.
     * Verifies that the stored value can be retrieved using [UserPreferencesDataStore.getValue].
     */
    @Test
    fun userPreferencesDataStore_PutIntValue_ValueStoredSuccessfully() = runTest(testDispatcher) {
        // Arrange
        val key = "user_score"
        val value = 42

        // Act
        userPreferencesDataStore.putValue(key, value)
        val retrievedValue = userPreferencesDataStore.getValue(key, 0)

        // Assert
        assertEquals(value, retrievedValue)
    }

    /**
     * Tests that [UserPreferencesDataStore.putValue] successfully stores a string value.
     * Verifies that the stored value can be retrieved using [UserPreferencesDataStore.getValue].
     */
    @Test
    fun userPreferencesDataStore_PutStringValue_ValueStoredSuccessfully() = runTest(testDispatcher) {
        // Arrange
        val key = "user_name"
        val value = "Alice"

        // Act
        userPreferencesDataStore.putValue(key, value)
        val retrievedValue = userPreferencesDataStore.getValue(key, "")

        // Assert
        assertEquals(value, retrievedValue)
    }

    /**
     * Tests that [UserPreferencesDataStore.putValue] successfully stores a custom type ([User]) with a serializer.
     * Verifies that the stored value can be retrieved using [UserPreferencesDataStore.getValue] with
     * the same serializer.
     */
    @Test
    fun userPreferencesDataStore_PutCustomTypeWithSerializer_ValueStoredSuccessfully() = runTest(testDispatcher) {
        // Arrange
        val key = "user_data"
        val user = User(1, "Bob", age = 20)

        // Act
        userPreferencesDataStore.putValue(key, user, User.serializer())
        val retrievedUser = userPreferencesDataStore.getValue(
            key,
            User.defaultUser,
            User.serializer(),
        )

        // Assert
        assertEquals(user, retrievedUser)
    }

    /**
     * Tests that [UserPreferencesDataStore.getValue] successfully retrieves a stored integer value.
     * Verifies that the retrieved value matches the stored value.
     */
    @Test
    fun userPreferencesDataStore_GetIntValue_ValueRetrievedSuccessfully() = runTest(testDispatcher) {
        // Arrange
        val key = "user_score"
        val defaultValue = 0
        val storedValue = 42

        // Act
        userPreferencesDataStore.putValue(key, storedValue)
        val result = userPreferencesDataStore.getValue(key, defaultValue)

        // Assert
        assertEquals(storedValue, result)
    }

    /**
     * Tests that [UserPreferencesDataStore.getValue] successfully retrieves a stored string value.
     * Verifies that the retrieved value matches the stored value.
     */
    @Test
    fun userPreferencesDataStore_GetStringValue_ValueRetrievedSuccessfully() = runTest(testDispatcher) {
        // Arrange
        val key = "user_name"
        val defaultValue = "Unknown"
        val storedValue = "Alice"

        // Act
        userPreferencesDataStore.putValue(key, storedValue)
        val result = userPreferencesDataStore.getValue(key, defaultValue)

        // Assert
        assertEquals(storedValue, result)
    }

    /**
     * Tests that [UserPreferencesDataStore.getValue] successfully retrieves a
     * stored custom type ([User]) with a serializer.
     * Verifies that the retrieved value matches the stored value.
     */
    @Test
    fun userPreferencesDataStore_GetCustomTypeWithSerializer_ValueRetrievedSuccessfully() = runTest(testDispatcher) {
        // Arrange
        val key = "user_data"
        val storedValue = User(1, "Bob", age = 20)

        // Act
        userPreferencesDataStore.putValue(key, storedValue, User.serializer())
        val result = userPreferencesDataStore.getValue(key, User.defaultUser, User.serializer())

        // Assert
        assertEquals(storedValue, result)
    }

    /**
     * Tests that [UserPreferencesDataStore.hasKey] correctly identifies the existence of a key.
     * Verifies that a key exists after storing a value.
     */
    @Test
    fun userPreferencesDataStore_HasKeyCheck_KeyExists() = runTest(testDispatcher) {
        // Arrange
        val key = "user_score"

        // Act
        userPreferencesDataStore.putValue(key, 42)
        val result = userPreferencesDataStore.hasKey(key)

        // Assert
        assertTrue(result)
    }

    /**
     * Tests that [UserPreferencesDataStore.hasKey] correctly identifies the absence of a key.
     * Verifies that a key does not exist when no value has been stored.
     */
    @Test
    fun userPreferencesDataStore_HasKeyCheck_KeyDoesNotExist() = runTest(testDispatcher) {
        // Arrange
        val key = "user_score"

        // Act
        val result = userPreferencesDataStore.hasKey(key)

        // Assert
        assertFalse(result)
    }

    /**
     * Tests that [UserPreferencesDataStore.removeValue] successfully removes a stored value.
     * Verifies that the key no longer exists after removal.
     */
    @Test
    fun userPreferencesDataStore_RemoveValue_ValueRemovedSuccessfully() = runTest(testDispatcher) {
        // Arrange
        val key = "user_score"

        // Act
        userPreferencesDataStore.putValue(key, 42)
        userPreferencesDataStore.removeValue(key)
        val result = userPreferencesDataStore.hasKey(key)

        // Assert
        assertFalse(result)
    }

    /**
     * Tests that [UserPreferencesDataStore.clearAll] successfully clears all stored values.
     * Verifies that the size of the data store is zero after clearing.
     */
    @Test
    fun userPreferencesDataStore_ClearAll_AllValuesClearedSuccessfully() = runTest(testDispatcher) {
        // Arrange
        val key1 = "user_score"
        val key2 = "user_name"
        userPreferencesDataStore.putValue(key1, 42)
        userPreferencesDataStore.putValue(key2, "Alice")

        // Act
        userPreferencesDataStore.clearAll()
        val result = userPreferencesDataStore.getSize()

        // Assert
        assertEquals(0, result)
    }

    /**
     * Tests that [UserPreferencesDataStore.getAllKeys] successfully retrieves all stored keys.
     * Verifies that the retrieved keys match the expected set of keys.
     */
    @Test
    fun userPreferencesDataStore_GetAllKeys_KeysRetrievedSuccessfully() = runTest(testDispatcher) {
        // Act
        userPreferencesDataStore.putValue("user_score", 42)
        userPreferencesDataStore.putValue("user_name", "Alice")
        val result = userPreferencesDataStore.getAllKeys()

        // Assert
        assertEquals(setOf("user_score", "user_name"), result)
    }

    /**
     * Tests that [UserPreferencesDataStore.getSize] successfully retrieves the number of stored key-value pairs.
     * Verifies that the size matches the expected number of entries.
     */
    @Test
    fun userPreferencesDataStore_GetSize_SizeRetrievedSuccessfully() = runTest(testDispatcher) {
        // Arrange
        val size = 2

        // Act
        userPreferencesDataStore.putValue("user_score", 42)
        userPreferencesDataStore.putValue("user_name", "Alice")
        val result = userPreferencesDataStore.getSize()

        // Assert
        assertEquals(size, result)
    }
}
