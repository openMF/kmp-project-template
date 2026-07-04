/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.retained

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kpt.core.base.datastore.screen.PagingCursor
import kpt.core.base.datastore.screen.ScreenUiState
import kpt.core.base.datastore.screen.ScreenUiStateStore
import org.koin.compose.koinInject

/**
 * The single feature entry point for durable per-screen UI state (GOAL AC-8).
 *
 * Composes [rememberSaveable] with the DI-wired [ScreenUiStateStore] so a screen
 * survives:
 *  - **config-change** (rotation, dark-mode flip) — via [rememberSaveable] +
 *    [LazyListState.Saver];
 *  - **navigation-away-then-back** — via the store's persisted read on first
 *    composition of a new `routeKey`;
 *  - **full OS process-death / app-kill** — via the debounced write-through to
 *    [Settings] that fires on every scroll delta.
 *
 * Feature adoption is a single line:
 * ```
 * val retained = rememberRetainedScreenState(routeKey = "loans:list")
 * PagingScreenContent(pagingStream, onRetry = ::retry, retainedState = retained) { ... }
 * ```
 *
 * @param routeKey stable identity for this screen instance (e.g. `"loans:list"` or
 *   `"loans:detail:${loanId}"`). Two composables with the same `routeKey` share
 *   the same persisted record.
 * @param store optional override; defaults to the DI-bound singleton.
 */
@Composable
fun rememberRetainedScreenState(
    routeKey: String,
    store: ScreenUiStateStore = koinInject(),
): RetainedScreenState {
    val listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState(firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 0)
    }

    // Single effect: restore on first composition of routeKey, then persist scroll
    // deltas through the store's debounce. Serial ordering (restore → collect)
    // prevents an initial (0, 0) overwrite of a previously-persisted position.
    LaunchedEffect(routeKey, listState, store) {
        val stored = store.observe(routeKey).firstOrNull()
        if (stored != null && (stored.scrollIndex > 0 || stored.scrollOffset > 0)) {
            listState.scrollToItem(stored.scrollIndex, stored.scrollOffset)
        }
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (idx, off) ->
            store.update(routeKey) { it.copy(scrollIndex = idx, scrollOffset = off) }
        }
    }

    return remember(routeKey, store, listState) {
        RetainedScreenState(routeKey, listState, store)
    }
}

/**
 * Holder returned by [rememberRetainedScreenState]. Exposes the [LazyListState]
 * to thread into a `LazyColumn` / `PagingScreenContent`, plus suspend setters for
 * the non-scroll UI-state slots (filter / tab / search / expansion / selection /
 * paging cursor). Every setter routes through [ScreenUiStateStore.update] so the
 * 300 ms debounce coalesces bursts.
 */
class RetainedScreenState internal constructor(
    private val routeKey: String,
    val listState: LazyListState,
    private val store: ScreenUiStateStore,
) {
    /** Live [ScreenUiState] flow — emits the current record and re-emits on change. */
    val observed: Flow<ScreenUiState?> = store.observe(routeKey)

    suspend fun setFilter(value: String?) =
        store.update(routeKey) { it.copy(filter = value) }

    suspend fun setTab(value: String?) =
        store.update(routeKey) { it.copy(tab = value) }

    suspend fun setSearch(value: String?) =
        store.update(routeKey) { it.copy(search = value) }

    suspend fun setExpanded(ids: List<String>) =
        store.update(routeKey) { it.copy(expandedIds = ids) }

    suspend fun setSelection(ids: List<String>) =
        store.update(routeKey) { it.copy(selectionIds = ids) }

    suspend fun setPagingCursor(cursor: PagingCursor?) =
        store.update(routeKey) { it.copy(pagingCursor = cursor) }
}
