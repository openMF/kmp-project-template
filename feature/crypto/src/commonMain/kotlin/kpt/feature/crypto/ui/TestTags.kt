/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.crypto.ui

/**
 * Append-only test-tag registry for the crypto feature.
 *
 * Consumed by:
 *  - Compose UI tests in `feature/crypto/src/commonTest/`
 *    (`CoinMarketsScreenUiTest`).
 *  - Maestro on-device flows in `maestro/screen-state/paging-restore.yaml`
 *    (AC-17 deep-scroll cursor restore).
 *
 * **APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).** The flat
 * top-level constants ([LIST], [ROW], [LOADING]) are the original Phase 04
 * baseline — they must never be renamed or removed while `CoinMarketsScreen`
 * or the shipped Compose UI test references them. The nested [CoinMarkets]
 * object adds the phase-5 namespaced accessors used by
 * `LoansJourneyUiTest`-shaped tag lookups and the paging-restore Maestro
 * flow; it aliases the flat constants where semantics overlap and adds new
 * tags (`LOAD_MORE`, `SEARCH_FIELD`) for surfaces the paging screen exposes
 * post-Phase-4.
 *
 * If a tag genuinely must be retired, add a sibling `// uitest-tag-retire:`
 * annotation and land the source deletion in the same commit as the
 * matching Maestro / UI-test callers.
 */
object TestTags {
    /** Phase-04 baseline: scrollable [PagingScreenContent] LazyColumn. */
    const val LIST: String = "coinMarkets.list"

    /** Phase-04 baseline: individual `CoinMarketRow` in the paging list. */
    const val ROW: String = "coinMarkets.row"

    /** Phase-04 baseline: load-more / footer loading indicator slot. */
    const val LOADING: String = "coinMarkets.loading"

    /**
     * Phase-05 nested namespace — mirrors the loans / home shape so Journey
     * UI tests and Maestro selectors can address crypto tags through the
     * same nested-object convention used by the rest of the app.
     */
    object CoinMarkets {
        /** Alias of [LIST] under the nested namespace. */
        const val PAGING_LIST: String = LIST

        /** Alias of [ROW] under the nested namespace. */
        const val ROW: String = "coinMarkets.row"

        /** Alias of the load-more footer tag under the nested namespace. */
        const val LOAD_MORE: String = LOADING

        /**
         * Reserved for a future search-field surface on `CoinMarketsScreen`.
         * Declared as an append-only slot so tests and Maestro flows can
         * reference it before the screen ships the input.
         */
        const val SEARCH_FIELD: String = "coinMarkets.search_field"
    }
}
