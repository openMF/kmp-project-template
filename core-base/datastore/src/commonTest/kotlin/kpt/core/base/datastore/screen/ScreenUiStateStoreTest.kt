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

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Common-test suite for [ScreenUiStateStore]. Runs on every KMP target
 * (`android`, `iosArm64`, `iosSimulatorArm64`, `desktop`, `js`, `wasmJs`) because
 * the file lives in `commonTest` and depends only on `com.russhwolf.settings.MapSettings`
 * from `multiplatform-settings-test` — no platform APIs.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScreenUiStateStoreTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    /** AC-5 — every declared field survives a debounced write + read cycle. */
    @Test
    fun roundTripsEveryField() = runTest {
        val settings = MapSettings()
        val store = ScreenUiStateStoreImpl(
            settings = settings,
            scope = this,
            nowMs = { 1_700_000_000_000L },
        )
        val expected = ScreenUiState(
            scrollIndex = 42,
            scrollOffset = 7,
            filter = "active",
            tab = "loans",
            search = "emi",
            expandedIds = listOf("a", "b", "c"),
            selectionIds = listOf("x", "y"),
            pagingCursor = PagingCursor(lastPageLoaded = 5, query = "btc"),
            // updated_at overwritten by nowMs() inside update()
            updatedAt = 0L,
        )

        store.update("route") { expected }
        advanceTimeBy(debounceFudge())
        advanceUntilIdle()

        val observed = store.observe("route").first()
        assertNotNull(observed, "observe should surface the just-written record")
        assertEquals(expected.scrollIndex, observed.scrollIndex)
        assertEquals(expected.scrollOffset, observed.scrollOffset)
        assertEquals(expected.filter, observed.filter)
        assertEquals(expected.tab, observed.tab)
        assertEquals(expected.search, observed.search)
        assertEquals(expected.expandedIds, observed.expandedIds)
        assertEquals(expected.selectionIds, observed.selectionIds)
        assertEquals(expected.pagingCursor, observed.pagingCursor)
        assertEquals(1_700_000_000_000L, observed.updatedAt)

        // Stronger proof: a fresh store reading only from Settings sees the same record.
        val fresh = ScreenUiStateStoreImpl(settings = settings, scope = this, nowMs = { 0L })
        val reloaded = fresh.observe("route").first()
        assertNotNull(reloaded, "fresh store must reload the persisted record")
        assertEquals(observed, reloaded)
    }

    /** AC-6 — 60 rapid mutations coalesce to ≤ 2 backend writes via 300 ms debounce. */
    @Test
    fun debouncesSixtyMutationsToAtMostTwoWrites() = runTest {
        val settings = CountingSettings(MapSettings())
        val store = ScreenUiStateStoreImpl(
            settings = settings,
            scope = this,
            nowMs = { 1_700_000_000_000L },
        )

        repeat(60) { i ->
            store.update("scroll") { it.copy(scrollIndex = i) }
        }
        advanceTimeBy(debounceFudge())
        advanceUntilIdle()

        assertTrue(
            settings.putStringCount <= 2,
            "expected ≤ 2 backend writes after 60 rapid updates, got ${settings.putStringCount}",
        )
        val last = store.observe("scroll").first()
        assertNotNull(last)
        assertEquals(59, last.scrollIndex, "final state should reflect the last update")
    }

    /** AC-7 — evictOlderThan drops entries older than the cutoff and keeps fresh ones. */
    @Test
    fun evictsEntriesOlderThan() = runTest {
        val settings = MapSettings()
        var now = 10_000_000L
        val store = ScreenUiStateStoreImpl(
            settings = settings,
            scope = this,
            nowMs = { now },
        )

        // Seed an OLD record and a FRESH record directly (bypass debounce for determinism).
        val oldRecord = ScreenUiState(scrollIndex = 1, updatedAt = 1_000_000L)
        val freshRecord = ScreenUiState(scrollIndex = 2, updatedAt = 9_000_000L)
        settings.putString(
            ScreenUiStateStore.KEY_PREFIX + "old",
            json.encodeToString(oldRecord),
        )
        settings.putString(
            ScreenUiStateStore.KEY_PREFIX + "fresh",
            json.encodeToString(freshRecord),
        )

        // now = 10_000_000; maxAgeMs = 2_000_000 → cutoff = 8_000_000.
        // old.updated_at = 1_000_000 (< cutoff) → evicted.
        // fresh.updated_at = 9_000_000 (>= cutoff) → kept.
        store.evictOlderThan(maxAgeMs = 2_000_000L)
        advanceUntilIdle()

        assertNull(
            settings.getStringOrNull(ScreenUiStateStore.KEY_PREFIX + "old"),
            "old record should be evicted",
        )
        assertNotNull(
            settings.getStringOrNull(ScreenUiStateStore.KEY_PREFIX + "fresh"),
            "fresh record should be retained",
        )
        assertNull(store.observe("old").first(), "observe should reflect the eviction")
        assertNotNull(store.observe("fresh").first())
    }

    /** AC-9 side-verify — kotlin.serialization plugin is applied; Json round-trip works. */
    @Test
    fun serializationCompilesAndRoundTripsViaJson() {
        val record = ScreenUiState(
            scrollIndex = 3,
            scrollOffset = 4,
            filter = "f",
            tab = "t",
            search = "s",
            expandedIds = listOf("p", "q"),
            selectionIds = listOf("r"),
            pagingCursor = PagingCursor(lastPageLoaded = 8, query = "abc"),
            updatedAt = 1_700_000_000_123L,
        )
        val encoded = json.encodeToString(record)
        // Snake-case @SerialName must land in the JSON payload.
        assertTrue(encoded.contains("scroll_index"), "expected snake_case @SerialName in JSON")
        assertTrue(encoded.contains("paging_cursor"), "expected nested @SerialName in JSON")
        assertTrue(encoded.contains("updated_at"), "expected updated_at @SerialName in JSON")

        val decoded = json.decodeFromString<ScreenUiState>(encoded)
        assertEquals(record, decoded, "Json round-trip must be exact")
    }

    /** AC-5 boundary — clear removes only the target routeKey. */
    @Test
    fun clearRemovesOnlyTargetRouteKey() = runTest {
        val settings = MapSettings()
        val store = ScreenUiStateStoreImpl(
            settings = settings,
            scope = this,
            nowMs = { 1_700_000_000_000L },
        )
        store.update("keep") { it.copy(filter = "keep-me") }
        store.update("drop") { it.copy(filter = "drop-me") }
        advanceTimeBy(debounceFudge())
        advanceUntilIdle()

        assertNotNull(store.observe("keep").first())
        assertNotNull(store.observe("drop").first())

        store.clear("drop")
        advanceUntilIdle()

        val remaining = store.observe("keep").first()
        assertNotNull(remaining, "keep should survive clear('drop')")
        assertEquals("keep-me", remaining.filter)
        assertNull(store.observe("drop").first(), "drop should be gone after clear")
        assertNull(
            settings.getStringOrNull(ScreenUiStateStore.KEY_PREFIX + "drop"),
            "backing settings should also lose the cleared key",
        )
    }

    // Enough of a fudge past DEFAULT_DEBOUNCE_MS to guarantee the debounce fires
    // under runTest's virtual clock.
    private fun debounceFudge(): Long = ScreenUiStateStore.DEFAULT_DEBOUNCE_MS + 100L
}

/**
 * Delegating [Settings] that counts `putString(...)` invocations — used by the
 * debounce test to prove 60 [ScreenUiStateStore.update] calls coalesce to at most
 * 2 backend writes.
 */
private class CountingSettings(private val backing: MapSettings) : Settings by backing {
    var putStringCount: Int = 0
        private set

    override fun putString(key: String, value: String) {
        putStringCount++
        backing.putString(key, value)
    }
}
