/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.emicalculator.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.model.demo.emi.EmiResult
import kotlin.math.abs
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Locks the [EmiCalculatorViewModel] contract:
 *
 *  - Default state has non-zero principal/rate/tenure (so the result is Content at first
 *    composition rather than blank).
 *  - Each `Update*` action mutates only its own state slice.
 *  - `emiState` is `ScreenState.Empty` whenever any of the three inputs is non-positive —
 *    an in-progress form is Empty, never Error (the screen renders the Empty slot).
 *  - Standard EMI math: `P × r × (1+r)^n / ((1+r)^n − 1)` with monthly r.
 *
 * The repository is faked at the [ScreenDataStream] seam rather than mocking Store5 — the
 * ViewModel's whole contract is "map inputs to a key, consume the stream's `.state`".
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EmiCalculatorViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private fun viewModel() = EmiCalculatorViewModel(FakeEmiCalculatorRepository())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun defaultStateHasPopulatedInputs() = runTest {
        val vm = viewModel()
        val state = vm.stateFlow.first()
        assertTrue(state.principal > 0, "default principal should be > 0")
        assertTrue(state.ratePercent > 0, "default rate should be > 0")
        assertTrue(state.tenureMonths > 0, "default tenure should be > 0")
    }

    @Test
    fun updateActionsMutateCorrectSliceOnly() = runTest {
        val vm = viewModel()

        vm.trySendAction(EmiAction.UpdatePrincipal(250_000.0))
        vm.trySendAction(EmiAction.UpdateRate(7.5))
        vm.trySendAction(EmiAction.UpdateTenure(120))
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.stateFlow.first()
        assertEquals(250_000.0, state.principal)
        assertEquals(7.5, state.ratePercent)
        assertEquals(120, state.tenureMonths)
    }

    @Test
    fun emiStateIsEmptyWhenPrincipalIsZero() = runTest {
        val vm = viewModel()
        vm.trySendAction(EmiAction.UpdatePrincipal(0.0))
        dispatcher.scheduler.advanceUntilIdle()

        // Skip the stateIn seed (Loading) and assert the first SETTLED state.
        val state = vm.emiState.first { it != ScreenState.Loading }
        assertEquals(ScreenState.Empty, state, "an incomplete form is Empty, not Error")
    }

    @Test
    fun emiStateIsEmptyWhenTenureIsZero() = runTest {
        val vm = viewModel()
        vm.trySendAction(EmiAction.UpdateTenure(0))
        dispatcher.scheduler.advanceUntilIdle()

        // Skip the stateIn seed (Loading) and assert the first SETTLED state.
        val state = vm.emiState.first { it != ScreenState.Loading }
        assertEquals(ScreenState.Empty, state, "an incomplete form is Empty, not Error")
    }

    @Test
    fun emiComputesCorrectlyFor100kAt12PercentFor10Months() = runTest {
        val vm = viewModel()
        vm.trySendAction(EmiAction.UpdatePrincipal(100_000.0))
        vm.trySendAction(EmiAction.UpdateRate(12.0))
        vm.trySendAction(EmiAction.UpdateTenure(10))
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.emiState.first {
            val s = vm.stateFlow.value
            it is ScreenState.Content &&
                s.principal == 100_000.0 && s.ratePercent == 12.0 && s.tenureMonths == 10
        }
        val result = assertIs<ScreenState.Content<EmiResult>>(state).data
        // P=100k, r=0.01/mo, n=10 → EMI ≈ 10,558.20
        assertTrue(
            abs(result.emi - 10_558.20) <= 1.0,
            "EMI should be ≈10,558.20 — was ${result.emi}",
        )
        assertTrue(
            result.totalInterest > 0,
            "interest should be positive for a non-zero rate",
        )
    }
}
