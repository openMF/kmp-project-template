/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.feature.watchlist.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.demo.watchlist.WatchlistRepository
import kpt.core.model.demo.watchlist.WatchlistItem

/**
 * In-memory [WatchlistRepository] for ViewModel- and UI-level tests. Backs the offline-local
 * `read_local_list` read side ([watchlistStream]) plus the `submit_offline_write` write side
 * ([contains]/[add]/[remove]) with a single [MutableStateFlow] — newest-added first, mirroring
 * the production ordering. No Room, no Store5, no Koin.
 */
internal class FakeWatchlistRepository(
    initial: List<WatchlistItem> = emptyList(),
) : WatchlistRepository {

    private val rows = MutableStateFlow(initial)
    val removeCalls = mutableListOf<String>()
    val addCalls = mutableListOf<String>()

    override fun watchlistStream(scope: CoroutineScope): ScreenDataStream<List<WatchlistItem>> =
        screenDataStreamForTesting(
            rows.map { if (it.isEmpty()) ScreenState.Empty else ScreenState.Content(it) },
        )

    override fun contains(coinId: String): Flow<Boolean> = rows.map { list -> list.any { it.coinId == coinId } }

    override suspend fun add(coinId: String) {
        addCalls += coinId
        rows.update { listOf(item(coinId)) + it.filterNot { r -> r.coinId == coinId } }
    }

    override suspend fun remove(coinId: String) {
        removeCalls += coinId
        rows.update { list -> list.filterNot { it.coinId == coinId } }
    }
}

/** [WatchlistItem] with a test-stable timestamp. */
internal fun item(coinId: String, addedAtMs: Long = 1_700_000_000_000L): WatchlistItem =
    WatchlistItem(coinId = coinId, addedAtMs = addedAtMs)
