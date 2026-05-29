/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.model.banking

/**
 * A single monthly row in a reducing-balance amortization schedule.
 *
 * Every row satisfies: [payment] = [principal] + [interest] (within floating-point tolerance).
 *
 * @property month  1-indexed payment number (1 = first payment).
 * @property payment Total payment amount (= EMI, constant for a fixed-rate loan).
 * @property principal Principal component of this payment (grows each month).
 * @property interest  Interest component of this payment (shrinks each month).
 * @property balance   Outstanding principal remaining after this payment.
 */
data class AmortizationRow(
    val month: Int,
    val payment: Double,
    val principal: Double,
    val interest: Double,
    val balance: Double,
)
