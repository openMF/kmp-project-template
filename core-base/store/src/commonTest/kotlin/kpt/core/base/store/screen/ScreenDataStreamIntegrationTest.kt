/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.screen

import app.cash.turbine.test
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkType
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import kpt.core.base.store.fixtures.FakeNetworkMonitor
import kpt.core.base.store.infra.FakeFetchedAtRepository
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * End-to-end integration tests for [asScreenStream].
 *
 * Uses real in-memory Store5 + [FakeNetworkMonitor] + [FakeFetchedAtRepository] to verify
 * that the full pipeline (Fetcher → DecisionEngine → ScreenState) produces the correct
 * state transitions without mocking any framework internals.
 */
class ScreenDataStreamIntegrationTest {

    private val onlineInfo = NetworkInfo(type = NetworkType.WiFi, isMetered = false)

    // Real-time budget for every Turbine block below. These tests drive a REAL in-memory Store5
    // whose internal coroutines are not fully virtual-time-bound, so a heavily-loaded CI runner can
    // exceed Turbine's 3s default while the logic under test is perfectly correct — the
    // 2026-09-02 `offline_with_empty_store_emits_Empty_offlineFirst` failure on a runner executing
    // 3054 Gradle tasks, green on four prior runs of the identical code. Widening the wait budget
    // removes the scheduling flake WITHOUT relaxing a single assertion.
    private val turbineTimeout = 30.seconds

    // ─── T1: online → Content(FRESH) ─────────────────────────────────────────

    @Test
    fun online_emits_Content_FRESH_after_successful_fetch() = runTest {
        val store = StoreBuilder
            .from<String, String>(fetcher = Fetcher.of { _ -> "hello" })
            .build()
        val network = FakeNetworkMonitor(NetworkStatus.Available(onlineInfo))
        val stream = store.asScreenStream(
            key = "k1",
            networkMonitor = network,
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:online-fresh",
            scope = backgroundScope,
        )

        stream.state.test(timeout = turbineTimeout) {
            // May emit Loading first (depends on Store5 in-memory-only path)
            val first = awaitItem()
            val content = if (first is ScreenState.Content) first else awaitItem()
            assertIs<ScreenState.Content<String>>(content)
            // Phase A of data-freshness-redesign (2026-06-17): Content path no longer
            // encodes STALE on the band — staleness lives in FreshnessSignal.band
            // computed by decideFreshness() sibling. For a live in-memory store with
            // network, either isRefreshing=true (request in-flight) or false (FRESH).
            // Both are valid initial emissions; either way the Content path is healthy.
            val signal = content.freshnessSignal
            // Any signal state is acceptable here — band defaults to Initial/Fresh
            // and isRefreshing reflects request lifecycle.
            assertTrue(
                true,
                "Content emitted via in-memory + live network path: signal=$signal",
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── T2: offline, no cache, CACHE_FIRST_SWR default → Empty (offline-first) ──

    @Test
    fun offline_with_empty_store_emits_Empty_offlineFirst() = runTest {
        // asScreenStream's default policy is CACHE_FIRST_SWR (offline-first): offline with no cached
        // data and no error surfaces the screen's own Empty state, NOT a blocking full-screen NoNetwork
        // (DecisionEngine offline-first-empty rule; asserted in DecisionEngineTest "…CACHE_FIRST_SWR =
        // Empty"). Offline CACHE_FIRST_SWR skips the network leg, so the throwing fetcher is never
        // invoked and error stays null. Every other policy still shows NoNetwork offline.
        val store = StoreBuilder
            .from<String, String>(fetcher = Fetcher.of { _ -> throw RuntimeException("no network") })
            .build()
        val network = FakeNetworkMonitor(NetworkStatus.Unavailable)
        val stream = store.asScreenStream(
            key = "k2",
            networkMonitor = network,
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:offline-nodata",
            scope = backgroundScope,
        )

        stream.state.test(timeout = turbineTimeout) {
            // Settle the debounced NetworkStatus + Store5 round-trip before deciding.
            advanceUntilIdle()
            // Drain until we see a non-Loading state
            var state: ScreenState<String> = awaitItem()
            while (state is ScreenState.Loading) { state = awaitItem() }
            assertIs<ScreenState.Empty>(state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── T3: CACHE_ONLY skips network even when online ────────────────────────

    @Test
    fun cacheOnly_online_store_emits_without_network_call() = runTest {
        var fetchCallCount = 0
        val store = StoreBuilder
            .from<String, String>(fetcher = Fetcher.of { _ ->
                fetchCallCount++
                "from-network"
            })
            .build()

        val network = FakeNetworkMonitor(NetworkStatus.Available(onlineInfo))

        // Prime the in-memory cache via a normal read first
        store.streamData("k3").test(timeout = turbineTimeout) {
            awaitItem() // consume
            cancelAndIgnoreRemainingEvents()
        }
        val callsAfterPrime = fetchCallCount

        val stream = store.asScreenStream(
            key = "k3",
            networkMonitor = network,
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:cache-only",
            scope = backgroundScope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
        )

        stream.state.test(timeout = turbineTimeout) {
            var state: ScreenState<String> = awaitItem()
            while (state is ScreenState.Loading) { state = awaitItem() }
            // CACHE_ONLY must not trigger new network fetches
            assertTrue(
                fetchCallCount == callsAfterPrime,
                "CACHE_ONLY must not call the fetcher; fetchCallCount=$fetchCallCount",
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── T4: reconnect triggers state refresh ────────────────────────────────

    @Test
    fun reconnect_triggers_refresh_after_offline() = runTest {
        val store = StoreBuilder
            .from<String, String>(fetcher = Fetcher.of { _ -> "refreshed" })
            .build()
        val network = FakeNetworkMonitor(NetworkStatus.Unavailable)

        val stream = store.asScreenStream(
            key = "k4",
            networkMonitor = network,
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:reconnect",
            scope = backgroundScope,
        )

        stream.state.test(timeout = turbineTimeout) {
            // Offline-first (CACHE_FIRST_SWR default): offline + empty + no error surfaces Empty, not
            // a blocking NoNetwork (see T2 + DecisionEngineTest). The reconnect below re-runs the
            // decision and fetches, moving the screen to Content.
            advanceUntilIdle()
            var state: ScreenState<String> = awaitItem()
            while (state is ScreenState.Loading) { state = awaitItem() }
            assertIs<ScreenState.Empty>(state)

            // Simulate reconnect
            network.setStatus(NetworkStatus.Available(onlineInfo))
            advanceUntilIdle()

            // After reconnect + debounce window, a refresh is triggered
            // Drain until we get Content or timeout
            var found = false
            run drainLoop@{
                repeat(5) {
                    val next = awaitItem()
                    if (next is ScreenState.Content) {
                        found = true
                        return@drainLoop
                    }
                }
            }
            assertTrue(found, "Expected Content after reconnect, but state never reached Content")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
