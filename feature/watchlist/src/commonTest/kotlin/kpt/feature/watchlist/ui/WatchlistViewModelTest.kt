/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.watchlist.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.model.demo.watchlist.WatchlistItem
import kpt.feature.watchlist.testing.FakeWatchlistRepository
import kpt.feature.watchlist.testing.item
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Behavior test for the `read_local_list` reference. Proves the pure-passthrough stream reflects
 * the repository (Empty vs Content, newest-first) and that `onRemove` dispatches the local write.
 */
class WatchlistViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun emptyRepository_streamSettlesToEmpty() = runTest {
        val vm = WatchlistViewModel(FakeWatchlistRepository())
        assertEquals(ScreenState.Empty, vm.watchlist.state.first { it !is ScreenState.Loading })
    }

    @Test
    fun nonEmptyRepository_streamSettlesToContentNewestFirst() = runTest {
        val vm = WatchlistViewModel(FakeWatchlistRepository(listOf(item("btc"), item("eth"))))
        val state = vm.watchlist.state.first { it is ScreenState.Content<*> }

        @Suppress("UNCHECKED_CAST")
        val content = state as ScreenState.Content<List<WatchlistItem>>
        assertEquals(listOf("btc", "eth"), content.data.map { it.coinId })
    }

    @Test
    fun onRemove_dispatchesRemoveToRepository() = runTest {
        val repo = FakeWatchlistRepository(listOf(item("btc")))
        val vm = WatchlistViewModel(repo)

        vm.onRemove("btc")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("btc"), repo.removeCalls)
    }
}
