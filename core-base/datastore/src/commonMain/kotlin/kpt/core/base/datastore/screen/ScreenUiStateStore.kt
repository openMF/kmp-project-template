/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.datastore.screen

import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock

/**
 * Plain reactive settings-backed repository for per-screen UI state.
 *
 * A plain reactive settings repository, deliberately NOT a Store5 store — the
 * record has no server-of-truth, so the S5-* / SC2 gate surface does not apply
 * here (GOAL D8). Storage is [Settings] injected as `named("plain")` — durable per
 * target: SharedPreferences on Android, NSUserDefaults on iOS, PreferencesSettings
 * on JVM/Desktop, localStorage on JS/wasmJs.
 *
 * Mutations coalesce behind a [DEFAULT_DEBOUNCE_MS]-ms debounce so a scroll storm
 * of 60 rapid [update] calls flushes at most 2 backend writes. Entries GC by
 * [ScreenUiState.updatedAt] via [evictOlderThan], defaulting to the 30-day TTL
 * that matches `StoreCacheManager.DEFAULT_DRAFT_TTL_MS` so retained UI state ages
 * out on the same cadence as SUBMITTED/FAILED drafts (GOAL AC-7).
 */
interface ScreenUiStateStore {
    /**
     * Observe the persisted [ScreenUiState] for [routeKey]. Emits the current value
     * (possibly `null` if no record yet) on subscription, then re-emits whenever
     * [update]/[clear]/[evictOlderThan] mutates that key. Distinct-until-changed.
     */
    fun observe(routeKey: String): Flow<ScreenUiState?>

    /**
     * Apply [block] to the current state for [routeKey] (defaulting to a fresh
     * [ScreenUiState] if none exists), stamp `updated_at`, buffer the result, and
     * flush to [Settings] after a [DEFAULT_DEBOUNCE_MS]-ms quiet window.
     */
    suspend fun update(routeKey: String, block: (ScreenUiState) -> ScreenUiState)

    /**
     * Remove every persisted [ScreenUiState] whose `updated_at` is older than
     * `now - maxAgeMs`. Defaults to [DEFAULT_MAX_AGE_MS] (30 days), mirroring
     * `StoreCacheManager.DEFAULT_DRAFT_TTL_MS`.
     */
    suspend fun evictOlderThan(maxAgeMs: Long = DEFAULT_MAX_AGE_MS)

    /**
     * Remove the persisted [ScreenUiState] for [routeKey] only.
     */
    suspend fun clear(routeKey: String)

    companion object {
        const val KEY_PREFIX: String = "kpt.screen-ui-state."
        const val DEFAULT_DEBOUNCE_MS: Long = 300L
        // Matches StoreCacheManager.DEFAULT_DRAFT_TTL_MS verbatim (30 days).
        const val DEFAULT_MAX_AGE_MS: Long = 30L * 24 * 60 * 60 * 1000
    }
}

internal class ScreenUiStateStoreImpl(
    private val settings: Settings,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val debounceMs: Long = ScreenUiStateStore.DEFAULT_DEBOUNCE_MS,
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ScreenUiStateStore {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val mutex = Mutex()
    private val pending = mutableMapOf<String, ScreenUiState>()
    private val flushJobs = mutableMapOf<String, Job>()
    private val changes = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun observe(routeKey: String): Flow<ScreenUiState?> =
        changes
            .filter { it == routeKey }
            .onStart { emit(routeKey) }
            .map { readCurrent(routeKey) }
            .distinctUntilChanged()

    override suspend fun update(routeKey: String, block: (ScreenUiState) -> ScreenUiState) {
        mutex.withLock {
            val current = pending[routeKey] ?: readPersisted(routeKey) ?: ScreenUiState()
            val next = block(current).copy(updatedAt = nowMs())
            pending[routeKey] = next
            flushJobs[routeKey]?.cancel()
            flushJobs[routeKey] = scope.launch {
                delay(debounceMs)
                val snapshot = mutex.withLock { pending[routeKey] }
                if (snapshot != null) {
                    settings.putString(
                        storageKey(routeKey),
                        json.encodeToString(snapshot),
                    )
                }
            }
        }
        changes.tryEmit(routeKey)
    }

    override suspend fun evictOlderThan(maxAgeMs: Long) {
        val cutoff = nowMs() - maxAgeMs
        val evicted = mutableListOf<String>()
        mutex.withLock {
            settings.keys
                .filter { it.startsWith(ScreenUiStateStore.KEY_PREFIX) }
                .forEach { storageKey ->
                    val record = settings.getStringOrNull(storageKey)
                        ?.let { raw -> runCatching { json.decodeFromString<ScreenUiState>(raw) }.getOrNull() }
                    if (record != null && record.updatedAt < cutoff) {
                        settings.remove(storageKey)
                        val rk = storageKey.removePrefix(ScreenUiStateStore.KEY_PREFIX)
                        pending.remove(rk)
                        flushJobs.remove(rk)?.cancel()
                        evicted += rk
                    }
                }
        }
        for (rk in evicted) changes.tryEmit(rk)
    }

    override suspend fun clear(routeKey: String) {
        mutex.withLock {
            flushJobs.remove(routeKey)?.cancel()
            pending.remove(routeKey)
            settings.remove(storageKey(routeKey))
        }
        changes.tryEmit(routeKey)
    }

    private fun readCurrent(routeKey: String): ScreenUiState? =
        pending[routeKey] ?: readPersisted(routeKey)

    private fun readPersisted(routeKey: String): ScreenUiState? {
        val raw = settings.getStringOrNull(storageKey(routeKey)) ?: return null
        return runCatching { json.decodeFromString<ScreenUiState>(raw) }.getOrNull()
    }

    private fun storageKey(routeKey: String): String =
        ScreenUiStateStore.KEY_PREFIX + routeKey
}
