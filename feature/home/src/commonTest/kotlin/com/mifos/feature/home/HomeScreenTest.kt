/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package com.mifos.feature.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import org.mifos.feature.home.HomeScreen
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class HomeScreenTest {
    @Test
    fun shouldShowHomeScreenText() = runComposeUiTest {
        setContent {
            HomeScreen()
        }
        onNodeWithText("Home Screen").assertIsDisplayed()
    }
}
