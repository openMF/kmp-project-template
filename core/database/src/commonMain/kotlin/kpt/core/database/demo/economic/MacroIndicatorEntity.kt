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

import androidx.room3.Entity

/**
 * Persistent row for a single year-level observation of a country macro indicator.
 *
 * Stored in the `macro_indicator` table (v13+). Mirrors the shape of
 * [InterestRateSeriesEntity]: one row per observation rather than a blob per series, so a
 * partial refresh replaces only the years it actually returned.
 *
 * This store used to be in-memory only ([org.mobilenativefoundation.store.store5.StoreBuilder]
 * with no `SourceOfTruth`), which meant the cache died with the process and every cold start
 * refetched — the one demo network store that was not read cache-first across a restart.
 *
 * @property countryCode ISO 3166-1 alpha-2 country code, e.g. `"US"`.
 * @property countryName Human-readable country name from the World Bank metadata.
 * @property indicator [kpt.core.model.demo.economic.IndicatorKind] name, e.g. `"GDP"`.
 * @property year 4-digit calendar year (UTC) — the World Bank publishes annual data only.
 * @property value Observed value, or `null` when the World Bank reports none (data sparsity is
 *   common; UI renders missing values as "—", never `0`).
 * @property source Upstream data source — currently always `"World Bank Open Data"`.
 * @property updatedAt Local cache-write timestamp in epoch-milliseconds.
 */
@Entity(tableName = "macro_indicator", primaryKeys = ["countryCode", "indicator", "year"])
data class MacroIndicatorEntity(
    val countryCode: String,
    val indicator: String,
    val year: Int,
    val countryName: String,
    val value: Double?,
    val source: String,
    val updatedAt: Long = 0L,
)
