/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loans.ui

import kotlinx.datetime.LocalDate
import kpt.core.model.demo.banking.Loan
import kpt.core.model.demo.banking.LoanKind

internal fun sampleLoan(
    id: String,
    name: String = "Loan $id",
    kind: LoanKind = LoanKind.PERSONAL,
    principal: Double = 10_000.0,
    principalRemaining: Double = 7_500.0,
    annualRatePercent: Double = 6.0,
    tenureMonths: Int = 24,
    monthsRemaining: Int = 18,
    monthlyPayment: Double = 443.21,
    nextDueDate: LocalDate = LocalDate(2026, 6, 1),
    totalPaid: Double = 2_500.0,
    createdAtMs: Long = 1_700_000_000_000L,
    updatedAtMs: Long = 1_700_000_000_000L,
): Loan = Loan(
    id = id,
    name = name,
    kind = kind,
    principal = principal,
    principalRemaining = principalRemaining,
    annualRatePercent = annualRatePercent,
    tenureMonths = tenureMonths,
    monthsRemaining = monthsRemaining,
    monthlyPayment = monthlyPayment,
    nextDueDate = nextDueDate,
    totalPaid = totalPaid,
    createdAtMs = createdAtMs,
    updatedAtMs = updatedAtMs,
)
