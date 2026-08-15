/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [SettingsScreen].
 *
 * SettingsScreen is a pure-UI screen — all dialog state is managed with
 * [rememberSaveable] and [rememberAnalyticsHelper] internally; no ViewModel is
 * needed. The test renders it directly inside [KptTheme] with no-op callbacks
 * and asserts the always-present root scaffold node identified by
 * [TestTags.Settings.SCREEN].
 */
@OptIn(ExperimentalTestApi::class)
class SettingsScreenUiTest {

    @Test
    fun screenIsDisplayed() = runComposeUiTest {
        setContent {
            KptTheme {
                SettingsScreen(
                    onBackClick = {},
                    onSyncAndDraftsClick = {},
                )
            }
        }
        onNodeWithTag(TestTags.Settings.SCREEN).assertIsDisplayed()
    }
}
