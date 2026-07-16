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

/**
 * Append-only test-tag registry for the interest-rates feature.
 *
 * Consumed by:
 *  - Compose UI tests in `feature/rates/src/commonTest/`
 *    (`InterestRatesScreenUiTest`, `InterestRateDetailScreenUiTest`).
 *  - Maestro on-device flows under `maestro/` — Maestro resolves these string
 *    constants via `assertVisible: { id: "..." }` matchers on Android's
 *    `Modifier.testTag(...)` semantics.
 *
 * **APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).** Removing or
 * renaming any string breaks the build (test-side compile reference) and any
 * on-device Maestro flows in the same commit. Add new tags at the bottom of
 * each nested object. If a tag genuinely must be retired, add a sibling
 * `// uitest-tag-retire: <reason>` annotation and land the source deletion in
 * the same commit as the matching UI-test / Maestro callers.
 */
object TestTags {

    /** Tags for [InterestRatesScreen] (the B7 rate-tracker list screen). */
    object Rates {
        /**
         * The root vertically-scrollable [Column] that hosts the four rate-row
         * cards. Always rendered regardless of per-row [ScreenState] — suitable
         * as the stable root assertion node in Compose UI tests.
         */
        const val RATES_SCROLL: String = "rates_scroll"
    }

    /** Tags for [InterestRateDetailScreen] (the per-series detail screen). */
    object RateDetail {
        /**
         * The root [Scaffold] surface of the detail screen. Tagged via the
         * `modifier` parameter so it is always in the composition tree
         * irrespective of the current [ScreenState] (Loading / Error / Content).
         */
        const val DETAIL_ROOT: String = "rate_detail_root"
    }
}
