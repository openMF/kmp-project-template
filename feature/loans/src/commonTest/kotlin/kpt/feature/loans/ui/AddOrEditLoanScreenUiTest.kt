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
 * Compose Multiplatform UI test for [AddOrEditLoanScreen].
 *
 * Renders the real screen with a fake-backed [EditLoanViewModel] (no Koin) wrapped in
 * [KptTheme], and asserts the root scaffold test-tag is visible.
 *
 * Two tests cover the two code paths of [AddOrEditLoanScreen]: "add new loan" (`loanId = null`)
 * and "edit existing loan" (`loanId` non-null). In both paths the Scaffold is always composed
 * regardless of form state (RULE-KMP-COMPOSE-UITEST-001 CU-1..CU-3).
 */
@OptIn(ExperimentalTestApi::class)
class AddOrEditLoanScreenUiTest {

    /**
     * "Add new loan" path — [loanId] is null, [EditLoanViewModel] starts with a blank form.
     */
    @Test
    fun scaffoldIsDisplayedInAddMode() = runComposeUiTest {
        val vm = EditLoanViewModel(
            repository = FakeLoanRepository(),
            outbox = InMemorySubmitOutbox(),
            loanId = null,
        )
        setContent {
            KptTheme {
                AddOrEditLoanScreen(
                    loanId = null,
                    onBackClick = {},
                    onSaved = {},
                    viewModel = vm,
                )
            }
        }
        onNodeWithTag(TestTags.AddOrEditLoan.SCAFFOLD).assertIsDisplayed()
    }

    /**
     * "Edit existing loan" path — [loanId] is non-null. The repo has no matching record so the
     * form stays at its defaults; the Scaffold is still composed and tagged correctly.
     */
    @Test
    fun scaffoldIsDisplayedInEditMode() = runComposeUiTest {
        val repo = FakeLoanRepository()
        val vm = EditLoanViewModel(
            repository = repo,
            outbox = InMemorySubmitOutbox(),
            loanId = "loan-edit-id",
        )
        setContent {
            KptTheme {
                AddOrEditLoanScreen(
                    loanId = "loan-edit-id",
                    onBackClick = {},
                    onSaved = {},
                    viewModel = vm,
                )
            }
        }
        onNodeWithTag(TestTags.AddOrEditLoan.SCAFFOLD).assertIsDisplayed()
    }
}
