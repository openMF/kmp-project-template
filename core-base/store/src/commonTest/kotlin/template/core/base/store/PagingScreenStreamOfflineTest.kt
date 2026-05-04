/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.store

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkChangeEvent
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Verifies the offline-guard contract added to [PagingScreenStream] in
 * PLAN-fw-260504-paging-offline-guard-and-screen-taxonomy §1.2.
 *
 * The guard re-types a [StorePageResult.Error] as [OfflineException] when the
 * network is offline at error time, so [LoadMoreFooter] and [DecisionEngine]
 * route the failure through the no-network treatment instead of generic error UI.
 */
class PagingScreenStreamOfflineTest {

    private val onlineInfo = NetworkInfo(type = NetworkType.WiFi, isMetered = false)

    @Test
    fun loadNextPage_offline_setsOfflineException() = runTest {
        val networkMonitor = FakeNetworkMonitor(NetworkStatus.Unavailable)
        val failingStore = StoreBuilder
            .from<PageKey, List<String>>(
                fetcher = Fetcher.of { _ -> throw RuntimeException("network down") },
            )
            .build()
        val stream = failingStore.asPagingScreenStream(
            networkMonitor = networkMonitor,
            scope = backgroundScope,
            pageSize = 5,
        )
        // Wait past the initial load (it also fails offline)
        stream.loadMoreError.firstNonNullValue()

        // First load also returned offline (no SoT). hasMore is still true (default),
        // and we'll attempt loadNextPage which should fail-fast as OfflineException.
        stream.loadNextPage()
        val err = stream.loadMoreError.firstNonNullValue()
        assertIs<OfflineException>(
            err,
            "loadNextPage offline must re-type the error as OfflineException for category-aware UI.",
        )
    }

    @Test
    fun loadNextPage_online_propagatesOriginalError() = runTest {
        val networkMonitor = FakeNetworkMonitor(NetworkStatus.Available(onlineInfo))
        val originalError = IllegalStateException("server fault")
        val failingStore = StoreBuilder
            .from<PageKey, List<String>>(
                fetcher = Fetcher.of { _ -> throw originalError },
            )
            .build()
        val stream = failingStore.asPagingScreenStream(
            networkMonitor = networkMonitor,
            scope = backgroundScope,
            pageSize = 5,
        )
        stream.loadMoreError.firstNonNullValue()
        stream.loadNextPage()
        val err = stream.loadMoreError.firstNonNullValue()
        assertEquals(
            originalError.message, err.message,
            "Online: original error message must be preserved (not re-typed).",
        )
        assertTrue(
            err !is OfflineException,
            "Online failures must NOT be re-typed as OfflineException.",
        )
    }

    @Test
    fun loadInitialPage_online_succeedsThroughFetcher() = runTest {
        // Sanity check that the offline-guard refactor didn't break the happy path.
        val networkMonitor = FakeNetworkMonitor(NetworkStatus.Available(onlineInfo))
        val store = StoreBuilder
            .from<PageKey, List<String>>(
                fetcher = Fetcher.of { key -> List(key.pageSize) { "p${key.page}-i$it" } },
            )
            .build()
        val stream = store.asPagingScreenStream(
            networkMonitor = networkMonitor,
            scope = backgroundScope,
            pageSize = 3,
        )
        // Wait until the initial load completes (isLoadingMore stays false; isInitialLoading flips off).
        // We can sample the loadMoreError to know the load attempt finished — null means success.
        val errorAfterInit = stream.loadMoreError.first()
        assertNull(errorAfterInit, "Online happy path must produce no error.")
    }

    @Test
    fun loadInitialPage_success_propagatesFetchedAtToScreenState() = runTest {
        // Verifies the fetchedAt tracking added in §4: a successful page load records
        // a wall-clock instant that flows through into ScreenState.Content.fetchedAt
        // so DataFreshnessIndicator can render "Updated Xs ago".
        val networkMonitor = FakeNetworkMonitor(NetworkStatus.Available(onlineInfo))
        val store = StoreBuilder
            .from<PageKey, List<String>>(
                fetcher = Fetcher.of { key -> List(key.pageSize) { "i$it" } },
            )
            .build()
        val stream = store.asPagingScreenStream(
            networkMonitor = networkMonitor,
            scope = backgroundScope,
            pageSize = 3,
        )
        // Wait for state to become Content (first emission after initial load completes).
        val contentState = stream.state.first { it is ScreenState.Content<*> } as ScreenState.Content<List<String>>
        assertNotNull(
            contentState.fetchedAt,
            "Successful page load must set fetchedAt so DataFreshnessIndicator shows the timestamp.",
        )
    }
}

/**
 * Suspends until the StateFlow emits a non-null value, then returns it.
 * Useful for tests that need to wait for a fire-and-forget coroutine to set an error.
 */
private suspend fun <T : Any> StateFlow<T?>.firstNonNullValue(): T = first { it != null }!!

/** Fake NetworkMonitor exposing a single static [NetworkStatus] for deterministic tests. */
private class FakeNetworkMonitor(status: NetworkStatus) : NetworkMonitor {
    private val state = MutableStateFlow(status)
    override val networkStatus: StateFlow<NetworkStatus> = state.asStateFlow()
    override val isOnline: StateFlow<Boolean> =
        MutableStateFlow(status is NetworkStatus.Available).asStateFlow()
    override val networkChanges: SharedFlow<NetworkChangeEvent> =
        MutableSharedFlow<NetworkChangeEvent>().asSharedFlow()
    override fun close() = Unit
}
