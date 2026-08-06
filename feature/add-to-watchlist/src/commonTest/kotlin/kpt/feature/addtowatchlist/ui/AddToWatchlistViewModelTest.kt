/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.addtowatchlist.ui

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.data.demo.watchlist.WatchlistRepository
import kpt.core.model.demo.watchlist.WatchlistItem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Behavior test for the `submit_offline_write` reference. Proves the star toggle routes to the
 * correct repository write based on current membership, and that `isTracked` re-emits after the
 * write (the offline-write invalidation contract), via an in-memory fake.
 */
class AddToWatchlistViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun untrackedCoin_onToggle_addsAndStarFillsToTracked() = runTest {
        val repo = FakeWatchlistRepository()
        val vm = AddToWatchlistViewModel(repository = repo, coinId = "btc")

        vm.isTracked.test {
            assertEquals(false, awaitItem()) // seed: not tracked
            vm.onToggle()
            assertEquals(true, awaitItem()) // contains() re-emits after add()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf("btc"), repo.addCalls)
        assertTrue(repo.removeCalls.isEmpty())
    }

    @Test
    fun trackedCoin_onToggle_removesAndStarClearsToUntracked() = runTest {
        val repo = FakeWatchlistRepository(initiallyTracked = setOf("eth"))
        val vm = AddToWatchlistViewModel(repository = repo, coinId = "eth")

        vm.isTracked.test {
            // stateIn seeds `false` (its initialValue), THEN contains() emits the real tracked=true.
            assertEquals(false, awaitItem()) // seed (initialValue)
            assertEquals(true, awaitItem()) // real membership settles to tracked
            vm.onToggle()
            assertEquals(false, awaitItem()) // contains() re-emits after remove()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf("eth"), repo.removeCalls)
        assertTrue(repo.addCalls.isEmpty())
    }

    @Test
    fun toggle_isIdempotentPerDirection_addThenRemoveReturnsToUntracked() = runTest {
        val repo = FakeWatchlistRepository()
        val vm = AddToWatchlistViewModel(repository = repo, coinId = "sol")

        vm.isTracked.test {
            assertEquals(false, awaitItem())
            vm.onToggle() // add
            assertEquals(true, awaitItem())
            vm.onToggle() // remove
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf("sol"), repo.addCalls)
        assertEquals(listOf("sol"), repo.removeCalls)
    }
}

/** In-memory [WatchlistRepository] — only the `contains`/`add`/`remove` surface the VM uses. */
private class FakeWatchlistRepository(
    initiallyTracked: Set<String> = emptySet(),
) : WatchlistRepository {

    private val tracked = MutableStateFlow(initiallyTracked)
    val addCalls = mutableListOf<String>()
    val removeCalls = mutableListOf<String>()

    override fun contains(coinId: String): Flow<Boolean> = tracked.map { coinId in it }

    override suspend fun add(coinId: String) {
        addCalls += coinId
        tracked.update { it + coinId }
    }

    override suspend fun remove(coinId: String) {
        removeCalls += coinId
        tracked.update { it - coinId }
    }

    override fun watchlistStream(scope: CoroutineScope): ScreenDataStream<List<WatchlistItem>> =
        error("watchlistStream is the read-side feature's concern; not used by AddToWatchlistViewModel")
}
