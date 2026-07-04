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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kpt.core.base.datastore.screen.ScreenUiStateStore
import kpt.core.base.store.paging.PagingScreenStream
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.crypto.CryptoRepository
import kpt.core.model.crypto.CoinMarket
import kpt.core.base.datastore.screen.PagingCursor as DatastorePagingCursor
import kpt.core.base.store.paging.PagingCursor as StorePagingCursor

/**
 * Read-side ViewModel for [CoinMarketsScreen].
 *
 * Deep-scroll restore (AC-17):
 *  - Hands [repository] a suspend `initialCursorProvider` that reads the last
 *    persisted cursor from [screenUiStateStore] via `observe(routeKey).first()`
 *    — never blocking (Kotlin/JS and Kotlin/wasmJs have no `runBlocking`).
 *  - Subscribes to [PagingScreenStream.cursorFlow] and writes changes back to
 *    the datastore whenever `lastPageLoaded` or `query` drifts. Scroll deltas
 *    also flow through the stream's cursor but are filtered out at persistence
 *    time by [distinctUntilChanged] — Phase 2's `rememberRetainedScreenState`
 *    already handles the top-level `scrollIndex` / `scrollOffset` axis via the
 *    same [ScreenUiStateStore] with a 300 ms debounce.
 *
 * Cursor mapping. The store-level [StorePagingCursor] carries the full restore
 * tuple `{lastPageLoaded, scrollIndex, scrollOffset, query}` because
 * `core-base/store` cannot depend on `core-base/datastore` (the current
 * dependency edge runs the other way: datastore → ui → store). The
 * datastore-level [DatastorePagingCursor] declares only `{lastPageLoaded,
 * query}`, so the mapping is:
 *
 *  - Read: `DatastorePagingCursor` → `StorePagingCursor` seeds
 *    `scrollIndex/scrollOffset = 0` (scroll restore is handled entirely by
 *    `rememberRetainedScreenState`'s top-level `scrollIndex`/`scrollOffset`
 *    fields, not by the paging cursor).
 *  - Write: only `lastPageLoaded` and `query` propagate; the store-level cursor's
 *    scroll fields are informational (they let a caller without
 *    `rememberRetainedScreenState` still observe scroll deltas through the
 *    single cursorFlow).
 */
class CoinMarketsViewModel(
    private val routeKey: String,
    private val repository: CryptoRepository,
    private val screenUiStateStore: ScreenUiStateStore,
) : BaseViewModel<Unit, Nothing, CoinMarketsAction>(Unit) {

    val pagingStream: PagingScreenStream<CoinMarket> = repository.coinMarketsStream(
        scope = viewModelScope,
        pageSize = DEFAULT_PAGE_SIZE,
        initialCursorProvider = { readPersistedCursor() },
    )

    init {
        viewModelScope.launch {
            pagingStream.cursorFlow
                .map { it.lastPageLoaded to it.query }
                .distinctUntilChanged()
                .collect { (lastPageLoaded, query) ->
                    // Only write when the paging axis actually changed. Scroll deltas
                    // are already persisted by rememberRetainedScreenState (Phase 2).
                    screenUiStateStore.update(routeKey) { current ->
                        current.copy(
                            pagingCursor = DatastorePagingCursor(
                                lastPageLoaded = lastPageLoaded,
                                query = query,
                            ),
                        )
                    }
                }
        }
    }

    /**
     * Reads the last persisted [DatastorePagingCursor] for [routeKey] and lifts
     * it into a store-level [StorePagingCursor]. Returns `null` when no record
     * exists (first-time visit) or the record has no paging_cursor slot
     * populated — either case falls through the standard first-page load.
     *
     * Runs inside the paging stream's `loadInitialPage` coroutine, so
     * suspending is safe. Never call this from a non-coroutine context.
     */
    private suspend fun readPersistedCursor(): StorePagingCursor? =
        screenUiStateStore.observe(routeKey).first()?.pagingCursor?.let { stored ->
            StorePagingCursor(
                lastPageLoaded = stored.lastPageLoaded,
                scrollIndex = 0,
                scrollOffset = 0,
                query = stored.query,
            )
        }

    /** Pull-to-refresh / initial retry. Resets the paging cursor to page 0. */
    fun retry() = pagingStream.refresh()

    /**
     * Called from [CoinMarketsScreen]'s `LaunchedEffect` observing the
     * hoisted [androidx.compose.foundation.lazy.LazyListState]. Deduped
     * downstream, so it is safe to invoke at Compose recomposition frequency.
     */
    fun onScrollChanged(index: Int, offset: Int) {
        pagingStream.updateScrollPosition(index = index, offset = offset)
    }

    override fun handleAction(action: CoinMarketsAction) = Unit

    private companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}

/** Placeholder action hierarchy — the read-only screen has no dispatch surface today. */
sealed interface CoinMarketsAction
