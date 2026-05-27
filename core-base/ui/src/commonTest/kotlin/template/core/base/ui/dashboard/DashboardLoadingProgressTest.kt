/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.ui.dashboard

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import template.core.base.store.screen.DataFreshness
import template.core.base.store.screen.ScreenState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pure-data contract tests for [aggregateDashboardProgress] + [toDashboardProgressState].
 * Compose UI tests for [DashboardProgressBar] / [IndependentCardLayout] land separately
 * once `compose.uiTest` is wired into core-base/ui's commonTest source set.
 */
class DashboardLoadingProgressTest {

    @Test
    fun emptyList_yieldsZeroProgress() = runTest {
        val result = flowOf(emptyList<ScreenState<Any>>()).aggregateDashboardProgress().first()
        assertEquals(
            DashboardProgressState(
                loaded = 0,
                total = 0,
                isAnyLoading = false,
                hasAnyError = false,
                hasAnyEmpty = false,
            ),
            result,
        )
    }

    @Test
    fun allLoading_marksAnyLoadingTrue_andZeroLoaded() = runTest {
        val states: List<ScreenState<Any>> = listOf(
            ScreenState.Loading,
            ScreenState.Loading,
            ScreenState.Loading,
        )
        val result = flowOf(states).aggregateDashboardProgress().first()
        assertEquals(0, result.loaded)
        assertEquals(3, result.total)
        assertTrue(result.isAnyLoading)
        assertFalse(result.hasAnyError)
        assertFalse(result.hasAnyEmpty)
    }

    @Test
    fun mixedContentAndLoading_countsLoadedOnly() = runTest {
        val states: List<ScreenState<String>> = listOf(
            ScreenState.Content(data = "a", freshness = DataFreshness.FRESH),
            ScreenState.Loading,
            ScreenState.Content(data = "b", freshness = DataFreshness.FRESH),
            ScreenState.Loading,
        )
        val result = flowOf(states).aggregateDashboardProgress().first()
        assertEquals(2, result.loaded)
        assertEquals(4, result.total)
        assertTrue(result.isAnyLoading)
        assertFalse(result.hasAnyError)
    }

    @Test
    fun allContent_marksIsAnyLoadingFalse_andAllLoaded() = runTest {
        val states: List<ScreenState<Int>> = listOf(
            ScreenState.Content(data = 1, freshness = DataFreshness.FRESH),
            ScreenState.Content(data = 2, freshness = DataFreshness.STALE),
            ScreenState.Content(data = 3, freshness = DataFreshness.UPDATING),
        )
        val result = flowOf(states).aggregateDashboardProgress().first()
        assertEquals(3, result.loaded)
        assertEquals(3, result.total)
        assertFalse(result.isAnyLoading)
        assertFalse(result.hasAnyError)
        assertFalse(result.hasAnyEmpty)
    }

    @Test
    fun anyError_marksHasAnyErrorTrue() = runTest {
        val states: List<ScreenState<Any>> = listOf(
            ScreenState.Content(data = "ok", freshness = DataFreshness.FRESH),
            ScreenState.Error(error = RuntimeException("boom")),
        )
        val result = flowOf(states).aggregateDashboardProgress().first()
        assertEquals(1, result.loaded)
        assertEquals(2, result.total)
        assertTrue(result.hasAnyError)
    }

    @Test
    fun noNetwork_alsoCountsAsError() = runTest {
        val states: List<ScreenState<Any>> = listOf(
            ScreenState.NoNetwork(isCaptivePortal = false),
        )
        val result = flowOf(states).aggregateDashboardProgress().first()
        assertEquals(0, result.loaded)
        assertEquals(1, result.total)
        assertTrue(result.hasAnyError, "NoNetwork is a failure-to-load — must roll up into hasAnyError.")
    }

    @Test
    fun anyEmpty_marksHasAnyEmptyTrue() = runTest {
        val states: List<ScreenState<Any>> = listOf(
            ScreenState.Content(data = "ok", freshness = DataFreshness.FRESH),
            ScreenState.Empty,
        )
        val result = flowOf(states).aggregateDashboardProgress().first()
        assertEquals(1, result.loaded)
        assertEquals(2, result.total)
        assertTrue(result.hasAnyEmpty)
        assertFalse(result.hasAnyError)
    }

    @Test
    fun pureConversion_matchesFlowExtension() {
        val states: List<ScreenState<Any>> = listOf(
            ScreenState.Loading,
            ScreenState.Content(data = "x", freshness = DataFreshness.FRESH),
            ScreenState.Empty,
            ScreenState.Error(error = IllegalStateException()),
        )
        val result = states.toDashboardProgressState()
        assertEquals(1, result.loaded)
        assertEquals(4, result.total)
        assertTrue(result.isAnyLoading)
        assertTrue(result.hasAnyEmpty)
        assertTrue(result.hasAnyError)
    }
}
