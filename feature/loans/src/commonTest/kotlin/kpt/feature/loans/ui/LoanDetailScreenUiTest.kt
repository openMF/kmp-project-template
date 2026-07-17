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
 * Compose Multiplatform UI test for [LoanDetailScreen].
 *
 * Renders the real screen with a fake-backed [LoanDetailViewModel] (no Koin) wrapped in
 * [KptTheme], and asserts the root scaffold test-tag is visible.
 *
 * The repo has no seeded loan so the screen stays in the Loading → Empty transition; the
 * Scaffold is composed immediately regardless of the data state (RULE-KMP-COMPOSE-UITEST-001
 * CU-1..CU-3).
 */
@OptIn(ExperimentalTestApi::class)
class LoanDetailScreenUiTest {

    @Test
    fun scaffoldIsDisplayed() = runComposeUiTest {
        val vm = LoanDetailViewModel(
            repository = FakeLoanRepository(),
            loanId = "test-loan-id",
        )
        setContent {
            KptTheme {
                LoanDetailScreen(
                    loanId = "test-loan-id",
                    onBackClick = {},
                    onEditClick = {},
                    onAmortizationClick = {},
                    viewModel = vm,
                )
            }
        }
        onNodeWithTag(TestTags.LoanDetail.SCAFFOLD).assertIsDisplayed()
    }
}
