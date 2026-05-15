/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.repositoryImpl

import co.touchlab.kermit.Logger
import org.mifos.core.data.repository.StoreCacheManager
import org.mifos.core.database.dao.BookkeeperDao
import org.mifos.core.database.dao.DraftDao
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Store

/**
 * Registration-based cache manager. Feature DI modules call [register] for each Store that
 * should be cleared on logout; foundation infrastructure stays free of feature knowledge.
 *
 * Usage in a feature's DI module:
 * ```kotlin
 * (get<StoreCacheManager>() as StoreCacheManagerImpl)
 *     .register(get(ApplicationStoreRegistry.YourStore))
 * ```
 */
@OptIn(ExperimentalStoreApi::class)
class StoreCacheManagerImpl(
    private val bookkeeperDao: BookkeeperDao,
    private val draftDao: DraftDao,
) : StoreCacheManager {

    private val registeredStores = mutableSetOf<Store<Any, Any>>()

    /** Register a store to be cleared on logout. Call from your feature's DI module. */
    @Suppress("UNCHECKED_CAST")
    fun register(store: Store<*, *>) {
        registeredStores += store as Store<Any, Any>
    }

    override suspend fun clearAll() {
        Logger.d { "StoreCacheManager: clearing ${registeredStores.size} registered stores" }

        // Store.clear() clears both in-memory cache AND SourceOfTruth (deleteAll)
        registeredStores.forEach { it.clear() }

        // Clear bookkeeper sync-failure records
        bookkeeperDao.deleteAll()

        // Clear pending form drafts — prevents user A's drafts surfacing on user B's session
        draftDao.deleteAll()
    }
}
