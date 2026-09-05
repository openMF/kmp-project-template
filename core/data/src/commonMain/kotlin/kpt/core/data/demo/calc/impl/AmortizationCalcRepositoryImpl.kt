/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.calc.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.demo.calc.AmortizationCalcRepository
import kpt.core.model.demo.calc.AmortizationBreakdown
import kpt.core.store.demo.calc.impl.AmortizationCalcParams
import org.mobilenativefoundation.store.store5.Store

/**
 * Store5-backed impl of [AmortizationCalcRepository].
 *
 * `CACHE_ONLY` — the "fetcher" is a pure local computation, so there is no network leg to
 * gate on and nothing to revalidate.
 */
internal class AmortizationCalcRepositoryImpl(
    private val store: Store<AmortizationCalcParams, AmortizationBreakdown>,
) : AmortizationCalcRepository {

    override fun breakdownStream(
        params: AmortizationCalcParams,
        scope: CoroutineScope,
    ): ScreenDataStream<AmortizationBreakdown> = store.asScreenStream(
        key = params,
        cacheKey = "amortizationCalc:${params.principal}:${params.ratePercent}:${params.tenureMonths}",
        scope = scope,
        fetchPolicy = FetchPolicy.CACHE_ONLY,
    )
}
