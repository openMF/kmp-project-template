/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.demo.economic

import androidx.room3.Entity

/**
 * Persistent row for a single data-point in an interest-rate time series.
 *
 * Stored in the `interest_rate_series` table (v10+). Each row represents one
 * (date, value) observation for a named series such as the FRED federal funds
 * rate or the 30-year mortgage rate.
 *
 * @property seriesId FRED series identifier (e.g. "FEDFUNDS", "MORTGAGE30US").
 * @property date Observation date in ISO-8601 format ("YYYY-MM-DD").
 * @property value Observed rate value.
 * @property updatedAt Local cache-write timestamp in epoch-milliseconds.
 */
@Entity(tableName = "interest_rate_series", primaryKeys = ["seriesId", "date"])
data class InterestRateSeriesEntity(
    val seriesId: String,
    val date: String,
    val value: Double,
    val updatedAt: Long = 0L,
)
