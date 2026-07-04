/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.paging

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mobilenativefoundation.store.store5.Store
import kpt.core.base.store.error.OfflineException
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.infra.DecisionEngine
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.DataOrigin
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.StoreData
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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
 *
 * ## Deep-scroll restore (Phase 4)
 *
 * Callers that wish to survive process death across a paged list pass an
 * `initialCursorProvider` to [asPagingScreenStream]: a suspend lambda that
 * yields the last-persisted [PagingCursor] (or `null` for a first-time visit).
 * On construction, [loadInitialPage] awaits the provider inside `scope.launch`
 * — never blocking commonMain (Kotlin/JS and Kotlin/wasm have no
 * `runBlocking`) — and, when a cursor is returned, batch-calls
 * `store.loadPage(refresh = false)` for pages `0..cursor.lastPageLoaded`,
 * concatenating results. Because every requested page is already durable in
 * Room from the prior session, there is no network round-trip.
 *
 * The [cursorFlow] StateFlow updates on:
 *   - every successful [loadNextPage] (bumps `lastPageLoaded`),
 *   - every [updateScrollPosition] call from the Screen (bumps
 *     `scrollIndex` / `scrollOffset`).
 *
 * Consumers observe [cursorFlow] and write it back to their persistence layer
 * (typically the datastore-side `ScreenUiStateStore` via the feature
 * ViewModel) with a debounce; the shared 300 ms datastore debounce absorbs
 * any scroll storm.
 */
