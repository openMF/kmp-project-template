/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.currencyrates.ui

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mifos.core.model.currency.ExchangeRates
import template.core.base.store.screen.DataFreshness
import template.core.base.store.screen.ScreenState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Locks the [CurrencyRatesViewModel] contract:
 *
 *  - Opens its exchange-rates stream with `base = "USD"` (hard-coded — see VM).
 *  - Search action filters the rates map case-insensitively.
 *  - Empty filtered result → Empty screen state (via `emptyIfContent`).
 *  - `onRetry()` and `onRefresh()` both fan out to the underlying stream.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyRatesViewModelTest {

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
    fun viewModelOpensExchangeRatesStreamWithUsdBase() = runTest {
        val repo = FakeCurrencyRepository()
        CurrencyRatesViewModel(repo)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals("USD", repo.lastExchangeRatesBase)
    }

    @Test
    fun initialScreenStateIsLoading() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = CurrencyRatesViewModel(repo)
        val state = vm.screenState.first()
        assertEquals(ScreenState.Loading, state)
    }

    @Test
    fun searchFiltersRatesByCodeCaseInsensitively() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = CurrencyRatesViewModel(repo)
        repo.exchangeRatesState.value = ScreenState.Content(
            data = ExchangeRates(
                base = "USD",
                date = "2026-05-25",
                rates = mapOf("EUR" to 0.92, "INR" to 83.5, "GBP" to 0.79),
            ),
            freshness = DataFreshness.FRESH,
        )
        dispatcher.scheduler.advanceUntilIdle()

        vm.screenState.test {
            // Drain initial Loading + Content-with-all-rates.
            awaitItem()

            vm.trySendAction(RatesAction.Search("eur"))
            dispatcher.scheduler.advanceUntilIdle()

            val filtered = expectMostRecentItem()
            assertTrue(filtered is ScreenState.Content)
            assertEquals(setOf("EUR"), filtered.data.rates.keys)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchWithNoMatchesYieldsEmptyState() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = CurrencyRatesViewModel(repo)
        repo.exchangeRatesState.value = ScreenState.Content(
            data = ExchangeRates(
                base = "USD",
                date = "2026-05-25",
                rates = mapOf("EUR" to 0.92, "INR" to 83.5),
            ),
            freshness = DataFreshness.FRESH,
        )
        dispatcher.scheduler.advanceUntilIdle()

        vm.trySendAction(RatesAction.Search("ZZZ"))
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.screenState.first { it is ScreenState.Empty }
        assertTrue(state is ScreenState.Empty)
    }

    @Test
    fun onRetryTriggersStreamRefresh() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = CurrencyRatesViewModel(repo)
        dispatcher.scheduler.advanceUntilIdle()

        repo.exchangeRatesRefresh.test {
            vm.onRetry()
            dispatcher.scheduler.advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onRefreshTriggersStreamRefresh() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = CurrencyRatesViewModel(repo)
        dispatcher.scheduler.advanceUntilIdle()

        repo.exchangeRatesRefresh.test {
            vm.onRefresh()
            dispatcher.scheduler.advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
