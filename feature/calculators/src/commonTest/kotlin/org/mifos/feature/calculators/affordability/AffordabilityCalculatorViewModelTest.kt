/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.calculators.affordability

import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AffordabilityCalculatorViewModelTest {

    @Test
    fun stateUpdatesPropagateThroughActions() = runTest {
        val vm = AffordabilityCalculatorViewModel()
        vm.trySendAction(AffordabilityAction.UpdateIncome(20_000.0))
        vm.trySendAction(AffordabilityAction.UpdateObligations(2_000.0))
        vm.trySendAction(AffordabilityAction.UpdateDti(0.5))
        vm.trySendAction(AffordabilityAction.UpdateRate(6.5))
        vm.trySendAction(AffordabilityAction.UpdateTenure(360))

        val state = vm.stateFlow.first()
        assertEquals(20_000.0, state.monthlyIncome)
        assertEquals(2_000.0, state.monthlyObligations)
        assertEquals(0.5, state.dtiRatio)
        assertEquals(6.5, state.ratePercent)
        assertEquals(360, state.tenureMonths)
    }

    @Test
    fun affordabilityFlowReactsToInputChanges() = runTest {
        val vm = AffordabilityCalculatorViewModel()
        vm.affordability.test {
            // Skip whatever the StateFlow's initial emission is.
            awaitItem()

            vm.trySendAction(AffordabilityAction.UpdateIncome(10_000.0))
            vm.trySendAction(AffordabilityAction.UpdateObligations(0.0))
            vm.trySendAction(AffordabilityAction.UpdateDti(0.4))
            vm.trySendAction(AffordabilityAction.UpdateRate(6.0))
            vm.trySendAction(AffordabilityAction.UpdateTenure(360))

            // Drain to the latest emission.
            val latest = expectMostRecentItem()
            // 40% × 10 000 − 0 = 4 000.
            assertTrue(
                abs(4_000.0 - latest.maxEmi) <= 0.01,
                "Max EMI should be 4 000, was ${latest.maxEmi}",
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
