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
import kotlinx.coroutines.test.runTest
import kpt.core.data.demo.watchlist.impl.WatchlistRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Locks the wasmJs invalidation-bridge wiring for [WatchlistRepositoryImpl].
 *
 * [FakeWatchlistDao] returns **cold snapshot** flows that never self-re-emit, faithfully
 * modelling Room 3 alpha05 on wasmJs (the `InvalidationTracker` does not fan out to a live
 * collector after a write). The ONLY way these assertions can pass is if:
 *  - reads are wrapped in `daoFlow(WATCHLIST_TABLE) { ... }` (re-subscribes on the bus), AND
 *  - writes are wrapped in `notifyingWrite(WATCHLIST_TABLE) { ... }` (publishes the bus signal).
 *
 * On the pre-fix code — raw `dao.observeAll()` reads + raw `dao.insert()`/`dao.delete()`
 * writes — the collector would see only the initial emission and every `awaitItem()` after
 * a mutation would time out. This is the regression guard for that defect class.
 */
class WatchlistReactiveInvalidationTest {

    private val dao = FakeWatchlistDao()
    private val repo: WatchlistRepository = WatchlistRepositoryImpl(dao)

    @Test
    fun watchlistReEmitsAfterAdd() = runTest {
        repo.watchlist().test {
            assertEquals(emptyList(), awaitItem().map { it.coinId })
            repo.add("btc")
            assertEquals(setOf("btc"), awaitItem().map { it.coinId }.toSet())
            repo.add("eth")
            assertEquals(setOf("btc", "eth"), awaitItem().map { it.coinId }.toSet())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun watchlistReEmitsAfterRemove() = runTest {
        repo.add("btc")
        repo.watchlist().test {
            assertEquals(setOf("btc"), awaitItem().map { it.coinId }.toSet())
            repo.remove("btc")
            assertEquals(emptySet(), awaitItem().map { it.coinId }.toSet())
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
