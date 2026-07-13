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
import kpt.core.database.demo.currency.entity.ExchangeRatesEntity

@Dao
interface ExchangeRatesDao {

    @Upsert
    suspend fun upsert(entity: ExchangeRatesEntity)

    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :currency LIMIT 1")
    fun getByBase(currency: String): Flow<ExchangeRatesEntity?>

    @Query("DELETE FROM exchange_rates WHERE baseCurrency = :currency")
    suspend fun deleteByBase(currency: String)

    @Query("DELETE FROM exchange_rates")
    suspend fun deleteAll()

    @Query("DELETE FROM exchange_rates WHERE fetchedAt < :epochMillis")
    suspend fun deleteOlderThan(epochMillis: Long)
}
