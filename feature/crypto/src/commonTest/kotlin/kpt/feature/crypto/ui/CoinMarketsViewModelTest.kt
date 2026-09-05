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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Locks the [CoinMarketsViewModel] contract — a thin holder over the repository's
 * [kpt.core.base.store.paging.PagingScreenStream], which is exactly why it needs a test: there is
 * no logic here to fail loudly, only a request contract to get silently wrong.
 *
 * The page size is the load-bearing value. It is a private `companion` constant handed to the
 * repository, so a change to it (or a refactor that stops passing it) produces a screen that still
 * scrolls and still renders — just with the wrong request shape, one row per network round trip.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CoinMarketsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun requestsAWholePageNotASingleRow() = runTest(dispatcher) {
        val repo = FakeCryptoRepository()
        CoinMarketsViewModel(repo)

        assertEquals(20, repo.lastPageSize)
    }

    @Test
    fun exposesTheRepositorysOwnStreamRatherThanARewrap() = runTest(dispatcher) {
        // The screen calls `PagingScreenContent(vm.pagingStream)`, so load-more, the footer, and
        // retry all hang off THIS instance. Re-wrapping it in the ViewModel would give the screen
        // a stream whose cursor is not the one the repository advances.
        val repo = FakeCryptoRepository()
        val vm = CoinMarketsViewModel(repo)

        assertSame(repo.lastMarketsStream, vm.pagingStream)
    }

    // NOTE: there is deliberately no "retry() re-fetches" test here. `retry()` delegates straight
    // to `PagingScreenStream.refresh()`, whose fetch runs on Store5's own dispatcher rather than
    // the test scheduler — asserting a fetch count from here races that dispatcher and passes or
    // fails on timing. The refresh semantics are covered deterministically in core-base/store
    // (`PagingScreenStreamOfflineTest`, `StorePagingSourceTest`); what belongs to THIS ViewModel is
    // the delegation above and the page size below.
}
