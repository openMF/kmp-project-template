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

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [EmiCalculatorScreen] — a pure-local-state
 * Store5-backed (MEMORY_ONLY) calculator screen. Builds the real [EmiCalculatorViewModel]
 * over [FakeEmiCalculatorRepository] (the real EMI math, faked Store plumbing), wraps in
 * [KptTheme], and asserts the always-rendered scaffold root — stable regardless of
 * which input values are set.
 */
@OptIn(ExperimentalTestApi::class)
class EmiCalculatorScreenUiTest {

    @Test
    fun screenIsDisplayed() = runComposeUiTest {
        val viewModel = EmiCalculatorViewModel(FakeEmiCalculatorRepository())
        setContent {
            KptTheme {
                EmiCalculatorScreen(
                    onBackClick = {},
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.EmiCalculator.SCREEN).assertIsDisplayed()
    }
}
