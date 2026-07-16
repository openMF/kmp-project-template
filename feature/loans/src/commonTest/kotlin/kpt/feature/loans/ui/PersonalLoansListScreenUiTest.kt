/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loans.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [PersonalLoansListScreen] — the harness proof that
 * `runComposeUiTest` (commonTest) executes against a real feature screen in this template.
 *
 * Renders the real screen with a fake-backed [PersonalLoansListViewModel] (no Koin) wrapped
 * in [KptTheme] (spacing + MaterialTheme), and asserts a stable `testTag` node. Per-screen
 * UI tests live in the screen's OWN module `commonTest`, so `internal` ScreenContent is
 * reachable — no visibility change needed (RULE-KMP-COMPOSE-UITEST-001 CU-1..CU-3).
 */
@OptIn(ExperimentalTestApi::class)
class PersonalLoansListScreenUiTest {

    @Test
    fun fabIsDisplayed() = runComposeUiTest {
        val viewModel = PersonalLoansListViewModel(FakeLoanRepository())
        setContent {
            KptTheme {
                PersonalLoansListScreen(
                    onBackClick = {},
                    onAddLoanClick = {},
                    onLoanClick = {},
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.LoansList.FAB).assertIsDisplayed()
    }
}
