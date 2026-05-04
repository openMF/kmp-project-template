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

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mobilenativefoundation.store.store5.Store

/**
 * Paginated variant of [ScreenDataStream].
 * Manages page loading, appending, error surfacing, and unified state for infinite lists.
 *
 * Usage:
 * ```
 * val pagingStream = clientStore.asPagingScreenStream(
 *     networkMonitor = networkMonitor,
 *     scope = viewModelScope,
 *     pageSize = 20,
 * )
 * val uiState = pagingStream.state  // Flow<ScreenState<List<Client>>>
 * fun loadMore() = pagingStream.loadNextPage()
 * ```
 */
class PagingScreenStream<T : Any> internal constructor(
    val state: Flow<ScreenState<List<T>>>,
    val hasMore: StateFlow<Boolean>,
    val isLoadingMore: StateFlow<Boolean>,
    /** The most recent load-more error, or null. Cleared on next loadNextPage/refresh. */
    val loadMoreError: StateFlow<Throwable?>,
    private val scope: CoroutineScope,
    private val store: Store<PageKey, List<T>>,
    private val pageSize: Int,
    private val query: String?,
    private val items: MutableStateFlow<List<T>>,
    private val hasMoreMutable: MutableStateFlow<Boolean>,
    private val isLoadingMoreMutable: MutableStateFlow<Boolean>,
    private val isInitialLoading: MutableStateFlow<Boolean>,
    private val error: MutableStateFlow<Throwable?>,
    private val currentPage: MutableStateFlow<Int>,
) {
    /** Load the next page. No-op if already loading or no more pages. Cache-first. */
    fun loadNextPage() {
        if (isLoadingMoreMutable.value || !hasMoreMutable.value) return
        scope.launch {
            isLoadingMoreMutable.value = true
            error.value = null
            val nextPage = currentPage.value + 1
            val pageKey = PageKey(page = nextPage, pageSize = pageSize, query = query)
            // refresh=false (cache-first): if this page was loaded recently, return the
            // cached copy instantly. Pull-to-refresh uses the dedicated refresh() path.
            when (val result = store.loadPage(pageKey, refresh = false)) {
                is StorePageResult.Success -> {
                    items.update { it + result.items }
                    currentPage.value = nextPage
                    hasMoreMutable.value = result.nextKey != null
                }
                is StorePageResult.Error -> {
                    error.value = result.error
                }
            }
            isLoadingMoreMutable.value = false
        }
    }

    /** Refresh from page 0, clearing all loaded pages. Forces network for the first page. */
    fun refresh() {
        scope.launch {
            currentPage.value = 0
            items.value = emptyList()
            hasMoreMutable.value = true
            error.value = null
            isInitialLoading.value = true
            loadInitialPage(refresh = true)
        }
    }

    fun retry() = refresh()

    internal fun loadInitialPage(refresh: Boolean = false) {
        scope.launch {
            val pageKey = PageKey.first(pageSize = pageSize, query = query)
            when (val result = store.loadPage(pageKey, refresh = refresh)) {
                is StorePageResult.Success -> {
                    items.value = result.items
                    hasMoreMutable.value = result.nextKey != null
                }
                is StorePageResult.Error -> {
                    error.value = result.error
                }
            }
            isInitialLoading.value = false
        }
    }
}

/**
 * Creates a [PagingScreenStream] with network-fused state via cmp-network-monitor.
 */
@Suppress("CyclomaticComplexMethod")
fun <Value : Any> Store<PageKey, List<Value>>.asPagingScreenStream(
    networkMonitor: NetworkMonitor,
    scope: CoroutineScope,
    pageSize: Int = PageKey.DEFAULT_PAGE_SIZE,
    query: String? = null,
): PagingScreenStream<Value> {
    val items = MutableStateFlow<List<Value>>(emptyList())
    val hasMore = MutableStateFlow(true)
    val isLoadingMore = MutableStateFlow(false)
    val isInitialLoading = MutableStateFlow(true)
    val error = MutableStateFlow<Throwable?>(null)
    val currentPage = MutableStateFlow(0)

    val screenState: Flow<ScreenState<List<Value>>> = combine(
        items,
        networkMonitor.networkStatus,
        isInitialLoading,
        error,
    ) { itemList, status, loading, err ->
        // Definitive Empty: paging knows the difference between "load hasn't started"
        // (isInitialLoading=true) and "load completed with zero items" (isInitialLoading=false,
        // err=null). DecisionEngine treats these the same (Loading), so we special-case
        // the post-load empty here before delegating.
        if (itemList.isEmpty() && !loading && err == null) {
            return@combine ScreenState.Empty
        }
        // Wrap paging state into a StoreData<List<Value>> and let DecisionEngine handle
        // every other transition (Loading / NoNetwork / CaptivePortal / Error /
        // Content+freshness). Single state machine for both single-key (asScreenStream)
        // and paged (this) flows — eliminates drift between two separate `when` blocks.
        // Note: DecisionEngine never reads .data when isEmpty=true, so emptyList() is a
        // safe sentinel here (avoids the EMPTY_SENTINEL `as List` cast which fails at
        // runtime — outer collection type isn't erased).
        val storeData = StoreData(
            data = itemList,
            origin = DataOrigin.NETWORK,
            isRefreshing = loading,
            fetchedAt = null,
            fetchedAtInstant = null,
            error = err,
            isEmpty = itemList.isEmpty(),
        )
        DecisionEngine.decide(storeData, status)
    }

    return PagingScreenStream(
        state = screenState,
        hasMore = hasMore.asStateFlow(),
        isLoadingMore = isLoadingMore.asStateFlow(),
        loadMoreError = error.asStateFlow(),
        scope = scope,
        store = this,
        pageSize = pageSize,
        query = query,
        items = items,
        hasMoreMutable = hasMore,
        isLoadingMoreMutable = isLoadingMore,
        isInitialLoading = isInitialLoading,
        error = error,
        currentPage = currentPage,
    ).also { it.loadInitialPage() }
}
