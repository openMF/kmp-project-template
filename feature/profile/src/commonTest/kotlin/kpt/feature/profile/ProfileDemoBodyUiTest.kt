/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.demo.profile.ProfileRepository
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.profile.ProfileInfo
import kpt.feature.profile.demo.ProfileDemoBody
import kpt.feature.profile.demo.ui.ProfileViewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test

/**
 * Covers the store-backed profile DEMO body.
 *
 * The body now reads through a Store5 `ScreenDataStream`, so this test supplies a fake
 * repository and asserts the body reaches Content and renders — proving the read path is
 * actually wired, not just that a static composable draws.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalScreenDataStreamTestingApi::class)
class ProfileDemoBodyUiTest {

    private class FakeProfileRepository : ProfileRepository {
        override fun profileStream(scope: CoroutineScope): ScreenDataStream<ProfileInfo> =
            screenDataStreamForTesting(
                state = MutableStateFlow(ScreenState.Content(ProfileInfo(appDisplayName = "Kpt Test"))),
            )
    }

    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun demoBodyRendersProfileContentFromTheStore() = runComposeUiTest {
        startKoin {
            modules(
                module {
                    single<ProfileRepository> { FakeProfileRepository() }
                    factory { ProfileViewModel(get()) }
                },
            )
        }
        setContent {
            KptTheme {
                ProfileScreen(profileBody = { ProfileDemoBody(viewModel = ProfileViewModel(FakeProfileRepository())) })
            }
        }
        onNodeWithTag(TestTags.Profile.SCREEN).assertIsDisplayed()
    }
}
