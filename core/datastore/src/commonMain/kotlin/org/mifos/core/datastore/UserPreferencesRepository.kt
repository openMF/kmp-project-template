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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.mifos.core.datastore.model.SampleUser

interface UserPreferencesRepository {
    val userFlow: Flow<SampleUser>
    val lastLoginFlow: Flow<Long>
    val lastLoginStateFlow: StateFlow<Long>
    val currentUser: SampleUser
    val lastLogin: Long

    suspend fun saveUser(user: SampleUser)
    suspend fun updateUserName(userName: String)
    suspend fun removeUser()
    suspend fun clearAll()
    fun hasUser(): Boolean
    fun observeAgeChange(onChange: (Int) -> Int): SettingsListener
}
