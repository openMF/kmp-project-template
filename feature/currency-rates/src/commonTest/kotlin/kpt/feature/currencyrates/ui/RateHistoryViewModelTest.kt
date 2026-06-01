/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.currencyrates.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.model.currency.RateHistory
import kpt.core.model.currency.RateHistoryKey
import kpt.core.base.store.screen.DataFreshness
import kpt.core.base.store.screen.ScreenState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Locks the [RateHistoryViewModel] contract:
 *
 *  - Defaults: target = INR, period = 30 days (locked by user-visible chip rows).
 *  - Selecting a currency/period mutates only that slice + dispatches a new
 *    [RateHistoryKey] to the stream factory (the VM's `distinctUntilChanged()`
 *    avoids duplicate emissions for no-op selections).
 *  - Initial screen state is Loading.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RateHistoryViewModelTest {

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
    fun defaultLocalStateIsInrThirtyDays() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = RateHistoryViewModel(repo)
        val state = vm.stateFlow.first()
        assertEquals("INR", state.targetCurrency)
        assertEquals(30, state.periodDays)
    }

    @Test
    fun initialScreenStateIsLoading() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = RateHistoryViewModel(repo)
        val screen = vm.screenState.first()
        assertEquals(ScreenState.Loading, screen)
    }

    @Test
    fun firstKeyEmittedMatchesDefaults() = runTest {
        val repo = FakeCurrencyRepository()
        RateHistoryViewModel(repo)
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(repo.rateHistoryKeys.isNotEmpty(), "expected at least one key emission")
        assertEquals(
            RateHistoryKey(from = "USD", to = "INR", days = 30),
            repo.rateHistoryKeys.first(),
        )
    }

    @Test
    fun changingCurrencyEmitsNewKey() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = RateHistoryViewModel(repo)
        dispatcher.scheduler.advanceUntilIdle()
        val before = repo.rateHistoryKeys.size

        vm.trySendAction(HistoryAction.SelectCurrency("EUR"))
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(
            repo.rateHistoryKeys.size > before,
            "expected a new key after currency change",
        )
        assertEquals("EUR", repo.rateHistoryKeys.last().to)
    }

    @Test
    fun changingPeriodEmitsNewKey() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = RateHistoryViewModel(repo)
        dispatcher.scheduler.advanceUntilIdle()
        val before = repo.rateHistoryKeys.size

        vm.trySendAction(HistoryAction.SelectPeriod(7))
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(repo.rateHistoryKeys.size > before)
        assertEquals(7, repo.rateHistoryKeys.last().days)
    }

    @Test
    fun streamContentSurfacesAsContentState() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = RateHistoryViewModel(repo)
        dispatcher.scheduler.advanceUntilIdle()

        repo.rateHistoryState.value = ScreenState.Content(
            data = RateHistory(
                from = "USD",
                to = "INR",
                startDate = "2026-04-25",
                endDate = "2026-05-25",
                rates = emptyList(),
            ),
            freshness = DataFreshness.FRESH,
        )
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.screenState.first { it is ScreenState.Content }
        assertTrue(state is ScreenState.Content)
        assertEquals("INR", state.data.to)
    }
}
