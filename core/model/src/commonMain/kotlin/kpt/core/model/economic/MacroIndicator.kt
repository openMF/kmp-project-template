/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.economic

/**
 * Domain representation of a country-level macro indicator sourced from the
 * World Bank Open Data API.
 *
 * The default consumer is the Banking Utility Toolkit's "B8 Country Macro Snapshot"
 * screen. Country identifiers ([countryCode]) use ISO 3166-1 alpha-2 codes (e.g.
 * `US`, `IN`, `DE`). The World Bank also accepts alpha-3 (`USA`, `IND`, `DEU`)
 * — both are forwarded through unchanged.
 *
 * @property countryCode ISO 3166-1 alpha-2 country code, e.g. `"US"`.
 * @property countryName Human-readable country name from the World Bank metadata.
 * @property indicator Which macro series this row represents — see [IndicatorKind].
 * @property observations Per-year observations, ordered ascending by [IndicatorObservation.year].
 *   Empty when the World Bank has no published data for the requested span.
 * @property source Upstream data source — currently always `"World Bank Open Data"`.
 */
data class MacroIndicator(
    val countryCode: String,
    val countryName: String,
    val indicator: IndicatorKind,
    val observations: List<IndicatorObservation>,
    val source: String = "World Bank Open Data",
)

/**
 * Single year-level macro observation.
 *
 * @property year 4-digit calendar year (UTC). World Bank publishes annual data only.
 * @property value Observed value at [year], or `null` when the World Bank reports
 *   `null` (data sparsity is common — many countries don't report every indicator
 *   every year). UI should render missing values as "—" not "0".
 */
data class IndicatorObservation(
    val year: Int,
    val value: Double?,
)

/**
 * Macro indicators surfaced by the toolkit. Each kind maps to a stable World Bank
 * indicator code via [worldBankCode].
 *
 * Add new kinds here when new indicators are needed — the World Bank catalogue has
 * ~1,500 indicators, but the toolkit only exposes the five most-used.
 */
enum class IndicatorKind(val worldBankCode: String) {
    /** Gross Domestic Product (current US$). */
    GDP("NY.GDP.MKTP.CD"),

    /** Consumer Price Index inflation, annual %. */
    INFLATION_CPI("FP.CPI.TOTL.ZG"),

    /** Unemployment, total (% of total labor force) — ILO modeled estimate. */
    UNEMPLOYMENT("SL.UEM.TOTL.ZS"),

    /** GDP per capita (current US$). */
    GDP_PER_CAPITA("NY.GDP.PCAP.CD"),

    /** GINI index — income inequality. */
    GINI("SI.POV.GINI"),
    ;

    companion object {
        /**
         * Resolve a [IndicatorKind] from a World Bank indicator code, or `null` when
         * unknown. The DTO mapper uses this to attribute a response to the right
         * enum constant.
         */
        fun fromWorldBankCode(code: String): IndicatorKind? =
            entries.firstOrNull { it.worldBankCode == code }
    }
}
