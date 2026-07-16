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
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.banking.BillReminder
import kpt.feature.bills.testing.FakeBillReminderRepository
import kpt.feature.bills.testing.FakeBillReminderScheduler
import kpt.feature.bills.testing.InMemorySubmitOutbox
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [AddOrEditBillReminderScreen] (RULE-KMP-COMPOSE-UITEST-001
 * CU-1..CU-3).
 *
 * Renders the screen in "add" mode (`billId = null`) with a fake-backed
 * [EditBillReminderViewModel] (no Koin) wrapped in [KptTheme], and asserts the Save button
 * is displayed. The Save button is always present inside the scrollable `Column` — it may
 * be disabled (blank name / zero amount) but it is always rendered, making it a stable
 * anchor for CU-1.
 *
 * Per-screen tests live in the screen's OWN module `commonTest`, so `internal` composables
 * in `kpt.feature.bills.ui` are reachable — no visibility change needed. All fakes in
 * `kpt.feature.bills.testing` are `internal` and visible from this same module.
 */
@OptIn(ExperimentalTestApi::class)
class AddOrEditBillReminderScreenUiTest {

    @Test
    fun saveButtonExists() = runComposeUiTest {
        val viewModel = EditBillReminderViewModel(
            repository = FakeBillReminderRepository(),
            scheduler = FakeBillReminderScheduler(),
            billId = null,
            outbox = InMemorySubmitOutbox<BillReminder>(),
        )
        setContent {
            KptTheme {
                AddOrEditBillReminderScreen(
                    onBackClick = {},
                    billId = null,
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.AddOrEditBill.SAVE_BUTTON).assertExists()
    }
}
