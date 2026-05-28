/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.store.screen

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.isOnlineDebounced
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.mobilenativefoundation.store.store5.Store
import template.core.base.store.infra.DecisionEngine
import template.core.base.store.infra.FetchedAtRepository

/**
 * Default reconnect-debounce window for [asScreenStream]. Suppresses transient
 * WiFi↔Cell handoff flicker while still feeling responsive on real reconnects.
 * Tunable per call via the `reconnectDebounceMs` parameter; pass `0L` to disable
 * auto-refresh-on-reconnect entirely.
 *
 * Mirrors `NetworkMonitorContract.DEFAULT_DEBOUNCE_MS` in `core/data/infra/`
 * (core-base intentionally doesn't depend on core/data, so the constant is
 * duplicated; both must move together if tuned).
 */
const val DEFAULT_RECONNECT_DEBOUNCE_MS: Long = 300L

/**
 * Default user-tap refresh debounce window for [ScreenDataStream.refresh] /
 * [ScreenDataStream.retry]. Within this window, rapid duplicate taps are
 * suppressed — protects the network layer from accidental thrashing when a
 * user mashes pull-to-refresh. Tunable per call via the
 * `userRefreshDebounceMs` parameter on `asScreenStream`; pass `0L` to disable.
 */
const val DEFAULT_USER_REFRESH_DEBOUNCE_MS: Long = 1_000L

/**
 * A unified data stream combining Store data + cmp-network-monitor into pre-decided [ScreenState].
 *
 * Eliminates all ViewModel boilerplate:
 * - No manual network observation (auto-refreshes on reconnect)
 * - No manual DataState → UI state mapping (DecisionEngine handles it)
 * - No manual retry/refresh logic (built-in)
 * - No WiFi↔Cell handoff flicker (debounced at 300ms)
 * - Preserves existing content during pull-to-refresh (lastContent cache)
 * - Detects captive portals (hotel WiFi login pages)
 *
 * Usage:
 * ```
 * class MyViewModel(store: Store<Long, Data>, networkMonitor: NetworkMonitor) : ViewModel() {
 *     private val stream = store.asScreenStream(
 *         key = clientId,
 *         networkMonitor = networkMonitor,
 *         scope = viewModelScope,
 *     )
 *     val uiState = stream.state
 *         .mapContent { data, _ -> transform(data) }
 *         .emptyIfContent { it.items.isEmpty() }
 *         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScreenState.Loading)
 *     fun onRetry() = stream.retry()
 * }
 * ```
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class ScreenDataStream<T> internal constructor(
    /**
     * Cold Flow of ScreenState decisions. Consumer should call .stateIn() once.
     * Intentionally cold Flow (not StateFlow) to avoid double-sharing
     * when consumer applies mapContent/combineContent before stateIn.
     */
    val state: Flow<ScreenState<T>>,
    private val refreshTrigger: MutableSharedFlow<Unit>,
    private val userRefreshDebounceMs: Long = DEFAULT_USER_REFRESH_DEBOUNCE_MS,
    private val timeSource: kotlin.time.TimeSource = kotlin.time.TimeSource.Monotonic,
) {
    private var lastRefreshMark: kotlin.time.TimeMark? = null

    /**
     * Trigger a network refresh. Preserves existing content while loading.
     *
     * Rapid duplicate taps within [userRefreshDebounceMs] are suppressed — protects
     * the network layer from accidental thrashing when a user mashes pull-to-refresh.
     * The first tap fires immediately; subsequent taps inside the window are dropped
     * silently. Pass `userRefreshDebounceMs = 0L` on the builder to disable.
     */
    fun refresh() {
        if (userRefreshDebounceMs > 0L) {
            val mark = lastRefreshMark
            if (mark != null) {
                val elapsedMs = mark.elapsedNow().inWholeMilliseconds
                if (elapsedMs < userRefreshDebounceMs) {
                    return // suppress duplicate tap
                }
            }
            lastRefreshMark = timeSource.markNow()
        }
        refreshTrigger.tryEmit(Unit)
    }

    /** Retry loading (semantic alias for refresh — used on error/no-network screens). */
    fun retry() = refresh()
}

