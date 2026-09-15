/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.showcase.transitions

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kpt.feature.showcase.TestTags
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [TransitionGalleryScreen].
 *
 * TransitionGalleryScreen is a dev-only, pure-UI gallery — no ViewModel, no
 * Store, no async state. It lists every [TransitionVariant] as a tappable
 * button. The test renders it directly inside [KptTheme] with no-op callbacks
 * and asserts the always-present root scaffold node identified by
 * [TestTags.TransitionGallery.SCREEN].
 */
@OptIn(ExperimentalTestApi::class)
class TransitionGalleryScreenUiTest {

    // `v2.runComposeUiTest`, not the v1 overload: v1 lets effects run on a background dispatcher
    // while the test thread observes layout/semantics, and this screen's transition animations lose
    // that race intermittently — `IllegalArgumentException: Detected multithreaded access to
    // SnapshotStateObserver … previousThreadId=35, currentThread=DefaultDispatcher-worker-1`.
    // Observed failing ~1 run in 4 both locally and in CI, on a module no recent change touched.
    // v2 defaults to `StandardTestDispatcher`, which queues those coroutines onto the test thread
    // instead of executing them immediately elsewhere, so the observation can no longer straddle
    // two threads. This is the replacement the v1 deprecation warning itself names.
    @Test
    fun screenIsDisplayed() = runComposeUiTest {
        setContent {
            KptTheme {
                TransitionGalleryScreen(
                    onNavigateToDemo = {},
                    onBackClick = {},
                )
            }
        }
        onNodeWithTag(TestTags.TransitionGallery.SCREEN).assertIsDisplayed()
    }
}
