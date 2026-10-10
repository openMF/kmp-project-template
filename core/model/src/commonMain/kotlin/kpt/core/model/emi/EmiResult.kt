/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.emi

/**
 * Output of an EMI calculation: the monthly instalment plus the totals it implies.
 */
data class EmiResult(
    /** The monthly instalment. */
    val emi: Double,
    /** Principal plus interest over the full tenure. */
    val totalPayment: Double,
    /** Interest alone — [totalPayment] minus the principal. */
    val totalInterest: Double,
)
