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
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [CountryMacroScreen].
 *
 * Builds the real [CountryMacroViewModel] with a [FakeMacroIndicatorsRepository]
 * (returns persistent Loading streams — no network, no Koin), wraps the screen in
 * [KptTheme], and asserts the always-rendered scaffold root is displayed.
 *
 * The assertion targets [TestTags.CountryMacro.SCREEN] which is applied to the
 * outermost [androidx.compose.material3.Scaffold] modifier — rendered in every
 * load state (Loading / Error / Content).
 */
@OptIn(ExperimentalTestApi::class)
class CountryMacroScreenUiTest {

    @Test
    fun screenIsDisplayed() = runComposeUiTest {
        val viewModel = CountryMacroViewModel(
            initialCountryCode = "US",
            repository = FakeMacroIndicatorsRepository(),
        )
        setContent {
            KptTheme {
                CountryMacroScreen(
                    countryCode = "US",
                    onBackClick = {},
                    onPickCountry = {},
                    onOpenIndicator = {},
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.CountryMacro.SCREEN).assertIsDisplayed()
    }
}
