/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.crypto.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.freshness.FreshnessBand
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kpt.core.model.demo.crypto.CoinDetail
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

/**
 * Locks the [CoinDetailViewModel] contract.
 *
 * The nav-parameter hand-off is the sharp edge: `coinId` arrives from the route via
 * `parametersOf(coinId)` and is handed straight to the repository. Passing the wrong one — or a
 * hardcoded default — renders a complete, plausible, entirely WRONG coin, which no render-only
 * assertion can see. [streamsTheCoinIdItWasConstructedWith] is the guard.
 *
 * Retry/refresh both route through the typed action channel rather than calling the stream
 * directly, so they are asserted to actually reach the stream's refresh trigger.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CoinDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private fun detail(id: String) = CoinDetail(
        id = id,
        name = "Bitcoin",
        symbol = "btc",
        imageUrl = "https://example.invalid/btc.png",
        currentPrice = 50_000.0,
        marketCap = 1_000_000_000L,
        marketCapRank = 1,
        priceChangePercent24h = 1.5,
        high24h = 51_000.0,
        low24h = 49_000.0,
        circulatingSupply = 19_000_000.0,
        maxSupply = 21_000_000.0,
        description = "digital gold",
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun streamsTheCoinIdItWasConstructedWith() = runTest(dispatcher) {
        val repo = FakeCryptoRepository()
        CoinDetailViewModel(coinId = "ethereum", repository = repo)

        assertEquals("ethereum", repo.lastCoinId)
    }

    @Test
    fun exposesRepositoryContentUnchanged() = runTest(dispatcher) {
        val d = detail("bitcoin")
        val repo = FakeCryptoRepository(detailState = MutableStateFlow(ScreenState.Content(d)))
        val vm = CoinDetailViewModel(coinId = "bitcoin", repository = repo)

        val state = vm.detail.state.first { it !is ScreenState.Loading }
        assertEquals(d, assertIs<ScreenState.Content<CoinDetail>>(state).data)
    }

    @Test
    fun retryAndRefreshBothReachTheStream() = runTest(dispatcher) {
        // Both actions collapse to `detail.refresh()`. Routing them through the action channel is
        // what makes them droppable — if the channel is not wired, the retry button does nothing
        // and the user is stuck on an error screen with no way out.
        val trigger = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
        val repo = FakeCryptoRepository(detailRefreshTrigger = trigger)
        val vm = CoinDetailViewModel(coinId = "bitcoin", repository = repo)
        val seen = mutableListOf<Unit>()
        backgroundScope.launch { trigger.collect { seen += it } }
        drain()

        vm.onRetry()
        drain()
        assertTrue(seen.size >= 1, "onRetry() must reach the stream's refresh trigger")

        vm.onRefresh()
        drain()
        assertTrue(seen.size >= 2, "onRefresh() must reach the stream's refresh trigger")
    }

    @Test
    fun projectsTheStreamsFreshnessRatherThanAConstant() = runTest(dispatcher) {
        // The TopAppBar indicator reads this. A `stateIn` that never re-collects — or a ViewModel
        // that hardcodes `FreshnessSignal.initial()` — leaves the badge permanently on "Initial",
        // telling the user the data is fine while it is hours stale. Feeding a DISTINCT Stale
        // signal is what separates a live projection from the initial default.
        val stale = FreshnessSignal(
            lastSyncedAt = null,
            ttl = 1.hours,
            lastError = null,
            band = FreshnessBand.Stale,
        )
        val repo = FakeCryptoRepository(
            detailState = MutableStateFlow(ScreenState.Content(detail("bitcoin"))),
            detailFreshness = MutableStateFlow(stale),
        )
        val vm = CoinDetailViewModel(coinId = "bitcoin", repository = repo)
        backgroundScope.launch { vm.freshness.collect { } }
        drain()

        assertEquals(FreshnessBand.Stale, vm.freshness.value.band)
    }
}

/**
 * Drains BOTH the `runTest` job tree and the scopes that live outside it — `viewModelScope` and
 * `backgroundScope`. `advanceUntilIdle()` alone does not start those, so a collector stays
 * unsubscribed and a replay-0 `tryEmit` is dropped with nothing to show for it; `runCurrent()`
 * does. Interleaving both, a few rounds, covers work that re-schedules itself.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun TestScope.drain() {
    repeat(3) {
        runCurrent()
        advanceUntilIdle()
    }
}
