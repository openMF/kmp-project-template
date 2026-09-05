/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.amortizationcalc

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kpt.feature.calculators.TestTags
import kpt.feature.calculators.wizard.FakeLoanRepository
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [AmortizationScreen].
 *
 * Uses the module-internal [FakeLoanRepository] (already used by
 * [kpt.feature.calculators.amortizationcalc.AmortizationViewModelTest]) and
 * passes `loanId = null` for the standalone "enter from scratch" mode.
 * Wraps in [KptTheme] and asserts the root Scaffold (RULE-KMP-COMPOSE-UITEST-001
 * CU-1..CU-3).
 */
@OptIn(ExperimentalTestApi::class)
class AmortizationScreenUiTest {

    @Test
    fun screenIsDisplayed() = runComposeUiTest {
        val viewModel = AmortizationViewModel(
            repository = FakeLoanRepository(),
            calcRepository = FakeAmortizationCalcRepository(),
            loanId = null,
        )
        setContent {
            KptTheme {
                AmortizationScreen(
                    onBackClick = {},
                    loanId = null,
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.Amortization.SCREEN).assertIsDisplayed()
    }
}