@OptIn(ExperimentalTime::class)
class PagingScreenStream<T : Any> internal constructor(
    val state: Flow<ScreenState<List<T>>>,
    val hasMore: StateFlow<Boolean>,
    val isLoadingMore: StateFlow<Boolean>,
    /** The most recent load-more error, or null. Cleared on next loadNextPage/refresh. */
    val loadMoreError: StateFlow<Throwable?>,
    /**
     * Durable paging cursor. Emits on every successful [loadNextPage] and every
     * [updateScrollPosition] call. Seeded from the `initialCursorProvider`
     * supplied to [asPagingScreenStream] (defaults to [PagingCursor.EMPTY] when
     * no provider is supplied or the provider yields `null`).
     */
    val cursorFlow: StateFlow<PagingCursor>,
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
    private val cursorMutable: MutableStateFlow<PagingCursor>,
    private val initialCursorProvider: suspend () -> PagingCursor?,
    /** Wall-clock instant of last successful page load. Drives DataFreshnessIndicator timestamp. */
    private val lastFetchedAt: MutableStateFlow<Instant?>,
    private val networkMonitor: NetworkMonitor,
    private val cacheKey: String,
    private val fetchedAtRepository: FetchedAtRepository,
    private val fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
) {
    /** Load the next page. No-op if already loading or no more pages. Cache-first. */
    fun loadNextPage() {
        if (isLoadingMoreMutable.value || !hasMoreMutable.value) return
        scope.launch {
            isLoadingMoreMutable.value = true
            error.value = null
            val nextPage = currentPage.value + 1
            val pageKey = PageKey(page = nextPage, pageSize = pageSize, query = query)
            // refresh=false (cache-first): if this page is in SoT, return it instantly.
            // Pull-to-refresh uses the dedicated refresh() path.
            when (val result = store.loadPage(pageKey, refresh = false)) {
                is StorePageResult.Success -> {
                    items.update { it + result.items }
                    currentPage.value = nextPage
                    hasMoreMutable.value = result.nextKey != null
                    cursorMutable.value = cursorMutable.value.copy(
                        lastPageLoaded = nextPage,
                        query = query,
                    )
                    // Only update on actual network success — cache hits keep the previous
                    // timestamp so DataFreshnessIndicator shows the real age of the data,
                    // not the moment we re-read from SoT.
                    if (result.fromNetwork) {
                        val now = Clock.System.now()
                        lastFetchedAt.value = now
                        fetchedAtRepository.write(cacheKey, now)
                    }
                }
                is StorePageResult.Error -> {
                    // Re-type as OfflineException when offline so categorize() routes
                    // through Network category and LoadMoreFooter shows the no-network
                    // treatment instantly — instead of "Failed to load more" after the
                    // fetcher's executeWithRetry burns ~3s on a hopeless retry.
                    error.value = retypeIfOffline(result.error)
                }
            }
            isLoadingMoreMutable.value = false
        }
    }

    /**
     * Refresh from page 0. Forces network for the first page.
     *
     * Keeps existing items in place during the fetch so the UI shows
     * `Content(UPDATING)` (data + refreshing) instead of bouncing through
     * `Loading` (data-less). This keeps the pull-to-refresh spinner visible
     * for the full duration of the fetch and prevents content flicker.
     * Items are replaced with the new page-0 data when the fetch completes.
     */
    fun refresh() {
        scope.launch {
            currentPage.value = 0
            // Don't clear items — preserves the visible list during refresh so
            // DecisionEngine emits Content(UPDATING) and the pull-to-refresh
            // spinner stays on screen until the fetch completes.
            hasMoreMutable.value = true
            error.value = null
            isInitialLoading.value = true
            loadInitialPage(refresh = true)
        }
    }

    fun retry() = refresh()

    /**
     * Record the current scroll position so the persisted cursor can restore it
     * across process death. Callers wire this to a `LaunchedEffect` on
     * `LazyListState.firstVisibleItemIndex` / `firstVisibleItemScrollOffset`.
     * Downstream persistence deduplicates via `distinctUntilChanged` — this
     * setter itself is cheap.
     */
    fun updateScrollPosition(index: Int, offset: Int) {
        cursorMutable.value = cursorMutable.value.copy(
            scrollIndex = index,
            scrollOffset = offset,
        )
    }

    /**
     * Deep-scroll restore (Phase 4): batch re-page `0..cursor.lastPageLoaded`
     * from the already-durable Room pages via `store.loadPage(refresh = false)`
     * — no network hit. On a mid-restore error, retain the pages loaded so far,
     * surface the error, and leave `currentPage` at the last good page so the
     * next `loadNextPage()` resumes correctly.
     */
    private suspend fun restoreFromCursor(cursor: PagingCursor) {
        val restored = mutableListOf<T>()
        var lastOk = -1
        var restoreError: Throwable? = null
        for (p in 0..cursor.lastPageLoaded) {
            val key = PageKey(page = p, pageSize = pageSize, query = cursor.query)
            val result = store.loadPage(key, refresh = false)
            if (result is StorePageResult.Success) {
                restored.addAll(result.items)
                lastOk = p
                hasMoreMutable.value = result.nextKey != null
            } else if (result is StorePageResult.Error) {
                restoreError = retypeIfOffline(result.error)
                break
            }
        }
        if (lastOk >= 0) {
            items.value = restored
            currentPage.value = lastOk
            cursorMutable.value = cursor.copy(lastPageLoaded = lastOk)
        }
        if (restoreError != null) {
            error.value = restoreError
        }
        isInitialLoading.value = false
    }

    internal fun loadInitialPage(refresh: Boolean = false) {
        scope.launch {
            // Deep-scroll restore path (Phase 4): resolve the persisted cursor
            // inside this coroutine so callers pass a suspend provider — never
            // block commonMain (no runBlocking on JS / wasmJs). When the
            // provider yields a non-null cursor AND we aren't force-refreshing
            // AND the policy allows cache reads, batch-re-page every page the
            // user had loaded before restart. Every requested page is already
            // durable in Room; store.loadPage(refresh = false) serves them
            // immediately without a network hit.
            val restoreCursor = if (!refresh && fetchPolicy != FetchPolicy.NETWORK_ONLY) {
                initialCursorProvider()
            } else {
                null
            }
            if (restoreCursor != null && restoreCursor.lastPageLoaded >= 0) {
                restoreFromCursor(restoreCursor)
                return@launch
            }
            // CACHE_ONLY: read from local SoT only — never hit the network.
            if (fetchPolicy == FetchPolicy.CACHE_ONLY) {
                val pageKey = PageKey.first(pageSize = pageSize, query = query)
                when (val result = store.loadPage(pageKey, refresh = false)) {
                    is StorePageResult.Success -> {
                        items.value = result.items
                        hasMoreMutable.value = result.nextKey != null
                        cursorMutable.value = cursorMutable.value.copy(
                            lastPageLoaded = 0,
                            query = query,
                        )
                    }
                    is StorePageResult.Error -> error.value = result.error
                }
                isInitialLoading.value = false
                return@launch
            }
            // Online-first policies always force a fresh fetch:
            //   - NETWORK_ONLY: stale data would be misleading; skip cache.
            // Store5's fresh(fallBackToSourceOfTruth = true) honours this intent at the
            // single-key layer (see streamDataForPolicy); for paging, the same forceRefresh
            // signal is the closest analogue — failure to fetch still surfaces existing
            // items from `items` MutableStateFlow if the previous page load primed them.
            val forceRefresh = refresh ||
                fetchPolicy == FetchPolicy.NETWORK_ONLY
            // Offline pre-check for forced-network: if we're offline,
            // don't even call store.loadPage — `fresh(key)` would dispatch to the
            // fetcher whose `executeWithRetry` may block indefinitely waiting for
            // reconnect. Fail fast with OfflineException so DecisionEngine renders
            // the no-network treatment and the pull-to-refresh spinner can hide.
            if (forceRefresh && !isOnlineNow()) {
                error.value = OfflineException()
                isInitialLoading.value = false
                return@launch
            }
            val pageKey = PageKey.first(pageSize = pageSize, query = query)
            when (val result = store.loadPage(pageKey, refresh = forceRefresh)) {
                is StorePageResult.Success -> {
                    items.value = result.items
                    hasMoreMutable.value = result.nextKey != null
                    cursorMutable.value = cursorMutable.value.copy(
                        lastPageLoaded = 0,
                        query = query,
                    )
                    // Network-only fetchedAt update — see loadNextPage for rationale.
                    if (result.fromNetwork) {
                        val now = Clock.System.now()
                        lastFetchedAt.value = now
                        fetchedAtRepository.write(cacheKey, now)
                    }
                }
                is StorePageResult.Error -> {
                    // Same offline-retype as loadNextPage — fixes the warm-reopen bug
                    // where the user navigates back to a paged screen after disabling
                    // internet and sees "failed to fetch" briefly while the fetcher
                    // burns retry budget. With this, DecisionEngine sees a Network-
                    // class error and renders NoNetwork immediately.
                    error.value = retypeIfOffline(result.error)
                }
            }
            isInitialLoading.value = false
        }
    }

    /**
     * If the device is offline, replace [original] with an [OfflineException] so the
     * UI stack routes the error through the no-network treatment. If we're online,
     * the original error is preserved untouched (caller still sees the real cause).
     */
    private suspend fun retypeIfOffline(original: Throwable): Throwable =
        if (isOnlineNow()) original else OfflineException()

    /** Sample current network state synchronously from the StateFlow. */
    private suspend fun isOnlineNow(): Boolean =
        networkMonitor.networkStatus.first() is NetworkStatus.Available
}

