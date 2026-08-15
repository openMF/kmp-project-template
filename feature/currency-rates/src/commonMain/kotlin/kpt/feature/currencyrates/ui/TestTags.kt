/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.currencyrates.ui

/**
 * Append-only test-tag registry for the currency-rates feature.
 *
 * Consumed by:
 *  - Compose UI tests in `feature/currency-rates/src/commonTest/`
 *    (`CurrencyRatesScreenUiTest`, `RateHistoryScreenUiTest`).
 *  - Maestro on-device flows under `maestro/screen-state/` — Maestro
 *    resolves these string constants via `assertVisible: { id: "..." }`
 *    matchers on Android's `Modifier.testTag(...)` semantics.
 *
 * **APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).** Removing or
 * renaming any string breaks the build (test-side compile reference) and any
 * on-device Maestro flows in the same commit. Add new tags at the bottom of
 * each nested object. If a tag genuinely must be retired, add a sibling
 * `// uitest-tag-retire: <reason>` annotation.
 */
object TestTags {

    /** Tags for [CurrencyRatesScreen]. */
    object CurrencyRates {
        /** Root [Scaffold] of the currency-rates list screen. */
        const val ROOT: String = "currency_rates_root"

        /**
         * The currency-converter card — the NETWORK_ONLY/CACHE_ONLY spot-rate
         * showcase surface (renders `CurrencyRatesViewModel.spotConversionRate`).
         */
        const val CONVERTER: String = "currency_rates_converter"
    }

    /** Tags for [RateHistoryScreen]. */
    object RateHistory {
        /** Root [Scaffold] of the rate-history screen. */
        const val ROOT: String = "rate_history_root"
    }
}
