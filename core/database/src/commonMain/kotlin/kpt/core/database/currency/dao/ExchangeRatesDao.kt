/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.currency.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.currency.entity.ExchangeRatesEntity

/**
 * Room DAO for cached FX rates, keyed by base currency.
 *
 * Bound into the Store's `SourceOfTruth` — the reader/writer/delete lambdas are the ONLY callers
 * of these members (S5-1). A repository reaching past the Store to a DAO bypasses caching and
 * freshness entirely.
 */
@DbDao
@Dao
interface ExchangeRatesDao {

    /** Inserts or replaces the cached rates for one base currency. */
    @Upsert
    suspend fun upsert(entity: ExchangeRatesEntity)

    /** Streams the cached rates for one base currency, emitting null when nothing is cached. */
    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :currency LIMIT 1")
    fun getByBase(currency: String): Flow<ExchangeRatesEntity?>

    /**
     * Evicts the cached rates for one base currency, leaving every other base intact — so switching base re-fetches
     * only what changed.
     */
    @Query("DELETE FROM exchange_rates WHERE baseCurrency = :currency")
    suspend fun deleteByBase(currency: String)

    /** Clears the cache. Called on logout. */
    @Query("DELETE FROM exchange_rates")
    suspend fun deleteAll()

    /**
     * Prunes rows cached before [epochMillis] — the cold-start sweep, so a stale day's rates are not served as
     * current.
     */
    @Query("DELETE FROM exchange_rates WHERE fetchedAt < :epochMillis")
    suspend fun deleteOlderThan(epochMillis: Long)
}