/**
 * Creates a [PagingScreenStream] with network-fused state via cmp-network-monitor.
 *
 * @param fetchedAtRepository Persists last-network-fetch timestamps so the staleness
 *   banner shows real "Updated 5m ago" across ViewModel destruction and (with a
 *   Room-backed impl) app restart. Required — there is no default. Production apps
 *   wire `RoomFetchedAtRepository` (`core/data`); tests use `FakeFetchedAtRepository`.
 * @param cacheKey Identifies this Store in the [fetchedAtRepository]. Convention:
 *   `"<feature>:<storeName>"` (e.g. `"crypto:coinMarkets"`).
 * @param initialCursorProvider Suspend lambda that yields the persisted
 *   [PagingCursor] for deep-scroll restore, or `null` for a first-time visit or a
 *   caller that does not persist the cursor. Resolved inside `loadInitialPage`'s
 *   coroutine so callers never block commonMain — Kotlin/JS and Kotlin/wasmJs have
 *   no `runBlocking`. Defaults to `{ null }` (no restore attempt, matches the
 *   pre-Phase-4 shape). See Phase 4 of `store5-screen-state-persistence`.
 */
@Suppress("CyclomaticComplexMethod", "LongMethod", "LongParameterList")
fun <Value : Any> Store<PageKey, List<Value>>.asPagingScreenStream(
    networkMonitor: NetworkMonitor,
    fetchedAtRepository: FetchedAtRepository,
    cacheKey: String,
    scope: CoroutineScope,
    pageSize: Int = PageKey.DEFAULT_PAGE_SIZE,
    query: String? = null,
    fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
    initialCursorProvider: suspend () -> PagingCursor? = { null },
): PagingScreenStream<Value> {
    val items = MutableStateFlow<List<Value>>(emptyList())
    val hasMore = MutableStateFlow(true)
    val isLoadingMore = MutableStateFlow(false)
    val isInitialLoading = MutableStateFlow(true)
    val error = MutableStateFlow<Throwable?>(null)
    val currentPage = MutableStateFlow(0)
    val lastFetchedAt = MutableStateFlow<Instant?>(null)
    val cursorMutable = MutableStateFlow(PagingCursor.EMPTY)
    // Seed lastFetchedAt from persistence so warm-reopen (new ViewModel for the
    // same Store) shows the real age of cached data. Launches in scope so the
    // I/O doesn't block stream construction; UI shows null timestamp briefly.
    scope.launch { lastFetchedAt.value = fetchedAtRepository.read(cacheKey) }

    val screenState: Flow<ScreenState<List<Value>>> = combine(
        items,
        networkMonitor.networkStatus,
        isInitialLoading,
        error,
        lastFetchedAt,
    ) { itemList, status, loading, err, fetchedAt ->
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
        // fetchedAtInstant flows through to DataFreshnessIndicator so paged screens
        // show "Updated Xs ago" timestamps just like single-key screens do.
        val storeData = StoreData(
            data = itemList,
            origin = DataOrigin.NETWORK,
            isRefreshing = loading,
            fetchedAt = null,
            fetchedAtInstant = fetchedAt,
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
        cursorFlow = cursorMutable.asStateFlow(),
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
        cursorMutable = cursorMutable,
        initialCursorProvider = initialCursorProvider,
        lastFetchedAt = lastFetchedAt,
        networkMonitor = networkMonitor,
        cacheKey = cacheKey,
        fetchedAtRepository = fetchedAtRepository,
        fetchPolicy = fetchPolicy,
    ).also { it.loadInitialPage() }
}
