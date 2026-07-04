/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.paging

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.fixtures.FakeNetworkMonitor
import kpt.core.base.store.infra.FakeFetchedAtRepository
import kpt.core.base.store.screen.ScreenState
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Deep-scroll-restore canary for Phase 4 of `store5-screen-state-persistence`.
 *
 * Primes a Store5 store's SourceOfTruth with four durable pages (0..3, twenty
 * items each) and constructs a [PagingScreenStream] with an
 * `initialCursorProvider` that yields a cursor at `lastPageLoaded = 3`. The
 * asserted contract:
 *
 *  - The first non-Loading emission of [PagingScreenStream.state] is a
 *    [ScreenState.Content] whose `data.size == 80` — the batch-restore path
 *    walked pages 0..3 from SoT and concatenated eighty items with **zero**
 *    network round-trips (the fetcher never runs because
 *    `store.loadPage(refresh = false)` short-circuits on populated SoT).
 *  - [PagingScreenStream.cursorFlow] settles at
 *    `lastPageLoaded = 3` after restore.
 *  - [PagingScreenStream.updateScrollPosition] propagates to `cursorFlow`
 *    without touching `lastPageLoaded` — so scroll-storm deltas do not disturb
 *    the paging cursor.
 *
 * Mirrors the fixture pattern in [LoadPageRefreshSemanticsTest] (real Store5
 * `SourceOfTruth.of` primed by a `mutableMapOf<PageKey, List<Int>>()`) so this
 * canary does not introduce a second FakeStore abstraction.
 */
class PagingCursorRestoreTest {

    private val onlineInfo = NetworkInfo(type = NetworkType.WiFi, isMetered = false)
    private val online = NetworkStatus.Available(onlineInfo)

    @Test
    fun batchRestoresFourPagesFromCursor() = runTest {
        val pageSize = 20
        val sotData = mutableMapOf<PageKey, List<Int>>()
        // Prime SoT with pages 0..3, 20 items each — every value distinct so an
        // out-of-order concatenation is caught by the size assertion + the
        // per-page ordering assertion below.
        for (p in 0..3) {
            val key = PageKey(page = p, pageSize = pageSize, query = null)
            sotData[key] = List(pageSize) { i -> p * pageSize + i }
        }
        var fetcherCalls = 0
        val store = StoreBuilder
            .from<PageKey, List<Int>, List<Int>>(
                fetcher = Fetcher.of { _ ->
                    fetcherCalls++
                    error("Fetcher must not run during cursor-restore — pages are already durable in SoT.")
                },
                sourceOfTruth = SourceOfTruth.of(
                    reader = { key -> flow { emit(sotData[key]) } },
                    writer = { key, value -> sotData[key] = value },
                ),
            )
            .build()
        val restored = PagingCursor(
            lastPageLoaded = 3,
            scrollIndex = 42,
            scrollOffset = 0,
            query = null,
        )
        val stream = store.asPagingScreenStream(
            networkMonitor = FakeNetworkMonitor(online),
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:cursor-restore",
            scope = backgroundScope,
            pageSize = pageSize,
            initialCursorProvider = { restored },
        )

        val content = stream.state.first { it is ScreenState.Content<*> }
        val contentTyped = content as ScreenState.Content<List<Int>>
        assertNotNull(contentTyped)
        assertEquals(
            expected = 80,
            actual = contentTyped.data.size,
            message = "Batch-restore must concatenate four pages of 20 items each (80 total).",
        )
        assertEquals(
            expected = (0 until 80).toList(),
            actual = contentTyped.data,
            message = "Concatenation order must be pages 0 → 3 for deterministic scroll restore.",
        )
        assertEquals(
            expected = 3,
            actual = stream.cursorFlow.value.lastPageLoaded,
            message = "cursorFlow must settle at the restored lastPageLoaded after batch restore.",
        )
        assertEquals(
            expected = 0,
            actual = fetcherCalls,
            message = "Fetcher must never run when every requested page is durable in SoT.",
        )
    }

    @Test
    fun updateScrollPositionPropagatesWithoutTouchingLastPageLoaded() = runTest {
        val pageSize = 20
        val sotData = mutableMapOf<PageKey, List<Int>>()
        sotData[PageKey(page = 0, pageSize = pageSize, query = null)] = List(pageSize) { it }
        val store = StoreBuilder
            .from<PageKey, List<Int>, List<Int>>(
                fetcher = Fetcher.of { key -> List(key.pageSize) { it } },
                sourceOfTruth = SourceOfTruth.of(
                    reader = { key -> flow { emit(sotData[key]) } },
                    writer = { key, value -> sotData[key] = value },
                ),
            )
            .build()
        val stream = store.asPagingScreenStream(
            networkMonitor = FakeNetworkMonitor(online),
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:scroll-only",
            scope = backgroundScope,
            pageSize = pageSize,
            initialCursorProvider = {
                PagingCursor(lastPageLoaded = 0, scrollIndex = 0, scrollOffset = 0, query = null)
            },
        )
        // Drain until content lands so cursorMutable has been updated by the
        // restore path.
        stream.state.first { it is ScreenState.Content<*> }
        val lastPageBefore = stream.cursorFlow.value.lastPageLoaded

        stream.updateScrollPosition(index = 50, offset = 10)

        val cursor = stream.cursorFlow.value
        assertEquals(50, cursor.scrollIndex, "scrollIndex must propagate to cursorFlow.")
        assertEquals(10, cursor.scrollOffset, "scrollOffset must propagate to cursorFlow.")
        assertEquals(
            expected = lastPageBefore,
            actual = cursor.lastPageLoaded,
            message = "updateScrollPosition must not disturb lastPageLoaded.",
        )
    }

    @Test
    fun nullCursorProviderRunsStandardInitialLoad() = runTest {
        // Regression: the pre-Phase-4 shape (no initialCursorProvider) must
        // still work — asPagingScreenStream defaults the provider to `{ null }`
        // and loadInitialPage falls through to the single-page path.
        val pageSize = 3
        val store = StoreBuilder
            .from<PageKey, List<String>>(
                fetcher = Fetcher.of { key -> List(key.pageSize) { "p${key.page}-i$it" } },
            )
            .build()
        val stream = store.asPagingScreenStream(
            networkMonitor = FakeNetworkMonitor(online),
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:no-cursor",
            scope = backgroundScope,
            pageSize = pageSize,
        )
        val content = stream.state.first { it is ScreenState.Content<*> } as ScreenState.Content<List<String>>
        assertEquals(pageSize, content.data.size)
        assertTrue(
            actual = content.data.all { it.startsWith("p0-") },
            message = "Default (no cursor) initial load must serve page 0 only.",
        )
        assertEquals(
            expected = 0,
            actual = stream.cursorFlow.value.lastPageLoaded,
            message = "cursorFlow.lastPageLoaded stays at 0 after standard initial load.",
        )
    }
}
