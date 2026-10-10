/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.calc

import kpt.core.model.banking.AmortizationRow
import kpt.core.model.emi.EmiResult

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
    /** One row per instalment, ordered from the first payment. Length equals the tenure in months. */
    val rows: List<AmortizationRow>,
    /** The totals the rows sum to. Precomputed so a screen need not fold the list to show a header. */
    val summary: EmiResult,
)
