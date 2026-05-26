/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.rates.ui

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.mifos.core.model.economic.InterestRateSeries
import org.mifos.core.model.economic.RateObservation
import template.core.base.store.screen.DataFreshness
import template.core.base.store.screen.ScreenState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Locks the [InterestRateDetailViewModel] contract:
 *
 *  - Known catalog ids resolve to the canonical [RateSeriesCatalog] key
 *    (preserving the full display name + 365-day window).
 *  - Unknown ids gracefully fall back to a minimal key with `seriesId == name`,
 *    so the detail screen still renders something rather than crashing.
 *  - Initial state is Loading.
 *  - Content emissions surface as [ScreenState.Content].
 *  - `onRetry()` and `onRefresh()` both refresh the underlying stream.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InterestRateDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun knownSeriesIdResolvesToCanonicalCatalogKey() = runTest {
        val repo = FakeEconomicRatesRepository()
        val vm = InterestRateDetailViewModel(seriesId = "DFF", repository = repo)
        dispatcher.scheduler.advanceUntilIdle()
        assertSame(RateSeriesCatalog.FedFunds, vm.seriesKey)
        assertEquals("DFF", repo.openedKeys.single().seriesId)
    }

    @Test
    fun unknownSeriesIdFallsBackToMinimalKey() = runTest {
        val repo = FakeEconomicRatesRepository()
        val vm = InterestRateDetailViewModel(seriesId = "UNKNOWN_ID", repository = repo)
        dispatcher.scheduler.advanceUntilIdle()
        // Fallback uses seriesId as both id + name so the title shows *something*.
        assertEquals("UNKNOWN_ID", vm.seriesKey.seriesId)
        assertEquals("UNKNOWN_ID", vm.seriesKey.name)
    }

    @Test
    fun initialScreenStateIsLoading() = runTest {
        val repo = FakeEconomicRatesRepository()
        val vm = InterestRateDetailViewModel(seriesId = "DFF", repository = repo)
        val state = vm.screenState.first()
        assertEquals(ScreenState.Loading, state)
    }

    @Test
    fun contentEmissionSurfacesAsContentState() = runTest {
        val repo = FakeEconomicRatesRepository()
        val vm = InterestRateDetailViewModel(seriesId = "DFF", repository = repo)
        dispatcher.scheduler.advanceUntilIdle()

        val sample = InterestRateSeries(
            seriesId = "DFF",
            name = "Federal Funds Rate",
            current = 5.33,
            unit = "%",
            observations = listOf(RateObservation(LocalDate(2026, 5, 25), 5.33)),
            source = "FRED",
        )
        repo.state.value = ScreenState.Content(data = sample, freshness = DataFreshness.FRESH)
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.screenState.first { it is ScreenState.Content }
        assertTrue(state is ScreenState.Content)
        assertSame(sample, state.data)
    }

    @Test
    fun onRetryTriggersStreamRefresh() = runTest {
        val repo = FakeEconomicRatesRepository()
        val vm = InterestRateDetailViewModel(seriesId = "DFF", repository = repo)
        dispatcher.scheduler.advanceUntilIdle()

        repo.refreshTrigger.test {
            vm.onRetry()
            dispatcher.scheduler.advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onRefreshTriggersStreamRefresh() = runTest {
        val repo = FakeEconomicRatesRepository()
        val vm = InterestRateDetailViewModel(seriesId = "DFF", repository = repo)
        dispatcher.scheduler.advanceUntilIdle()

        repo.refreshTrigger.test {
            vm.onRefresh()
            dispatcher.scheduler.advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
