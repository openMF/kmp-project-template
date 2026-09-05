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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.demo.emi.EmiCalculatorRepository
import kpt.core.domain.demo.emi.calculateEmi
import kpt.core.model.demo.emi.EmiResult
import kpt.core.store.demo.emi.impl.EmiParams

/**
 * Fakes [EmiCalculatorRepository] at the [ScreenDataStream] seam.
 *
 * The EMI math itself is the real `calculateEmi` — what is faked is only the Store5 plumbing
 * around it, because the ViewModel's contract is "map inputs to a key, consume the stream's
 * `.state`", not "compute an EMI". Uses the framework's sanctioned
 * `screenDataStreamForTesting` factory rather than reaching for the class's internal
 * constructor.
 */
@OptIn(ExperimentalScreenDataStreamTestingApi::class)
internal class FakeEmiCalculatorRepository : EmiCalculatorRepository {

    override fun emiStream(params: EmiParams, scope: CoroutineScope): ScreenDataStream<EmiResult> {
        val result = calculateEmi(params.principal, params.ratePercent, params.tenureMonths)
        return screenDataStreamForTesting(
            state = MutableStateFlow(ScreenState.Content(result)),
        )
    }
}
