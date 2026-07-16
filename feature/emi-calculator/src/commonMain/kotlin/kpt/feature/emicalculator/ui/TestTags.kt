/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.emicalculator.ui

/**
 * Append-only test-tag registry for the emi-calculator feature.
 * Consumed by Compose UI tests in `feature/emi-calculator/src/commonTest/`.
 * APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).
 */
object TestTags {

    /** Tags for [EmiCalculatorScreen]. */
    object EmiCalculator {
        /** Root scaffold — always rendered regardless of input state. */
        const val SCREEN: String = "emi_calculator_screen"
    }
}
