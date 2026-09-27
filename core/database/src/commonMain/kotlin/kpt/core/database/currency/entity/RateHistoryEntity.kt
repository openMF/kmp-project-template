/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.currency.entity

import androidx.room3.Entity
import kpt.core.base.database.annotation.DbEntity

@Entity(
    tableName = "rate_history",
    primaryKeys = ["fromCurrency", "toCurrency", "startDate", "endDate"],
)
/**
 * Room row for a cached historical FX series.
 *
 * An ENTITY, not the domain model — the entity↔domain mapping lives in the Store's
 * `SourceOfTruth`, so nothing above `core/store` sees this type.
 */
@DbEntity
data class RateHistoryEntity(
    /** Base currency code. */
    val fromCurrency: String,
    /** Quote currency code. */
    val toCurrency: String,
    /** First day in the series, `YYYY-MM-DD`. */
    val startDate: String,
    /** Last day in the series, `YYYY-MM-DD`. */
    val endDate: String,
    /**
     * The series as a JSON array of (date, rate) pairs. A JSON column rather than a rows table: a series is always
     * read and replaced whole, and a widened window is a different cache key, not an append.
     */
    val ratesJson: String,
    /** Epoch millis the row was cached. */
    val fetchedAt: Long,
)