/**
 * Test-only constructor for [ScreenDataStream]. Lets feature-module unit tests
 * stitch a [state] flow + an externally-observable [refreshTrigger] into a real
 * [ScreenDataStream] instance, without granting feature modules access to the
 * primary [internal] constructor.
 *
 * The framework's own tests use [FakeScreenDataStream] (a higher-level fake);
 * cross-module consumers should reach for this when they need bespoke routing
 * (per-key fakes, multi-stream dashboards) that doesn't fit the FakeScreenDataStream
 * single-buffer shape. Returned instance is fully usable production-side — there
 * is no internal "test mode" flag.
 *
 * Marked `ExperimentalScreenDataStreamTestingApi` so feature modules opt-in
 * explicitly, signalling test-only intent in the call site.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This API is for unit tests of ScreenDataStream consumers only. " +
        "Do not call from production code.",
)
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalScreenDataStreamTestingApi

/**
 * Construct a [ScreenDataStream] from an arbitrary [state] flow and a caller-owned
 * [refreshTrigger]. The trigger is what [ScreenDataStream.refresh] / .retry() will
 * emit to — tests can subscribe to it to assert refresh dispatch behavior without
 * touching the framework's internals.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
@ExperimentalScreenDataStreamTestingApi
fun <T> screenDataStreamForTesting(
    state: Flow<ScreenState<T>>,
    refreshTrigger: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1),
    userRefreshDebounceMs: Long = 0L, // tests don't want debounce by default
    timeSource: kotlin.time.TimeSource = kotlin.time.TimeSource.Monotonic,
): ScreenDataStream<T> = ScreenDataStream(
    state = state,
    refreshTrigger = refreshTrigger,
    userRefreshDebounceMs = userRefreshDebounceMs,
    timeSource = timeSource,
)

/**
 * Creates a [ScreenDataStream] from this Store, fusing network state via cmp-network-monitor.
 *
 * Features:
 * - Auto-refreshes when network reconnects (offline→online, debounced 300ms by default)
 * - Preserves last known content during refresh (no flicker to Loading)
 * - DecisionEngine maps all StoreData + NetworkStatus combinations to ScreenState
 * - Handles captive portal detection
 * - Single refresh/retry entry point with 1s user-tap debounce
 *
 * **Concurrent-subscriber behavior:** N subscribers to the same Store key with the
 * same FetchPolicy each get their own [ScreenDataStream] instance with its own
 * refresh trigger. Whether the underlying network fetch is shared depends on the
 * Store's configuration: a Store backed by a [org.mobilenativefoundation.store.store5.SourceOfTruth]
 * (e.g. Room) OR an explicit `cachePolicy(MemoryPolicy)` will dedupe concurrent
 * reads through the cache layer. A plain `StoreBuilder.from(fetcher)` (no SoT,
 * no cachePolicy) fires the fetcher once per subscriber. Configure each app
 * Store deliberately based on the dedup requirement.
 *
 * @param key Store key to stream data for.
 * @param networkMonitor cmp-network-monitor's NetworkMonitor (injected via Koin).
 * @param scope CoroutineScope (typically viewModelScope) for auto-refresh coroutine.
 * @param isEmpty Optional predicate for "no data has arrived yet from Store".
 *   NOT for "empty list" detection — use [emptyIfContent] for that.
 * @param reconnectDebounceMs Window suppressing transient WiFi↔Cell flicker on
 *   reconnect refresh; pass `0L` to disable auto-refresh-on-reconnect entirely.
 *   Defaults to 300ms (sensible for most apps). Forks with slow networks may
 *   pass 1-2s; high-frequency dashboards may pass 100ms.
 * @param userRefreshDebounceMs Window suppressing rapid duplicate user-tap refresh
 *   requests; pass `0L` to disable. Defaults to 1s. Protects against pull-to-refresh
 *   spam.
 */
