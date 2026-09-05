/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.calc

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.demo.calc.AmortizationBreakdown
import kpt.core.store.demo.calc.impl.AmortizationCalcParams

/** Read surface for the amortization calculator (`calculator_multi`, MEMORY_ONLY). */
interface AmortizationCalcRepository {

    /** A [ScreenDataStream] over the breakdown computed for [params]. */
    fun breakdownStream(
        params: AmortizationCalcParams,
        scope: CoroutineScope,
    ): ScreenDataStream<AmortizationBreakdown>
}
