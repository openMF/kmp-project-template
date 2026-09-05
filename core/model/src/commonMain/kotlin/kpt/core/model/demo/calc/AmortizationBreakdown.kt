/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.demo.calc

import kpt.core.model.demo.banking.AmortizationRow
import kpt.core.model.demo.emi.EmiResult

/**
 * One amortization calculation: the per-installment [rows] and the [summary] totals.
 *
 * Both halves derive from the SAME inputs, so they are one Store value rather than two
 * independently-cached ones — a screen can never render a schedule from one parameter set
 * beside a summary from another.
 *
 * Reuses [AmortizationRow] (`core/model/demo/banking`), the same row type `feature/amortization`
 * renders, so the two amortization surfaces agree on one shape.
 */
data class AmortizationBreakdown(
    val rows: List<AmortizationRow>,
    val summary: EmiResult,
)
