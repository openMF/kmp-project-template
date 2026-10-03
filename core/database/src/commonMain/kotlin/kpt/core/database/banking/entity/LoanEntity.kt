/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.banking.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.datetime.LocalDate
import kpt.core.base.database.annotation.DbEntity
import kpt.core.model.banking.LoanKind

/**
 * Persistent row for a personal loan tracked by the user.
 *
 * Mirrors [kpt.core.model.banking.Loan]; mapping lives in the repository
 * layer (`core/data/banking/`). Stored locally only — no remote sync.
 *
 * Type-converters in [kpt.core.database.banking.converter.BankingTypeConverters]
 * handle the [LoanKind] enum (TEXT) and [LocalDate] (ISO-8601 TEXT) columns.
 */
@DbEntity
@Entity(tableName = "banking_loans")
data class LoanEntity(
    /** Client-generated UUID. Primary key. */
    @PrimaryKey
    val id: String,
    /** User-facing label, e.g. "Home Mortgage". */
    val name: String,
    /** Loan category — drives icons and grouping. */
    val kind: LoanKind,
    /** Original loan amount. */
    val principal: Double,
    /** Outstanding balance. User-maintained, not derived from a payment history. */
    val principalRemaining: Double,
    /** APR as a percentage, e.g. `6.5` for 6.5%. */
    val annualRatePercent: Double,
    /**
     * Tenure the loan was taken for, in months. Never changes — [monthsRemaining] is what moves, and the pair is what
     * a progress bar needs.
     */
    val tenureMonths: Int,
    /** Months until payoff. User-maintained. */
    val monthsRemaining: Int,
    /** The EMI. */
    val monthlyPayment: Double,
    /** Next payment due-date. Stored as ISO-8601 text — see `BankingTypeConverters`. */
    val nextDueDate: LocalDate,
    /** Amount paid to date. User-maintained. */
    val totalPaid: Double,
    /** Epoch millis when the loan was added. */
    val createdAtMs: Long,
    /** Epoch millis of the most recent edit. */
    val updatedAtMs: Long,
)
