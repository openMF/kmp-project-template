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
@Entity(tableName = "banking_loans")
data class LoanEntity(
    @PrimaryKey
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
