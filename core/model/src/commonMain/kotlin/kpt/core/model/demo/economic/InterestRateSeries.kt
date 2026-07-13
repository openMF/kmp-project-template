/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.demo.economic

import kotlinx.datetime.LocalDate

/**
 * Domain representation of an interest-rate time series sourced from FRED
 * (Federal Reserve Economic Data).
 *
 * The default consumer is the Banking Utility Toolkit's "B7 Interest Rate Tracker"
 * screen. Series identifiers ([seriesId]) align 1:1 with FRED's public series
 * catalogue — e.g. `DFF` (Effective Federal Funds Rate), `DPRIME` (Bank Prime Loan
 * Rate), `MORTGAGE30US` (30-Year Fixed Mortgage Average).
 *
 * @property seriesId FRED series identifier (case-sensitive).
 * @property name Human-readable label, e.g. "Federal Funds Rate".
 * @property current Most recent observation value (latest entry in [observations]).
 * @property unit Unit of measure: typically `"%"` for rates, `"USD"` for nominal series.
 * @property observations Time-ordered observations (typically last 365 days). Empty
 *   when the series has no published data in the requested window.
 * @property source Upstream data source — currently always `"FRED"`.
 */
data class InterestRateSeries(
    val seriesId: String,
    val name: String,
    val current: Double,
    val unit: String,
    val observations: List<RateObservation>,
    val source: String = "FRED",
)

/**
 * Single observation in an interest-rate time series.
 *
 * @property date Observation date (FRED publishes daily, weekly, monthly cadence).
 * @property value Observed rate at [date]. Always non-null at the domain layer —
 *   FRED's `"."` no-data marker is stripped during DTO -> domain mapping.
 */
data class RateObservation(
    val date: LocalDate,
    val value: Double,
)
