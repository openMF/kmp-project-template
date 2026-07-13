/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.demo.currency.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.database.demo.currency.entity.RateHistoryEntity

@Dao
interface RateHistoryDao {

    @Upsert
    suspend fun upsert(entity: RateHistoryEntity)

    @Query(
        """
        SELECT * FROM rate_history
        WHERE fromCurrency = :from AND toCurrency = :to
        AND startDate = :startDate AND endDate = :endDate
        LIMIT 1
        """,
    )
    fun get(from: String, to: String, startDate: String, endDate: String): Flow<RateHistoryEntity?>

    @Query("DELETE FROM rate_history WHERE fromCurrency = :from AND toCurrency = :to")
    suspend fun delete(from: String, to: String)

    @Query("DELETE FROM rate_history")
    suspend fun deleteAll()
}
