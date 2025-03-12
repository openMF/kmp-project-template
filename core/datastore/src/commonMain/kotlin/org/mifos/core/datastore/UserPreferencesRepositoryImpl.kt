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

import org.mifos.core.datastore.model.AppSettings
import org.mifos.corebase.datastore.UserPreferencesDataStore

private const val APP_SETTINGS_KEY = "app_settings"

class UserPreferencesRepositoryImpl(
    private val dataStore: UserPreferencesDataStore,
) : UserPreferencesRepository {

    override suspend fun updateSettings(settings: AppSettings) {
        dataStore.putValue(
            key = APP_SETTINGS_KEY,
            value = settings,
            serializer = AppSettings.serializer(),
        )
    }

    override suspend fun getSettings(defaultValue: AppSettings): AppSettings {
        return dataStore.getValue(
            key = APP_SETTINGS_KEY,
            default = defaultValue,
            serializer = AppSettings.serializer(),
        )
    }
}
