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
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.isOnlineDebounced
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.mobilenativefoundation.store.store5.Store

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
class ScreenDataStream<T> internal constructor(
    /**
     * Cold Flow of ScreenState decisions. Consumer should call .stateIn() once.
     * Intentionally cold Flow (not StateFlow) to avoid double-sharing
     * when consumer applies mapContent/combineContent before stateIn.
     */
    val state: Flow<ScreenState<T>>,
    private val refreshTrigger: MutableSharedFlow<Unit>,
) {
    /** Trigger a network refresh. Preserves existing content while loading. */
    fun refresh() {
        refreshTrigger.tryEmit(Unit)
    }

    /** Retry loading (semantic alias for refresh — used on error/no-network screens). */
    fun retry() = refresh()
}

/**
 * Creates a [ScreenDataStream] from this Store, fusing network state via cmp-network-monitor.
 *
 * Features:
 * - Auto-refreshes when network reconnects (offline→online, debounced 300ms)
 * - Preserves last known content during refresh (no flicker to Loading)
 * - DecisionEngine maps all StoreData + NetworkStatus combinations to ScreenState
 * - Handles captive portal detection
 * - Single refresh/retry entry point
 *
 * @param key Store key to stream data for.
 * @param networkMonitor cmp-network-monitor's NetworkMonitor (injected via Koin).
 * @param scope CoroutineScope (typically viewModelScope) for auto-refresh coroutine.
 * @param isEmpty Optional predicate for "no data has arrived yet from Store".
 *   NOT for "empty list" detection — use [emptyIfContent] for that.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
    key: Key,
    networkMonitor: NetworkMonitor,
    scope: CoroutineScope,
    isEmpty: (Output) -> Boolean = { false },
): ScreenDataStream<Output> {
    val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Auto-refresh when network reconnects (debounced to avoid WiFi↔Cell flicker)
    scope.launch {
        networkMonitor.isOnlineDebounced(300L)
            .distinctUntilChanged()
            .filter { it }
            .drop(1) // Skip initial emission (don't double-load on start)
            .collect { refreshTrigger.tryEmit(Unit) }
    }

    // Track last known content to preserve during refresh
    var lastContent: StoreData<Output>? = null

    val storeFlow: Flow<StoreData<Output>> = refreshTrigger
        .onStart { emit(Unit) } // Initial load on subscription
        .flatMapLatest {
            streamDataNoFallback(key = key, isEmpty = isEmpty)
        }
        .map { storeData ->
            if (!storeData.isEmpty) {
                lastContent = storeData
                storeData
            } else if (storeData.isEmpty && lastContent != null && storeData.error == null) {
                // Refresh in progress — preserve last content with UPDATING
                lastContent!!.copy(isRefreshing = true)
            } else {
                storeData
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
    )
}

/**
 * Overload accepting a Flow<Key> for dynamic keys (e.g., selected client from DataStore).
 * Re-streams from Store when key changes. Resets lastContent on key change.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
    keyFlow: Flow<Key>,
    networkMonitor: NetworkMonitor,
    scope: CoroutineScope,
    isEmpty: (Output) -> Boolean = { false },
): ScreenDataStream<Output> {
    val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    scope.launch {
        networkMonitor.isOnlineDebounced(300L)
            .distinctUntilChanged()
            .filter { it }
            .drop(1)
            .collect { refreshTrigger.tryEmit(Unit) }
    }

    var lastContent: StoreData<Output>? = null

    val storeFlow: Flow<StoreData<Output>> = combine(
        keyFlow,
        refreshTrigger.onStart { emit(Unit) },
    ) { key, _ -> key }
        .flatMapLatest { key ->
            lastContent = null // Reset on key change
            streamDataNoFallback(key = key, isEmpty = isEmpty)
        }
        .map { storeData ->
            if (!storeData.isEmpty) {
                lastContent = storeData
                storeData
            } else if (storeData.isEmpty && lastContent != null && storeData.error == null) {
                lastContent!!.copy(isRefreshing = true)
            } else {
                storeData
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
    )
}
