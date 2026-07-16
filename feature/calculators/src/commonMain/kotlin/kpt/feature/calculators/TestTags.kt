/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators

/**
 * Append-only test-tag registry for the calculators feature.
 *
 * Consumed by Compose UI tests in `feature/calculators/src/commonTest/`.
 * **APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).**
 * Removing or renaming any constant breaks the build (test-side compile
 * reference). Add new tags at the bottom. If a tag genuinely must be retired,
 * add a sibling `// uitest-tag-retire: <reason>` annotation.
 */
object TestTags {

    /** Tags for [kpt.feature.calculators.affordability.AffordabilityCalculatorScreen]. */
    object Affordability {
        /** Root scaffold — always rendered regardless of input state. */
        const val SCREEN: String = "calc_affordability_screen"
    }

    /** Tags for [kpt.feature.calculators.amortization.AmortizationScreen]. */
    object Amortization {
        /** Root scaffold — always rendered regardless of input state. */
        const val SCREEN: String = "calc_amortization_screen"
    }

    /** Tags for [kpt.feature.calculators.comparison.LoanComparisonScreen]. */
    object Comparison {
        /** Root scaffold — always rendered regardless of input state. */
        const val SCREEN: String = "calc_comparison_screen"
    }

    /** Tags for [kpt.feature.calculators.wizard.LoanCalcWizardScreen]. */
    object Wizard {
        /** Root scaffold — always rendered regardless of wizard step. */
        const val SCREEN: String = "calc_wizard_screen"
    }
}
