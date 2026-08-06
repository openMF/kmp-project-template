/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.rates.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kpt.core.base.store.screen.ScreenState
import kpt.core.model.demo.economic.InterestRateSeries
import kpt.core.model.demo.economic.RateObservation
import kpt.core.store.demo.economic.impl.InterestRateSeriesKey
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Locks the [InterestRatesViewModel] contract:
 *
 *  - Each of the 4 series slots evolves **independently** (one card may be in
 *    `Content` while another is still `Loading`).
 *  - `RefreshAll` fans out to all 4 underlying streams.
 *  - Per-row retry only refreshes its own stream.
 *  - `RateSeriesCatalog` shapes the keys passed to the stream factory.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InterestRatesViewModelTest {

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
    fun initialStateHasAllFourSlotsInLoading() = runTest {
        val factory = FakeRateStreamFactory()
        val vm = InterestRatesViewModel(factory)

        val state = vm.stateFlow.first()

        assertEquals(ScreenState.Loading, state.fedFunds)
        assertEquals(ScreenState.Loading, state.prime)
        assertEquals(ScreenState.Loading, state.mortgage30Y)
        assertEquals(ScreenState.Loading, state.treasury10Y)
    }

    @Test
    fun viewModelOpensAStreamForEachCatalogSeries() = runTest {
        val factory = FakeRateStreamFactory()
        InterestRatesViewModel(factory)

        val openedSeries = factory.openedKeys.map { it.seriesId }.toSet()
        assertEquals(
            setOf("DFF", "DPRIME", "MORTGAGE30US", "DGS10"),
            openedSeries,
        )
    }

    @Test
    fun fedFundsStreamEmissionUpdatesOnlyTheFedFundsSlot() = runTest {
        val factory = FakeRateStreamFactory()
        val vm = InterestRatesViewModel(factory)
        // Let `launchIn(viewModelScope)` collectors register against the StateFlow before we emit.
        dispatcher.scheduler.advanceUntilIdle()

        val series = sampleSeries(seriesId = "DFF", name = "Federal Funds Rate", current = 5.33)
        factory.emit(seriesId = "DFF", state = ScreenState.Content(series))
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.stateFlow.first { it.fedFunds is ScreenState.Content }
        val content = state.fedFunds as? ScreenState.Content<InterestRateSeries>
        assertNotNull(content, "fedFunds should now be Content")
        assertSame(series, content.data)
        assertEquals(ScreenState.Loading, state.prime)
        assertEquals(ScreenState.Loading, state.mortgage30Y)
        assertEquals(ScreenState.Loading, state.treasury10Y)
    }

    @Test
    fun rowsEvolveIndependently() = runTest {
        val factory = FakeRateStreamFactory()
        val vm = InterestRatesViewModel(factory)
        dispatcher.scheduler.advanceUntilIdle()

        factory.emit(
            seriesId = "DFF",
            state = ScreenState.Content(sampleSeries(seriesId = "DFF")),
        )
        factory.emit(
            seriesId = "DPRIME",
            state = ScreenState.Error(error = IllegalStateException("boom"), isNetworkError = false),
        )
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.stateFlow.first { current ->
            current.fedFunds is ScreenState.Content && current.prime is ScreenState.Error
        }

        assertTrue(state.fedFunds is ScreenState.Content)
        assertTrue(state.prime is ScreenState.Error)
        // Untouched rows still Loading — independence holds.
        assertEquals(ScreenState.Loading, state.mortgage30Y)
        assertEquals(ScreenState.Loading, state.treasury10Y)
    }

    @Test
    fun refreshAllInvokesEveryStreamRefresh() = runTest {
        val factory = FakeRateStreamFactory()
        val vm = InterestRatesViewModel(factory)
        dispatcher.scheduler.advanceUntilIdle()

        vm.trySendAction(RatesAction.RefreshAll)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, factory.refreshCount(seriesId = "DFF"))
        assertEquals(1, factory.refreshCount(seriesId = "DPRIME"))
        assertEquals(1, factory.refreshCount(seriesId = "MORTGAGE30US"))
        assertEquals(1, factory.refreshCount(seriesId = "DGS10"))
    }

    @Test
    fun perRowRetryRefreshesOnlyThatRow() = runTest {
        val factory = FakeRateStreamFactory()
        val vm = InterestRatesViewModel(factory)
        dispatcher.scheduler.advanceUntilIdle()

        vm.trySendAction(RatesAction.RetryFedFunds)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, factory.refreshCount(seriesId = "DFF"))
        assertEquals(0, factory.refreshCount(seriesId = "DPRIME"))
        assertEquals(0, factory.refreshCount(seriesId = "MORTGAGE30US"))
        assertEquals(0, factory.refreshCount(seriesId = "DGS10"))

        vm.trySendAction(RatesAction.RetryMortgage30Y)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, factory.refreshCount(seriesId = "MORTGAGE30US"))
        assertEquals(0, factory.refreshCount(seriesId = "DPRIME"))
        assertEquals(0, factory.refreshCount(seriesId = "DGS10"))
    }

    @Test
    fun rateSeriesCatalogExposesFourCanonicalEntries() {
        val catalogIds = RateSeriesCatalog.all.map { it.seriesId }
        assertEquals(listOf("DFF", "DPRIME", "MORTGAGE30US", "DGS10"), catalogIds)
        // Every default series asks for a 365-day window — sub-plan 04 detail screen.
        RateSeriesCatalog.all.forEach { assertEquals(365, it.days) }
    }

    @Test
    fun rateSeriesCatalogFindBySeriesIdReturnsCanonicalKeyForKnownIds() {
        assertSame(RateSeriesCatalog.FedFunds, RateSeriesCatalog.findBySeriesId("DFF"))
        assertEquals(null, RateSeriesCatalog.findBySeriesId("UNKNOWN"))
    }

    private fun sampleSeries(seriesId: String, name: String = "Sample", current: Double = 4.5): InterestRateSeries =
        InterestRateSeries(
            seriesId = seriesId,
            name = name,
            current = current,
            unit = "%",
            observations = listOf(
                RateObservation(LocalDate(2026, 5, 23), current),
            ),
            source = "FRED",
        )
}

/**
 * Hand-rolled fake of the feature-private [RateStreamFactory] seam.
 *
 * Records every `open()` call (so the test can assert the VM subscribed to the
 * expected catalog keys), exposes a per-series `emit()` for state transitions,
 * and counts `refresh()` invocations per series id.
 */
private class FakeRateStreamFactory : RateStreamFactory {

    val openedKeys: MutableList<InterestRateSeriesKey> = mutableListOf()
    private val flows: MutableMap<String, MutableStateFlow<ScreenState<InterestRateSeries>>> =
        mutableMapOf()
    private val refreshes: MutableMap<String, Int> = mutableMapOf()

    override fun open(key: InterestRateSeriesKey, scope: CoroutineScope): RateStream {
        openedKeys += key
        val flow = flows.getOrPut(key.seriesId) { MutableStateFlow(ScreenState.Loading) }
        return object : RateStream {
            override val state: Flow<ScreenState<InterestRateSeries>> = flow
            override fun refresh() {
                refreshes[key.seriesId] = (refreshes[key.seriesId] ?: 0) + 1
            }
        }
    }

    fun emit(seriesId: String, state: ScreenState<InterestRateSeries>) {
        flows.getOrPut(seriesId) { MutableStateFlow(ScreenState.Loading) }.value = state
    }

    fun refreshCount(seriesId: String): Int = refreshes[seriesId] ?: 0
}
