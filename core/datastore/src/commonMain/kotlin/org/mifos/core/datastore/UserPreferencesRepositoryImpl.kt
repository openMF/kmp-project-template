/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.datastore

import com.russhwolf.settings.SettingsListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import org.mifos.core.datastore.model.SampleUser

class UserPreferencesRepositoryImpl(
    private val dataStore: UserPreferencesDataStore,
    ioDispatcher: CoroutineDispatcher,
) : UserPreferencesRepository {
    override val userFlow: Flow<SampleUser> = dataStore.userFlow
    override val lastLoginFlow: Flow<Long> = dataStore.lastLoginFlow.flowOn(ioDispatcher)
    override val lastLoginStateFlow: StateFlow<Long> = dataStore.lastLoginStateFlow
    override val currentUser: SampleUser get() = dataStore.getUser()
    override val lastLogin: Long get() = dataStore.getLastLogin()

    override suspend fun saveUser(user: SampleUser) {
        dataStore.saveUser(user)
    }

    override suspend fun updateUserName(userName: String) {
        dataStore.updateUserName(userName)
    }

    override suspend fun removeUser() {
        dataStore.removeUser()
    }

    override suspend fun clearAll() {
        dataStore.clearAll()
    }

    override fun hasUser(): Boolean = dataStore.hasUser()

    override fun observeAgeChange(onChange: (Int) -> Int): SettingsListener {
        return dataStore.observeAgeChange(onChange)
    }
}
