/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.bills.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kpt.feature.bills.testing.FakeBillReminderRepository
import kpt.feature.bills.testing.FakeBillReminderScheduler
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [BillRemindersListScreen] (RULE-KMP-COMPOSE-UITEST-001
 * CU-1..CU-3).
 *
 * Renders the real screen with a fake-backed [BillRemindersListViewModel] (no Koin) wrapped
 * in [KptTheme] (spacing + MaterialTheme), and asserts the FAB is displayed. The FAB lives
 * in `Scaffold.floatingActionButton` and is always present regardless of repository state,
 * making it the most stable anchor for CU-1.
 *
 * Per-screen tests live in the screen's OWN module `commonTest`, so `internal` composables
 * in `kpt.feature.bills.ui` are reachable — no visibility change needed. The fakes in
 * `kpt.feature.bills.testing` are also `internal` and visible from this same module.
 */
@OptIn(ExperimentalTestApi::class)
class BillRemindersListScreenUiTest {

    @Test
    fun fabIsDisplayed() = runComposeUiTest {
        val viewModel = BillRemindersListViewModel(
            repository = FakeBillReminderRepository(),
            scheduler = FakeBillReminderScheduler(),
        )
        setContent {
            KptTheme {
                BillRemindersListScreen(
                    onBackClick = {},
                    onAddBillClick = {},
                    onEditBillClick = {},
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.BillsList.FAB).assertIsDisplayed()
    }
}
