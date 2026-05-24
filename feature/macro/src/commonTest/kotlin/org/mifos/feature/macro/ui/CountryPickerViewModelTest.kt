/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.macro.ui

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mifos.core.data.economic.SupportedCountries
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Locks the [CountryPickerViewModel] semantics:
 * - Initial state shows the full curated list, empty search query.
 * - Search filters via [SupportedCountries.search] and is reactive.
 * - Clearing the query restores the full list.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CountryPickerViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateExposesEmptyQueryAndFullList() = runTest {
        val vm = CountryPickerViewModel()
        vm.stateFlow.test {
            val initial = awaitItem()
            assertEquals("", initial.searchQuery)
            assertEquals(SupportedCountries.list, initial.results)
        }
    }

    @Test
    fun searchQueryFiltersResultsViaSupportedCountries() = runTest {
        val vm = CountryPickerViewModel()
        vm.trySendAction(CountryPickerAction.Search("ind"))
        vm.stateFlow.test {
            val state = expectMostRecentItem()
            assertEquals("ind", state.searchQuery)
            assertTrue(state.results.any { it.code == "IN" })
            // No US in results — its name doesn't contain "ind" and its code
            // doesn't start with "IND".
            assertTrue(state.results.none { it.code == "US" })
        }
    }

    @Test
    fun emptyingQueryRestoresFullList() = runTest {
        val vm = CountryPickerViewModel()
        vm.trySendAction(CountryPickerAction.Search("us"))
        vm.trySendAction(CountryPickerAction.Search(""))
        vm.stateFlow.test {
            val state = expectMostRecentItem()
            assertEquals(SupportedCountries.list, state.results)
        }
    }
}
