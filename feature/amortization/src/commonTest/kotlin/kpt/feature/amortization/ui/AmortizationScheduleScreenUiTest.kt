/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.amortization.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [AmortizationScheduleScreen] — a Koin-parameterized
 * (loanId) read-only projection screen. Builds the real [AmortizationScheduleViewModel]
 * with the module's [FakeLoanRepository], wraps in [KptTheme], and asserts the always-
 * rendered scaffold root — stable regardless of the ScreenContent load state.
 */
@OptIn(ExperimentalTestApi::class)
class AmortizationScheduleScreenUiTest {

    @Test
    fun screenIsDisplayed() = runComposeUiTest {
        val viewModel = AmortizationScheduleViewModel(FakeLoanRepository(), loanId = "test-loan")
        setContent {
            KptTheme {
                AmortizationScheduleScreen(
                    loanId = "test-loan",
                    onBackClick = {},
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.AmortizationSchedule.SCREEN).assertIsDisplayed()
    }
}
