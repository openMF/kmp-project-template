package org.mifos.core.database.repository

import kotlinx.coroutines.flow.Flow

interface SampleRepository {
    fun getAllSamples(): Flow<List<SampleItem>>
    suspend fun insertSample(name: String): Long
    suspend fun deleteById(id: Long): Long
}

data class SampleItem(val id: Long, val name: String)