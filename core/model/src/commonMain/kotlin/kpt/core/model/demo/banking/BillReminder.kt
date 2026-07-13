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

import kotlinx.serialization.Serializable

/**
 * A recurring (or one-time) bill the user wants to be reminded about.
 * Purely local — no remote sync, no calendar export. The "reminder" itself
 * is delivered by the in-app notification surface; this record is the
 * declarative configuration.
 *
 * Serializable so the offline-resilient `DraftSubmitHandler` can persist the
 * payload across process death even though the "submit" target is a
 * local-only [kpt.core.data.demo.banking.BillReminderRepository] commit.
 *
 * @property id Stable identifier (client-generated UUID). Primary key.
 * @property name Bill label, e.g. "Electricity Bill", "Netflix".
 * @property amount Expected charge amount.
 * @property dueDay Day-of-month the bill is due (1-31). For months without
 *   that day (e.g. day 31 in February), the dashboard clamps to month-end.
 * @property recurrence How often the bill repeats; [Recurrence.ONCE] for
 *   single-occurrence bills.
 * @property category Coarse category used for grouping in the dashboard.
 * @property enabled Whether the reminder fires; disabled reminders persist
 *   but are silent until re-enabled.
 * @property reminderDaysBefore How many days before [dueDay] the reminder
 *   surfaces. Default = 1 (next-day reminder).
 * @property createdAtMs Epoch millis when the reminder was first saved.
 * @property updatedAtMs Epoch millis of the most recent edit.
 */
@Serializable
data class BillReminder(
    val id: String,
    val name: String,
    val amount: Double,
    val dueDay: Int,
    val recurrence: Recurrence,
    val category: BillCategory,
    val enabled: Boolean = true,
    val reminderDaysBefore: Int = 1,
    val createdAtMs: Long,
    val updatedAtMs: Long,
)

/** How often a bill repeats. */
@Serializable
enum class Recurrence {
    MONTHLY,
    QUARTERLY,
    ANNUALLY,
    ONCE,
}

/** Coarse spending category — drives icons and dashboard grouping. */
@Serializable
enum class BillCategory {
    UTILITIES,
    HOUSING,
    TRANSPORT,
    FOOD,
    SUBSCRIPTIONS,
    OTHER,
}
