/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.store.banking.impl

import org.mifos.core.database.banking.dao.BillReminderDao
import org.mifos.core.database.banking.entity.BillReminderEntity
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import template.core.base.store.infra.StoreFactory

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
 */
fun provideBillRemindersStore(
    dao: BillReminderDao,
): Store<Unit, List<BillReminderEntity>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.observeAll() },
        writer = { _: Unit, reminders: List<BillReminderEntity> ->
            reminders.forEach { dao.upsert(it) }
        },
        delete = { _: Unit -> dao.deleteAll() },
        deleteAll = { dao.deleteAll() },
    ),
)
