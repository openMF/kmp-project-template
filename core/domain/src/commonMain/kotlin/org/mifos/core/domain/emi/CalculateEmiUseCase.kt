/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.domain.emi

import org.mifos.core.model.emi.EmiResult
import kotlin.math.pow

/**
 * Calculates EMI (Equated Monthly Installment) using the standard formula:
 * EMI = P × r × (1+r)^n / ((1+r)^n - 1)
 *
 * @param principal Loan amount
 * @param annualRatePercent Annual interest rate in percentage
 * @param tenureMonths Number of months
 * @return [EmiResult] with monthly EMI, total payment, and total interest
 */
fun calculateEmi(principal: Double, annualRatePercent: Double, tenureMonths: Int): EmiResult {
    val monthlyRate = annualRatePercent / 12.0 / 100.0
    val n = tenureMonths.toDouble()
    val emi = if (monthlyRate == 0.0) {
        principal / n
    } else {
        val factor = (1 + monthlyRate).pow(n)
        principal * monthlyRate * factor / (factor - 1)
    }
    val totalPayment = emi * tenureMonths
    return EmiResult(
        emi = emi,
        totalPayment = totalPayment,
        totalInterest = totalPayment - principal,
    )
}
