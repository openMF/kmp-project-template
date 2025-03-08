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

import org.mifos.core.datastore.model.SampleUser
import org.mifos.corebase.datastore.UserPreferencesDataStore

class UserPreferencesRepositoryImpl(
    private val dataStore: UserPreferencesDataStore,
) : UserPreferencesRepository {
    override suspend fun saveUser(
        key: String,
        user: SampleUser,
    ) {
        dataStore.putValue(
            key = key,
            value = user,
            serializer = SampleUser.serializer(),
        )
    }

    override suspend fun getUser(
        key: String,
        defaultValue: SampleUser,
    ): SampleUser {
        return dataStore.getValue(
            key = key,
            default = defaultValue,
            serializer = SampleUser.serializer(),
        )
    }

    override suspend fun getDoubleNumber(key: String, defaultValue: Double): Double {
        return dataStore.getValue(key, defaultValue)
    }

    override suspend fun saveDoubleNumber(key: String, number: Double) {
        dataStore.putValue(key, number)
    }
}
