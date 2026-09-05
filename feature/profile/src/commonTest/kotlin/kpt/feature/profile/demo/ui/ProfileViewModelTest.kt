/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.demo.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.demo.profile.ProfileRepository
import kpt.core.model.demo.profile.ProfileInfo
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Locks the [ProfileViewModel] contract: it is a straight PASS-THROUGH of the repository's
 * store-backed stream.
 *
 * That is the whole point of the class — no `stateIn`, no re-exposed `StateFlow<ScreenState<…>>`,
 * no projection — so the screen can render it with `ScreenContent(stream = viewModel.profile)`,
 * which collects with lifecycle awareness and wires `onRetry = stream::retry` itself. A future
 * change that re-introduces a projection layer here would duplicate state the stream already
 * holds, and these assertions are what catch it.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalScreenDataStreamTestingApi::class)
class ProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeProfileRepository(
        private val state: MutableStateFlow<ScreenState<ProfileInfo>>,
    ) : ProfileRepository {
        var streamCalls = 0
            private set

        override fun profileStream(scope: CoroutineScope): ScreenDataStream<ProfileInfo> {
            streamCalls++
            return screenDataStreamForTesting(state)
        }
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun exposesTheRepositoryStreamContentUnchanged() = runTest {
        val info = ProfileInfo(appDisplayName = "Kpt Test")
        val vm = ProfileViewModel(FakeProfileRepository(MutableStateFlow(ScreenState.Content(info))))

        val state = vm.profile.state.first { it != ScreenState.Loading }
        assertEquals(info, assertIs<ScreenState.Content<ProfileInfo>>(state).data)
    }

    @Test
    fun surfacesEmptyFromTheStoreRatherThanMaskingIt() = runTest {
        // A fork whose fetcher finds no signed-in user must reach the screen's Empty slot — the VM
        // does not substitute a placeholder of its own.
        val vm = ProfileViewModel(FakeProfileRepository(MutableStateFlow(ScreenState.Empty)))

        val state = vm.profile.state.first { it != ScreenState.Loading }
        assertEquals(ScreenState.Empty, state)
    }

    @Test
    fun buildsTheStreamExactlyOncePerViewModel() = runTest {
        // Pass-through, not per-collection construction: a VM that rebuilt the stream on every
        // read would drop the store's cache between collectors.
        val repo = FakeProfileRepository(MutableStateFlow(ScreenState.Content(ProfileInfo("Kpt Test"))))
        val vm = ProfileViewModel(repo)

        vm.profile.state.first { it != ScreenState.Loading }
        vm.profile.state.first { it != ScreenState.Loading }
        assertEquals(1, repo.streamCalls)
    }
}
