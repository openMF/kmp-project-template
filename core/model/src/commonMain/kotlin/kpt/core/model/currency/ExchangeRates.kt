/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.currency

/**
 * FX rates for one base currency on one day — the `rates` map is quote-code → rate.
 */
data class ExchangeRates(
    /** Base currency code, e.g. `USD` — every rate is quoted against it. */
    val base: String,
    /** The day these rates are for, `YYYY-MM-DD`. */
    val date: String,
    /** Quote-currency code → rate against [base]. */
    val rates: Map<String, Double>,
)

/**
 * Store key for a historical series: currency pair plus window length.
 *
 * The window is PART of the key, so widening it is a different key and a full re-fetch. That is the
 * `read_windowed_series` contract — a widened window is not a page append.
 */
data class RateHistoryKey(
    /** Base currency code. */
    val from: String,
    /** Quote currency code. */
    val to: String,
    /** Window length in days. Part of the key on purpose — see the note above. */
    val days: Int,
)

/**
 * A historical FX series for one pair over a date range.
 */
data class RateHistory(
    /** Base currency code. */
    val from: String,
    /** Quote currency code. */
    val to: String,
    /** First day in the series, `YYYY-MM-DD`. */
    val startDate: String,
    /** Last day in the series, `YYYY-MM-DD`. */
    val endDate: String,
    /** The samples, ordered by date ascending. */
    val rates: List<RatePoint>,
)

/**
 * One (date, rate) sample within a [RateHistory].
 */
data class RatePoint(
    /** The sample's day, `YYYY-MM-DD`. */
    val date: String,
    /** The rate on that day. */
    val value: Double,
)
