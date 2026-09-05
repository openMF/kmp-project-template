/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.calc.impl

import kpt.core.base.store.infra.StoreFactory
import kpt.core.model.demo.calc.AmortizationBreakdown
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.Store

/**
 * Store key for one amortization calculation — the calculator's inputs.
 *
 * An amortization schedule for a 240-month loan is a 240-row list rebuilt on every keystroke
 * when it is derived straight off a form `StateFlow`. Keying the computation makes repeats
 * free and, more importantly, puts the result on the standard `ScreenState` read path.
 */
data class AmortizationCalcParams(
    val principal: Double,
    val ratePercent: Double,
    val tenureMonths: Int,
) {
    /** True when the inputs describe a computable loan — the store is only read when valid. */
    val isComputable: Boolean
        get() = principal > 0 && ratePercent > 0 && tenureMonths > 0
}

/**
 * The compute PORT for the amortization store.
 *
 * As with `EmiStore`, `core/store` cannot import `core/domain` (store → domain → data → store
 * would be a cycle), so the schedule/summary computation is bound by the FEATURE module and
 * resolved through Koin at runtime. The port also maps the domain's row shape onto
 * `core/model`'s `AmortizationRow`, so nothing below the feature layer depends on core/domain.
 */
fun interface AmortizationCompute {
    suspend operator fun invoke(params: AmortizationCalcParams): AmortizationBreakdown
}

/**
 * MEMORY_ONLY Store5 store over the amortization computation
 * (`feature_profile.combo_id: calculator_multi`).
 *
 * MEMORY_ONLY is legal only because no `cache_strategy` is declared for this feature (SC2).
 */
fun provideAmortizationCalcStore(
    compute: AmortizationCompute,
): Store<AmortizationCalcParams, AmortizationBreakdown> =
    StoreFactory.createMemoryStore(
        fetcher = Fetcher.of { params: AmortizationCalcParams -> compute(params) },
    )
