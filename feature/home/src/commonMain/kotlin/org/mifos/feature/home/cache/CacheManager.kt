/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home.cache

/**
 * Interface for managing in-memory caching of key-value pairs in the data store.
 *
 * Implementations of this interface provide methods for storing, retrieving, removing,
 * and clearing cached entries, as well as checking cache size and key existence.
 *
 * Example usage:
 * ```kotlin
 * val cache: CacheManager<String, Any> = LruCacheManager(100)
 * cache.put("theme", "dark")
 * val value = cache.get("theme")
 * cache.remove("theme")
 * cache.clear()
 * val size = cache.size()
 * val exists = cache.containsKey("theme")
 * ```
 *
 * @param K The type of the cache key.
 * @param V The type of the cached value.
 */
interface CacheManager<K, V> {
    /**
     * Stores a value in the cache associated with the specified key.
     *
     * @param key The key to associate with the value.
     * @param value The value to store.
     */
    fun put(key: K, value: V)

    /**
     * Retrieves a value from the cache associated with the specified key.
     *
     * @param key The key to retrieve.
     * @return The cached value, or `null` if not present.
     */
    fun get(key: K): V?

    /**
     * Removes the value associated with the specified key from the cache.
     *
     * @param key The key to remove.
     * @return The removed value, or `null` if not present.
     */
    fun remove(key: K): V?

    /**
     * Clears all entries from the cache.
     */
    fun clear()

    /**
     * Returns the number of entries currently stored in the cache.
     *
     * @return The cache size.
     */
    fun size(): Int

    /**
     * Checks whether the cache contains the specified key.
     *
     * @param key The key to check.
     * @return `true` if the cache contains the key, `false` otherwise.
     */
    fun containsKey(key: K): Boolean
}

/**
 * Base exception for all preference-related errors in the data store.
 *
 * @param message The error message.
 * @param cause The cause of the exception, if any.
 *
 * Example:
 * ```kotlin
 * throw PreferencesException("Something went wrong")
 * ```
 */
sealed class PreferencesException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

/**
 * Thrown when a cache operation fails in the data store.
 *
 * @param message The error message.
 * @param cause The cause of the exception, if any.
 *
 * Example:
 * ```kotlin
 * throw CacheException("Failed to cache value")
 * ```
 */
class CacheException(message: String, cause: Throwable? = null) :
    PreferencesException("Cache operation failed: $message", cause)
