/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.amortizationcalc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.demo.calc.AmortizationCalcRepository
import kpt.core.domain.demo.calc.amortizationSchedule
import kpt.core.domain.demo.calc.computeEmi
import kpt.core.model.demo.banking.AmortizationRow
import kpt.core.model.demo.calc.AmortizationBreakdown
import kpt.core.store.demo.calc.impl.AmortizationCalcParams

/**
 * Fakes [AmortizationCalcRepository] at the [ScreenDataStream] seam.
 *
 * The schedule/summary math is the REAL domain computation — what is faked is only the Store5
 * plumbing, mirroring the production `AmortizationCompute` binding in `CalculatorsModule`.
 */
@OptIn(ExperimentalScreenDataStreamTestingApi::class)
internal class FakeAmortizationCalcRepository : AmortizationCalcRepository {

    override fun breakdownStream(
        params: AmortizationCalcParams,
        scope: CoroutineScope,
    ): ScreenDataStream<AmortizationBreakdown> {
        val breakdown = AmortizationBreakdown(
            rows = amortizationSchedule(
                params.principal,
                params.ratePercent,
                params.tenureMonths,
            ).map { row ->
                AmortizationRow(
                    month = row.installmentNumber,
                    payment = row.principalPaid + row.interestPaid,
                    principal = row.principalPaid,
                    interest = row.interestPaid,
                    balance = row.balanceRemaining,
                )
            },
            summary = computeEmi(params.principal, params.ratePercent, params.tenureMonths),
        )
        return screenDataStreamForTesting(state = MutableStateFlow(ScreenState.Content(breakdown)))
    }
}
