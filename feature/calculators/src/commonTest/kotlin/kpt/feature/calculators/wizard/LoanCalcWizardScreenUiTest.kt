/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.wizard

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.banking.LoanCalcScenario
import kpt.feature.calculators.TestTags
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [LoanCalcWizardScreen].
 *
 * Uses the module-internal [FakeSubmitOutbox] and [FakeLoanRepository] already
 * used by [LoanCalcWizardViewModelTest]. Starts the wizard fresh (`scenarioId = null`)
 * so no resume dialog is triggered. Wraps in [KptTheme] and asserts the root
 * Scaffold is displayed (RULE-KMP-COMPOSE-UITEST-001 CU-1..CU-3).
 */
@OptIn(ExperimentalTestApi::class)
class LoanCalcWizardScreenUiTest {

    @Test
    fun screenIsDisplayed() = runComposeUiTest {
        val viewModel = LoanCalcWizardViewModel(
            outbox = FakeSubmitOutbox<LoanCalcScenario>(),
            repository = FakeLoanRepository(),
            scenarioIdArg = null,
        )
        setContent {
            KptTheme {
                LoanCalcWizardScreen(
                    onBackClick = {},
                    scenarioId = null,
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.Wizard.SCREEN).assertIsDisplayed()
    }
}