@OptIn(ExperimentalCoroutinesApi::class, kotlin.time.ExperimentalTime::class)
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
    key: Key,
    networkMonitor: NetworkMonitor,
    fetchedAtRepository: FetchedAtRepository,
    cacheKey: String,
    scope: CoroutineScope,
    isEmpty: (Output) -> Boolean = { false },
    fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
    reconnectDebounceMs: Long = DEFAULT_RECONNECT_DEBOUNCE_MS,
    userRefreshDebounceMs: Long = DEFAULT_USER_REFRESH_DEBOUNCE_MS,
): ScreenDataStream<Output> {
    val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Auto-refresh when network reconnects (debounced to avoid WiFi↔Cell flicker)
    // Skipped for CACHE_ONLY — no network requests are ever made.
    // Skipped when reconnectDebounceMs <= 0 — consumer opted out of auto-refresh.
    if (fetchPolicy != FetchPolicy.CACHE_ONLY && reconnectDebounceMs > 0L) {
        scope.launch {
            networkMonitor.isOnlineDebounced(reconnectDebounceMs)
                .distinctUntilChanged()
                .filter { it }
                .drop(1) // Skip initial emission (don't double-load on start)
                .collect { refreshTrigger.tryEmit(Unit) }
        }
    }

    // Periodic background refresh — FetchPolicy.PERIODIC only.
    // The ticker fires through the same refreshTrigger as user-tap / reconnect refresh,
    // so lastContent preservation + debounce + freshness banner all work uniformly.
    // foregroundOnly: parsed but currently informational; the foreground gate is
    // a follow-up phase (needs LocalAppForegroundFlow threading through core-base/ui).
    if (fetchPolicy is FetchPolicy.PERIODIC) {
        scope.launch {
            while (isActive) {
                delay(fetchPolicy.intervalMillis)
                refreshTrigger.tryEmit(Unit)
            }
        }
    }

    // Seed persisted timestamp so warm-reopen shows real "Updated 5m ago".
    // Held in a holder so the map step below can read the latest value.
    var persistedFetchedAt: kotlin.time.Instant? = null
    scope.launch { persistedFetchedAt = fetchedAtRepository.read(cacheKey) }

    // Track last known content to preserve during refresh
    var lastContent: StoreData<Output>? = null

    val storeFlow: Flow<StoreData<Output>> = refreshTrigger
        .onStart { emit(Unit) } // Initial load on subscription
        .flatMapLatest {
            streamDataForPolicy(key = key, policy = fetchPolicy, isEmpty = isEmpty)
        }
        .map { storeData ->
            // Persistence: when Store5 hands us NETWORK-origin data with a fresh
            // timestamp, write it through. When we get CACHE/empty with null
            // timestamp, fall back to the persisted value so the staleness banner
            // can show the real age across ViewModel destruction.
            val enriched = when {
                storeData.origin == DataOrigin.NETWORK && storeData.fetchedAtInstant != null -> {
                    fetchedAtRepository.write(cacheKey, storeData.fetchedAtInstant)
                    storeData
                }
                storeData.fetchedAtInstant == null && persistedFetchedAt != null ->
                    storeData.copy(fetchedAtInstant = persistedFetchedAt)
                else -> storeData
            }
            if (!enriched.isEmpty) {
                lastContent = enriched
                enriched
            } else if (enriched.isEmpty && lastContent != null && enriched.error == null) {
                // Refresh in progress — preserve last content with UPDATING
                lastContent.copy(isRefreshing = true)
            } else {
                enriched
            }
        }

    // Combine with FULL NetworkStatus (not just Boolean) for captive portal detection
    val screenStateFlow: Flow<ScreenState<Output>> = combine(
        storeFlow,
        networkMonitor.networkStatus,
    ) { storeData, status ->
        DecisionEngine.decide(storeData, status)
    }

    return ScreenDataStream(
        state = screenStateFlow,
        refreshTrigger = refreshTrigger,
        userRefreshDebounceMs = userRefreshDebounceMs,
    )
}

