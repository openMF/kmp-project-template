/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.affordability

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kpt.feature.calculators.TestTags
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [AffordabilityCalculatorScreen].
 *
 * The VM is a no-arg, pure-local-state class — no repository or outbox
 * needed. Wraps in [KptTheme] and asserts the root Scaffold is displayed
 * (RULE-KMP-COMPOSE-UITEST-001 CU-1..CU-3).
 */
@OptIn(ExperimentalTestApi::class)
class AffordabilityCalculatorScreenUiTest {

    @Test
    fun screenIsDisplayed() = runComposeUiTest {
        val viewModel = AffordabilityCalculatorViewModel()
        setContent {
            KptTheme {
                AffordabilityCalculatorScreen(
                    onBackClick = {},
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.Affordability.SCREEN).assertIsDisplayed()
    }
}
