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

interface UserPreferencesRepository {

    suspend fun saveUser(key: String, user: SampleUser)
    suspend fun getUser(key: String, defaultValue: SampleUser): SampleUser

    suspend fun getDoubleNumber(key: String, defaultValue: Double): Double
    suspend fun saveDoubleNumber(key: String, number: Double)
}
