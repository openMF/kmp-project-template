/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.rates.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [InterestRatesScreen] — the B7 rate-tracker
 * list screen.
 *
 * Renders the real screen with a fake-backed [InterestRatesViewModel] (no Koin)
 * wrapped in [KptTheme], and asserts the root scrollable column is in the
 * composition tree. The VM starts every slot in [ScreenState.Loading] so the
 * screen renders its four card placeholders immediately — no async data needed.
 *
 * Reuses [FakeEconomicRatesRepository] already defined in this module's
 * `commonTest` (RULE-KMP-COMPOSE-UITEST-001 CU-1..CU-3).
 */
@OptIn(ExperimentalTestApi::class)
class InterestRatesScreenUiTest {

    @Test
    fun ratesScrollIsDisplayed() = runComposeUiTest {
        val viewModel = InterestRatesViewModel(FakeEconomicRatesRepository())
        setContent {
            KptTheme {
                InterestRatesScreen(
                    onBackClick = {},
                    onSeriesClick = {},
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.Rates.RATES_SCROLL).assertIsDisplayed()
    }
}
