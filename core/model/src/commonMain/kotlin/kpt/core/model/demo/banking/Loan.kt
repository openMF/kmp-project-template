/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.demo.banking

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * A personal loan tracked by the user — purely local, no remote sync.
 *
 * This is a *tracker*, not a live ledger: [principalRemaining], [monthsRemaining]
 * and [totalPaid] reflect what the user has entered, not values computed from a
 * payment history. Users update these fields when they want the numbers to be
 * accurate. A future "log a payment" feature can recompute these from a ledger.
 *
 * Serializable so the offline-resilient `DraftSubmitHandler` can persist the
 * payload across process death even though the "submit" target here is a
 * local-only [kpt.core.data.demo.banking.LoanRepository] commit.
 *
 * @property id Stable identifier (client-generated UUID). Primary key.
 * @property name User-facing label (e.g. "Home Mortgage", "Auto Loan").
 * @property kind Loan category — drives icons and grouping in the UI.
 * @property principal Original loan amount.
 * @property principalRemaining Current outstanding balance (user-maintained).
 * @property annualRatePercent APR as a percentage value (e.g. `6.5` = 6.5%).
 * @property tenureMonths Original loan tenure in months.
 * @property monthsRemaining Remaining months until payoff (user-maintained).
 * @property monthlyPayment Equated Monthly Installment (EMI).
 * @property nextDueDate Next payment due-date.
 * @property totalPaid Total amount paid to-date (user-maintained).
 * @property createdAtMs Epoch millis when the loan was added.
 * @property updatedAtMs Epoch millis of the most recent edit.
 */
@Serializable
data class Loan(
    val id: String,
    val name: String,
    val kind: LoanKind,
    val principal: Double,
    val principalRemaining: Double,
    val annualRatePercent: Double,
    val tenureMonths: Int,
    val monthsRemaining: Int,
    val monthlyPayment: Double,
    val nextDueDate: LocalDate,
    val totalPaid: Double,
    val createdAtMs: Long,
    val updatedAtMs: Long,
)

/** High-level loan category used for grouping, icons, and analytics. */
@Serializable
enum class LoanKind {
    PERSONAL,
    MORTGAGE,
    AUTO,
    STUDENT,
    BUSINESS,
    OTHER,
}
