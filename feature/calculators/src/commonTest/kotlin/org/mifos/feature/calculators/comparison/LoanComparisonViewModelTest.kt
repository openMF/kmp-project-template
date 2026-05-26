/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.calculators.comparison

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoanComparisonViewModelTest {

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
    fun defaultStateHasThreeStaggeredScenarios() = runTest {
        val vm = LoanComparisonViewModel()
        val state = vm.stateFlow.value
        assertEquals(3, state.scenarios.size)
    }

    @Test
    fun updateScenarioReplacesOnlyThatIndex() = runTest {
        val vm = LoanComparisonViewModel()
        val replacement = LoanScenario(principal = 1.0, ratePercent = 1.0, tenureMonths = 1)
        vm.trySendAction(LoanComparisonAction.UpdateScenario(index = 1, scenario = replacement))
        dispatcher.scheduler.advanceUntilIdle()
        val updated = vm.stateFlow.value.scenarios
        assertEquals(replacement, updated[1])
        assertTrue(updated[0] != replacement)
        assertTrue(updated[2] != replacement)
    }

    @Test
    fun analysisIdentifiesCheapestByTotalPayable() = runTest {
        val vm = LoanComparisonViewModel()
        vm.trySendAction(
            LoanComparisonAction.UpdateScenario(
                0,
                LoanScenario(principal = 100_000.0, ratePercent = 5.0, tenureMonths = 60),
            ),
        )
        vm.trySendAction(
            LoanComparisonAction.UpdateScenario(
                1,
                LoanScenario(principal = 100_000.0, ratePercent = 9.0, tenureMonths = 120),
            ),
        )
        vm.trySendAction(
            LoanComparisonAction.UpdateScenario(
                2,
                LoanScenario(principal = 100_000.0, ratePercent = 12.0, tenureMonths = 240),
            ),
        )
        dispatcher.scheduler.advanceUntilIdle()

        // Wait until the analysis re-computes for the latest scenarios + identifies
        // scenario 0 as cheapest (lowest rate × shortest tenure).
        val latest = vm.analysis.first { it.cheapestIndex == 0 && it.results.size == 3 }
        assertEquals(0, latest.cheapestIndex)
    }
}
