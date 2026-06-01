/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.rates.ui

import kpt.core.store.economic.impl.InterestRateSeriesKey

/**
 * The four default FRED series shown by the B7 Interest Rate Tracker.
 *
 * Each entry is a stable [InterestRateSeriesKey] — FRED series ID + human-readable
 * label + unit + 365-day window. Forks that want to add or remove a series only
 * have to edit this list; the ViewModel + Screen are series-agnostic past it.
 *
 * FRED series catalogue: https://fred.stlouisfed.org/categories/
 */
internal object RateSeriesCatalog {
    /** Effective Federal Funds Rate — the overnight bank-to-bank lending rate. */
    val FedFunds = InterestRateSeriesKey(
        seriesId = "DFF",
        name = "Federal Funds Rate",
        unit = "%",
        days = 365,
    )

    /** Bank Prime Loan Rate — the rate U.S. banks charge their most creditworthy customers. */
    val Prime = InterestRateSeriesKey(
        seriesId = "DPRIME",
        name = "Prime Rate",
        unit = "%",
        days = 365,
    )

    /** 30-Year Fixed Mortgage Average — Freddie Mac weekly survey. */
    val Mortgage30Y = InterestRateSeriesKey(
        seriesId = "MORTGAGE30US",
        name = "30-Year Mortgage",
        unit = "%",
        days = 365,
    )

    /** 10-Year Treasury Constant Maturity Rate — benchmark long-term yield. */
    val Treasury10Y = InterestRateSeriesKey(
        seriesId = "DGS10",
        name = "10-Year Treasury",
        unit = "%",
        days = 365,
    )

    val all: List<InterestRateSeriesKey> = listOf(FedFunds, Prime, Mortgage30Y, Treasury10Y)

    /** Look up the canonical catalog entry for a [seriesId], or `null` for unknown IDs. */
    fun findBySeriesId(seriesId: String): InterestRateSeriesKey? =
        all.firstOrNull { it.seriesId == seriesId }
}
