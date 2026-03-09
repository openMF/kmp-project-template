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

    override suspend fun deleteById(id: Long)=
        db.sampleQueries.deleteById(id)
}