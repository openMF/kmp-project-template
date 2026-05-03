/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import org.mifos.core.database.entity.CoinMarketEntity

@Dao
interface CoinMarketDao {

    @Upsert
    suspend fun upsertAll(entities: List<CoinMarketEntity>)

    @Query("SELECT * FROM coin_markets ORDER BY marketCapRank ASC LIMIT :limit OFFSET :offset")
    fun getPage(limit: Int, offset: Int): Flow<List<CoinMarketEntity>>

    @Query("SELECT * FROM coin_markets ORDER BY marketCapRank ASC")
    fun getAll(): Flow<List<CoinMarketEntity>>

    @Query("DELETE FROM coin_markets WHERE page = :page")
    suspend fun deleteByPage(page: Int)

    @Query("DELETE FROM coin_markets")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM coin_markets")
    suspend fun count(): Int
}
