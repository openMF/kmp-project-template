/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.macro.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.economic.IndicatorKind
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [MacroIndicatorDetailScreen].
 *
 * A Koin-parameterized screen (countryCode + indicatorKind via [parametersOf]);
 * in tests we bypass Koin and construct the real [MacroIndicatorDetailViewModel]
 * with a [FakeMacroIndicatorsRepository] — the same pattern used by
 * [AmortizationScheduleScreenUiTest] (loanId literal → direct VM construction).
 *
 * The [FakeMacroIndicatorsRepository] emits no state into its shared flows,
 * so the screen renders in [kpt.core.base.store.screen.ScreenState.Loading] which
 * still produces the root Scaffold — the target of [TestTags.MacroIndicatorDetail.SCREEN].
 */
@OptIn(ExperimentalTestApi::class)
class MacroIndicatorDetailScreenUiTest {

    @Test
    fun screenIsDisplayed() = runComposeUiTest {
        val viewModel = MacroIndicatorDetailViewModel(
            countryCode = "US",
            indicatorKind = IndicatorKind.GDP,
            repository = FakeMacroIndicatorsRepository(),
        )
        setContent {
            KptTheme {
                MacroIndicatorDetailScreen(
                    countryCode = "US",
                    indicatorKind = IndicatorKind.GDP,
                    onBackClick = {},
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.MacroIndicatorDetail.SCREEN).assertIsDisplayed()
    }
}