/**
 * Overload accepting a Flow<Key> for dynamic keys (e.g., selected client from DataStore).
 * Re-streams from Store when key changes. Resets lastContent on key change.
 *
 * @param cacheKeyFor Computes the persistence key for each Key emission. Lets the
 *   `framework_fetched_at` table store one timestamp per key — e.g. one per
 *   selected client — instead of overwriting on every key change.
 */
@OptIn(ExperimentalCoroutinesApi::class, kotlin.time.ExperimentalTime::class)
@Suppress("CyclomaticComplexMethod", "LongParameterList")
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
    keyFlow: Flow<Key>,
    networkMonitor: NetworkMonitor,
    fetchedAtRepository: FetchedAtRepository,
    cacheKeyFor: (Key) -> String,
    scope: CoroutineScope,
    isEmpty: (Output) -> Boolean = { false },
    fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
    reconnectDebounceMs: Long = DEFAULT_RECONNECT_DEBOUNCE_MS,
    userRefreshDebounceMs: Long = DEFAULT_USER_REFRESH_DEBOUNCE_MS,
): ScreenDataStream<Output> {
    val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    if (fetchPolicy != FetchPolicy.CACHE_ONLY && reconnectDebounceMs > 0L) {
        scope.launch {
            networkMonitor.isOnlineDebounced(reconnectDebounceMs)
                .distinctUntilChanged()
                .filter { it }
                .drop(1)
                .collect { refreshTrigger.tryEmit(Unit) }
        }
    }

    // Periodic background refresh — see single-key overload for rationale.
    if (fetchPolicy is FetchPolicy.PERIODIC) {
        scope.launch {
            while (isActive) {
                delay(fetchPolicy.intervalMillis)
                refreshTrigger.tryEmit(Unit)
            }
        }
    }

    var lastContent: StoreData<Output>? = null
    var currentCacheKey: String? = null
    var persistedFetchedAt: kotlin.time.Instant? = null

    val storeFlow: Flow<StoreData<Output>> = combine(
        keyFlow,
        refreshTrigger.onStart { emit(Unit) },
    ) { key, _ -> key }
        .flatMapLatest { key ->
            lastContent = null // Reset on key change
            currentCacheKey = cacheKeyFor(key)
            // Re-seed persistedFetchedAt for the new key.
            persistedFetchedAt = fetchedAtRepository.read(currentCacheKey)
            streamDataForPolicy(key = key, policy = fetchPolicy, isEmpty = isEmpty)
        }
        .map { storeData ->
            val key = currentCacheKey ?: return@map storeData
            val enriched = when {
                storeData.origin == DataOrigin.NETWORK && storeData.fetchedAtInstant != null -> {
                    fetchedAtRepository.write(key, storeData.fetchedAtInstant)
                    storeData
                }
                storeData.fetchedAtInstant == null && persistedFetchedAt != null ->
                    storeData.copy(fetchedAtInstant = persistedFetchedAt)
                else -> storeData
            }
            if (!enriched.isEmpty) {
                lastContent = enriched
                enriched
            } else if (enriched.isEmpty && lastContent != null && enriched.error == null) {
                lastContent!!.copy(isRefreshing = true)
            } else {
                enriched
            }
        }

    val screenStateFlow: Flow<ScreenState<Output>> = combine(
        storeFlow,
        networkMonitor.networkStatus,
    ) { storeData, status ->
        DecisionEngine.decide(storeData, status)
    }

    return ScreenDataStream(
        state = screenStateFlow,
        refreshTrigger = refreshTrigger,
        userRefreshDebounceMs = userRefreshDebounceMs,
    )
}
