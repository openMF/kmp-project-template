/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.banking.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.datetime.LocalDate
import kpt.core.model.banking.BillCategory
import kpt.core.model.banking.LoanKind
import kpt.core.model.banking.Recurrence

/**
 * Room 3 [TypeConverter] collection for the banking-domain tables
 * (`banking_loans`, `banking_bill_reminders`).
 *
 * - Enums ([LoanKind], [Recurrence], [BillCategory]) persist as their `name` (TEXT).
 *   Adding a new enum entry is forward-compatible; removing/renaming an entry
 *   requires a data migration (round-trip test in
 *   `BankingTypeConvertersTest` guards regressions).
 * - [LocalDate] persists as ISO-8601 string (`YYYY-MM-DD`) via
 *   [kotlinx.datetime.LocalDate]'s default `toString` / `parse`.
 *
 * Registered on [kpt.core.database.AppDatabase] via `@ColumnTypeConverters`.
 */
class BankingTypeConverters {

    // --- LoanKind ---

    @ColumnTypeConverter
    fun fromLoanKind(value: LoanKind): String = value.name

    @ColumnTypeConverter
    fun toLoanKind(value: String): LoanKind = LoanKind.valueOf(value)

    // --- Recurrence ---

    @ColumnTypeConverter
    fun fromRecurrence(value: Recurrence): String = value.name

    @ColumnTypeConverter
    fun toRecurrence(value: String): Recurrence = Recurrence.valueOf(value)

    // --- BillCategory ---

    @ColumnTypeConverter
    fun fromBillCategory(value: BillCategory): String = value.name

    @ColumnTypeConverter
    fun toBillCategory(value: String): BillCategory = BillCategory.valueOf(value)

    // --- LocalDate (ISO-8601 string) ---

    @ColumnTypeConverter
    fun fromLocalDate(value: LocalDate): String = value.toString()

    @ColumnTypeConverter
    fun toLocalDate(value: String): LocalDate = LocalDate.parse(value)
}
