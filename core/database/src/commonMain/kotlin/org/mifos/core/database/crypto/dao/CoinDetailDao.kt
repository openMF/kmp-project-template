/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.crypto.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import org.mifos.core.database.crypto.entity.CoinDetailEntity

@Dao
interface CoinDetailDao {

    @Upsert
    suspend fun upsert(entity: CoinDetailEntity)

    @Query("SELECT * FROM coin_detail WHERE id = :coinId LIMIT 1")
    fun getById(coinId: String): Flow<CoinDetailEntity?>

    @Query("DELETE FROM coin_detail WHERE id = :coinId")
    suspend fun delete(coinId: String)

    @Query("DELETE FROM coin_detail")
    suspend fun deleteAll()
}
