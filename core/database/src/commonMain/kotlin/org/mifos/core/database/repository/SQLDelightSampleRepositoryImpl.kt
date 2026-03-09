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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mifos.core.database.MifosSQLDelightDatabase

class SQLDelightSampleRepositoryImpl(private val db: MifosSQLDelightDatabase) : SampleRepository {
    override fun getAllSamples(): Flow<List<SampleItem>> =
        db.sampleQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { SampleItem(it.id, it.name) } }

    override suspend fun insertSample(name: String) =
        db.sampleQueries.insertSample(name)

    override suspend fun deleteById(id: Long) =
        db.sampleQueries.deleteById(id)
}
