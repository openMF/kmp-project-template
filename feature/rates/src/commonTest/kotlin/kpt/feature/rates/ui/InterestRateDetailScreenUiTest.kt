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
 * Compose Multiplatform UI test for [InterestRateDetailScreen] — the per-series
 * detail screen.
 *
 * Renders the real screen with a fake-backed [InterestRateDetailViewModel]
 * (no Koin) wrapped in [KptTheme], and asserts the root [Scaffold] surface is
 * in the composition tree. The VM keeps [ScreenState.Loading] by default so
 * the screen renders the framework's loading state inside the Scaffold —
 * no async data emission is needed to assert presence of the root node.
 *
 * Reuses [FakeEconomicRatesRepository] already defined in this module's
 * `commonTest` (RULE-KMP-COMPOSE-UITEST-001 CU-1..CU-3).
 */
@OptIn(ExperimentalTestApi::class)
class InterestRateDetailScreenUiTest {

    @Test
    fun detailRootIsDisplayed() = runComposeUiTest {
        val viewModel = InterestRateDetailViewModel(
            seriesId = "DFF",
            repository = FakeEconomicRatesRepository(),
        )
        setContent {
            KptTheme {
                InterestRateDetailScreen(
                    seriesId = "DFF",
                    onBackClick = {},
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.RateDetail.DETAIL_ROOT).assertIsDisplayed()
    }
}
