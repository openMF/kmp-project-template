/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.banking.impl

import kpt.core.database.demo.banking.entity.BillReminderEntity
import kpt.core.model.demo.banking.BillReminder

/**
 * Entity ⇄ domain mappers for the bill-reminders feature. They live in `core/store` (the lowest
 * layer that sees BOTH the Room [BillReminderEntity] and the domain [BillReminder]) so
 * [provideBillRemindersStore]'s `SourceOfTruth` can emit the DOMAIN model per the read-path
 * contract; `BillReminderRepositoryImpl` consumes them for its DAO-direct reads + write path.
 */
fun BillReminderEntity.toDomain(): BillReminder = BillReminder(
    id = id,
    name = name,
    amount = amount,
    dueDay = dueDay,
    recurrence = recurrence,
    category = category,
    enabled = enabled,
    reminderDaysBefore = reminderDaysBefore,
    createdAtMs = createdAtMs,
    updatedAtMs = updatedAtMs,
)

/** @see toDomain — the persistable inverse used by the repository's write path. */
fun BillReminder.toEntity(): BillReminderEntity = BillReminderEntity(
    id = id,
    name = name,
    amount = amount,
    dueDay = dueDay,
    recurrence = recurrence,
    category = category,
    enabled = enabled,
    reminderDaysBefore = reminderDaysBefore,
    createdAtMs = createdAtMs,
    updatedAtMs = updatedAtMs,
)
