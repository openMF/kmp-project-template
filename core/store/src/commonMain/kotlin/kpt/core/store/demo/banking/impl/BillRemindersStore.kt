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

import kotlinx.coroutines.flow.map
import kpt.core.base.database.invalidation.daoFlow
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.demo.banking.dao.BillReminderDao
import kpt.core.database.demo.banking.entity.BillReminderEntity
import kpt.core.model.demo.banking.BillReminder
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Build an offline-only [Store] for recurring bill reminders.
 *
 * Backed exclusively by [BillReminderDao] (OFFLINE_LOCAL_ONLY archetype) — bill
 * reminders are user-created local records with no remote source. The repository
 * layer writes via [BillReminderDao.upsert]; this store surfaces the full list
 * reactively, ordered by day-of-month then creation time (DAO default order).
 *
 * Key: [Unit] — returns all reminders. Filtered reads (e.g., upcoming within N days)
 * are handled in the repository via [BillReminderDao.observeUpcoming].
 *
 * The DAO reader is wrapped with [daoFlow] so wasmJs collectors re-emit after writes
 * even when Room 3 alpha05's async InvalidationTracker fails to fan out (see
 * `core-base/database/.../invalidation/README.md`). On Android/Desktop/iOS the wrap
 * is a microsecond no-op alongside Room's native invalidation.
 */
fun provideBillRemindersStore(dao: BillReminderDao): Store<Unit, List<BillReminder>> =
    StoreFactory.createOfflineStore(
        sourceOfTruth = SourceOfTruth.of(
            // Emit the DOMAIN model — entity→domain map lives in the SourceOfTruth (read-path contract).
            reader = { _: Unit ->
                daoFlow(BILL_REMINDERS_TABLE) { dao.observeAll() }
                    .map { rows -> rows.map(BillReminderEntity::toDomain) }
            },
            writer = { _: Unit, reminders: List<BillReminder> ->
                reminders.forEach { dao.upsert(it.toEntity()) }
            },
            delete = { _: Unit -> dao.deleteAll() },
            deleteAll = { dao.deleteAll() },
        ),
    )

/** Room `@Entity(tableName = …)` for [BillReminderEntity]. Shared with the repository's writes. */
private const val BILL_REMINDERS_TABLE = "banking_bill_reminders"
