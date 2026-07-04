/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.ui

/**
 * Append-only test-tag registry for the home / bottom-nav-dashboard feature.
 *
 * Consumed by:
 *  - Compose UI tests in `feature/home/src/commonTest/`
 *    (`HomeScreenUiTest`).
 *  - Maestro on-device flows under `maestro/screen-state/` (notably
 *    `tab-switch.yaml`, AC-16).
 *
 * **APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).** Removing or
 * renaming any tag breaks the build (test-side compile reference) and the
 * on-device Maestro flows in the same commit. Retirement requires a sibling
 * `// uitest-tag-retire:` annotation landed with all matching callers.
 */
object TestTags {

    /** Tags for [HomeScreen] (the bottom-nav home dashboard). */
    object Home {
        /** The vertically-scrollable dashboard `Column`. */
        const val DASHBOARD_SCROLL: String = "home_dashboard_scroll"

        /** Hero card / carousel tile that navigates to the loans list. */
        const val TILE_LOANS: String = "home_tile_loans"

        /** Bills-quick card. */
        const val TILE_BILLS: String = "home_tile_bills"

        /** Rates-quick card (FRED-backed, Fed Funds + 30Y Mortgage). */
        const val TILE_RATES: String = "home_tile_rates"

        /** Exchange-rate card (USD base, Frankfurter-backed). */
        const val TILE_FX: String = "home_tile_fx"
    }
}
