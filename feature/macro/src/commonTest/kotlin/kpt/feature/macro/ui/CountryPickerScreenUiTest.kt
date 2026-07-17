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
 * Compose Multiplatform UI test for [CountryPickerScreen].
 *
 * [CountryPickerViewModel] is pure-local (no Store, no network, no Koin
 * parameter injection) so the real ViewModel can be constructed with its
 * no-arg constructor. No fake repository is needed. Wraps the screen in
 * [KptTheme] and asserts the always-rendered scaffold root.
 *
 * The assertion targets [TestTags.CountryPicker.SCREEN] which is applied to the
 * outermost [androidx.compose.material3.Scaffold] modifier.
 */
@OptIn(ExperimentalTestApi::class)
class CountryPickerScreenUiTest {

    @Test
    fun screenIsDisplayed() = runComposeUiTest {
        val viewModel = CountryPickerViewModel()
        setContent {
            KptTheme {
                CountryPickerScreen(
                    onBackClick = {},
                    onCountryPicked = {},
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.CountryPicker.SCREEN).assertIsDisplayed()
    }
}
