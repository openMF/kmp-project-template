/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.currencyrates.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [RateHistoryScreen] — verifies the root
 * [Scaffold] node renders in the in-process `runComposeUiTest` harness
 * (RULE-KMP-COMPOSE-UITEST-001 CU-1..CU-3).
 *
 * Constructs [RateHistoryViewModel] with the existing [FakeCurrencyRepository]
 * test double (no Koin, no device) and wraps the screen in [KptTheme].
 * The screen starts in [kpt.core.base.store.screen.ScreenState.Loading] which
 * still renders the Scaffold root — so the tag assertion is stable from the
 * very first frame.
 */
@OptIn(ExperimentalTestApi::class)
class RateHistoryScreenUiTest {

    @Test
    fun rootScaffoldIsDisplayed() = runComposeUiTest {
        val viewModel = RateHistoryViewModel(FakeCurrencyRepository())
        setContent {
            KptTheme {
                RateHistoryScreen(
                    onBackClick = {},
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.RateHistory.ROOT).assertIsDisplayed()
    }
}
