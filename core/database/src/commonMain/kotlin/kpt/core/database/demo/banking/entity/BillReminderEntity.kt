/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.demo.banking.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.model.demo.banking.BillCategory
import kpt.core.model.demo.banking.Recurrence

/**
 * Persistent row for a recurring (or one-time) bill reminder.
 *
 * Mirrors [kpt.core.model.demo.banking.BillReminder]; mapping lives in
 * `core/data/banking/`. Stored locally only — no remote sync.
 *
 * Type-converters in [kpt.core.database.demo.banking.converter.BankingTypeConverters]
 * handle the [Recurrence] and [BillCategory] enum columns (TEXT).
 */
@Entity(tableName = "banking_bill_reminders")
data class BillReminderEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val amount: Double,
    val dueDay: Int,
    val recurrence: Recurrence,
    val category: BillCategory,
    val enabled: Boolean,
    val reminderDaysBefore: Int,
    val createdAtMs: Long,
    val updatedAtMs: Long,
)
