/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.watchlist

import app.cash.turbine.test
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.demo.watchlist.impl.WatchlistRepositoryImpl
import kpt.core.data.infra.InMemoryFetchedAtRepository
import kpt.core.data.infra.onlineNetworkMonitor
import kpt.core.model.demo.watchlist.WatchlistItem
import kpt.core.store.demo.watchlist.impl.provideWatchlistStore
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Locks the wasmJs invalidation-bridge wiring for the watchlist surface — reads flow through the
 * Store5 `SourceOfTruth` + the repository's `ScreenDataStream` (`watchlistStream(scope)` →
 * `watchlistStore.asScreenStream(...)`); writes go through `notifyingWrite(WATCHLIST_TABLE)`.
 *
 * [FakeWatchlistDao] returns **cold snapshot** flows that never self-re-emit (modelling Room 3
 * alpha05 on wasmJs). The assertions can only pass because the Store reader is wrapped in
 * `daoFlow(WATCHLIST_TABLE)` and the writes publish the paired bus signal. Regression guard.
 */
class WatchlistReactiveInvalidationTest {

    private val dao = FakeWatchlistDao()
    private val repo: WatchlistRepository = WatchlistRepositoryImpl(
        watchlistStore = provideWatchlistStore(dao),
        dao = dao,
        networkMonitor = onlineNetworkMonitor(),
        fetchedAtRepository = InMemoryFetchedAtRepository(),
    )

    @Test
    fun watchlistReEmitsAfterAdd() = runTest {
        repo.watchlistStream(backgroundScope).state
            .mapNotNull { it.coinIdsOrNull() }
            .distinctUntilChanged()
            .test {
                assertEquals(emptySet(), awaitItem())
                repo.add("btc")
                assertEquals(setOf("btc"), awaitItem())
                repo.add("eth")
                assertEquals(setOf("btc", "eth"), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun watchlistReEmitsAfterRemove() = runTest {
        repo.add("btc")
        repo.watchlistStream(backgroundScope).state
            .mapNotNull { it.coinIdsOrNull() }
            .distinctUntilChanged()
            .test {
                assertEquals(setOf("btc"), awaitItem())
                repo.remove("btc")
                assertEquals(emptySet(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun containsReEmitsWhenCoinAddedThenRemoved() = runTest {
        repo.contains("btc").test {
            assertEquals(false, awaitItem())
            repo.add("btc")
            assertEquals(true, awaitItem())
            repo.remove("btc")
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

/** Extract coin ids from a `ScreenState` list (Content → ids, Empty → ∅, Loading/Error → null-skip). */
private fun ScreenState<List<WatchlistItem>>.coinIdsOrNull(): Set<String>? = when (this) {
    is ScreenState.Content -> data.map { it.coinId }.toSet()
    ScreenState.Empty -> emptySet()
    else -> null
}
