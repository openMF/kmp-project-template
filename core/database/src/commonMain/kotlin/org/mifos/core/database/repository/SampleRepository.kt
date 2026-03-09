/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.repository

import kotlinx.coroutines.flow.Flow

interface SampleRepository {
    fun getAllSamples(): Flow<List<SampleItem>>
    suspend fun insertSample(name: String): Long
    suspend fun deleteById(id: Long): Long
}

data class SampleItem(val id: Long, val name: String)
