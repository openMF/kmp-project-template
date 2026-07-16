/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.showcase.stategallery

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kpt.feature.showcase.TestTags
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [StateGalleryScreen].
 *
 * StateGalleryScreen is a dev-only, pure-UI gallery — no ViewModel, no Store,
 * no async state. It scrolls through every [kpt.core.base.store.screen.ScreenState]
 * variant and component-scale primitives. The test renders it directly inside
 * [KptTheme] with a no-op back-click callback and asserts the always-present
 * root scaffold node identified by [TestTags.StateGallery.SCREEN].
 */
@OptIn(ExperimentalTestApi::class)
class StateGalleryScreenUiTest {

    @Test
    fun screenIsDisplayed() = runComposeUiTest {
        setContent {
            KptTheme {
                StateGalleryScreen(
                    onBackClick = {},
                )
            }
        }
        onNodeWithTag(TestTags.StateGallery.SCREEN).assertIsDisplayed()
    }
}
