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
import kpt.core.base.database.annotation.DbEntity
import kpt.core.model.banking.BillCategory
import kpt.core.model.banking.Recurrence

/**
 * Persistent row for a recurring (or one-time) bill reminder.
 *
 * Mirrors [kpt.core.model.banking.BillReminder]; mapping lives in
 * `core/data/banking/`. Stored locally only — no remote sync.
 *
 * Type-converters in [kpt.core.database.banking.converter.BankingTypeConverters]
 * handle the [Recurrence] and [BillCategory] enum columns (TEXT).
 */
@DbEntity
@Entity(tableName = "banking_bill_reminders")
data class BillReminderEntity(
    /** Client-generated UUID. Primary key. */
    @PrimaryKey
    val id: String,
    /** Bill label, e.g. "Electricity Bill". */
    val name: String,
    /** Amount due per occurrence. */
    val amount: Double,
    /** Day of the month it falls due, 1–31. Clamped to the month's length when shorter. */
    val dueDay: Int,
    /** How often it repeats. */
    val recurrence: Recurrence,
    /** Spending category, for grouping and icons. */
    val category: BillCategory,
    /** Whether reminders fire. A disabled bill is still listed. */
    val enabled: Boolean,
    /** How many days ahead of [dueDay] to notify. */
    val reminderDaysBefore: Int,
    /** Epoch millis when the reminder was added. */
    val createdAtMs: Long,
    /** Epoch millis of the most recent edit. */
    val updatedAtMs: Long,
)
