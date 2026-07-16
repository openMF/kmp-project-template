/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.macro.ui

/**
 * Append-only test-tag registry for the macro feature.
 * Consumed by Compose UI tests in `feature/macro/src/commonTest/`.
 * APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).
 */
object TestTags {

    /** Tags for [CountryMacroScreen]. */
    object CountryMacro {
        /** Root scaffold — always rendered regardless of load state. */
        const val SCREEN: String = "country_macro_screen"
    }

    /** Tags for [CountryPickerScreen]. */
    object CountryPicker {
        /** Root scaffold — always rendered regardless of load state. */
        const val SCREEN: String = "country_picker_screen"
    }

    /** Tags for [MacroIndicatorDetailScreen]. */
    object MacroIndicatorDetail {
        /** Root scaffold — always rendered regardless of load state. */
        const val SCREEN: String = "macro_indicator_detail_screen"
    }
}
