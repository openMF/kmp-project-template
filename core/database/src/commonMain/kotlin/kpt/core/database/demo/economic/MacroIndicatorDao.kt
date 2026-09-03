/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.demo.economic

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data-access object for the `macro_indicator` table.
 *
 * All reads return reactive [Flow]s; all writes are `suspend` one-shots. Observations are ordered
 * ascending by year to match [kpt.core.model.demo.economic.MacroIndicator.observations], whose
 * contract is "ordered ascending by year" — the mapper therefore does not re-sort.
 */
@Dao
interface MacroIndicatorDao {

    /**
     * Observe every cached observation for one (country, indicator) pair, oldest year first.
     *
     * @param countryCode ISO 3166-1 alpha-2 country code.
     * @param indicator [kpt.core.model.demo.economic.IndicatorKind] name.
     */
    @Query(
        "SELECT * FROM macro_indicator WHERE countryCode = :countryCode AND indicator = :indicator " +
            "ORDER BY year ASC",
    )
    fun observeSeries(countryCode: String, indicator: String): Flow<List<MacroIndicatorEntity>>

    /** Insert or replace a batch of observations (used during cache refresh). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<MacroIndicatorEntity>)

    /**
     * Drop every cached observation for one (country, indicator) pair.
     *
     * Called before [upsertAll] on a refresh so years the World Bank has since retracted do not
     * linger — REPLACE alone would leave stale rows the new response no longer covers.
     */
    @Query("DELETE FROM macro_indicator WHERE countryCode = :countryCode AND indicator = :indicator")
    suspend fun deleteSeries(countryCode: String, indicator: String)

    /** Clear the whole table — used by the logout cache purge. */
    @Query("DELETE FROM macro_indicator")
    suspend fun clear()
}
