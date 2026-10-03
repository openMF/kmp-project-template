/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.banking.impl

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.banking.dao.BillReminderDao
import kpt.core.database.banking.entity.BillReminderEntity
import kpt.core.model.banking.BillReminder
import org.mobilenativefoundation.store.store5.MutableStore
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
 * The DAO reader is a plain Room `Flow`; Room's InvalidationTracker re-emits to live collectors after writes
 * even when Room 3 alpha05's async InvalidationTracker fails to fan out (see
 * `core-base/database/.../invalidation/README.md`). On Android/Desktop/iOS the wrap
 * is a microsecond no-op alongside Room's native invalidation.
 */
@StoreProvider(id = "billReminders")
@CacheKey(name = "LIST", key = "billReminders")
@CacheKey(fn = "item", key = "billReminder:{id}", params = ["id:String"])
fun provideBillRemindersStore(dao: BillReminderDao): Store<Unit, List<BillReminder>> =
    StoreFactory.createOfflineStore(
        sourceOfTruth = SourceOfTruth.of(
            // Emit the DOMAIN model — entity→domain map lives in the SourceOfTruth (read-path contract).
            reader = { _: Unit ->
                dao.observeAll()
                    .map { rows -> rows.map(BillReminderEntity::toDomain) }
            },
            writer = { _: Unit, reminders: List<BillReminder> ->
                reminders.forEach { dao.upsert(it.toEntity()) }
            },
            delete = { _: Unit -> dao.deleteAll() },
            deleteAll = { dao.deleteAll() },
        ),
    )

/**
 * Per-item READ store for one bill reminder (keyed by bill id) — the offline-local detail read.
 *
 * Mirrors [kpt.core.store.banking.impl.provideLoanDetailStore]. Repository-internal (not
 * DI-registered): it exists so an edit form can hydrate THROUGH the store instead of calling
 * `repository.getById(...)` straight onto the DAO. That raw read was the
 * S5-2 / S5-10 read-path bypass — the write path already went through a store while the read
 * beside it did not.
 */
// store-binding: internal — repository-internal; see the KDoc above for the S5-2/S5-10 rationale
/**
 * Single bill reminder by id — `OFFLINE_LOCAL_ONLY`, Room is the authority.
 *
 * Keyed by id rather than reusing the list store so a detail screen re-reads only its own row.
 */
fun provideBillReminderDetailStore(dao: BillReminderDao): Store<String, BillReminder> =
    StoreFactory.createOfflineStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { id: String ->
                dao.observeById(id).map { it?.toDomain() }
            },
            writer = { _: String, _: BillReminder -> Unit },
            delete = { id: String -> dao.deleteById(id) },
            deleteAll = { dao.deleteAll() },
        ),
    )

/**
 * Per-item WRITE store for bill reminders (keyed by bill id). Every mutation flows through
 * `store.write` / `store.clear`, so the repository never touches the DAO for writes — the SoT
 * writer/delete are the single DAO callers. Local-only ([StoreFactory.createOfflineMutableStore] —
 * no-op Updater); the writer/delete are plain DAO writes so the paired [provideBillRemindersStore]
 * read collectors (and the repository's DAO-direct the Room `Flow` reads) re-emit on wasmJs.
 */
@StoreProvider(id = "billRemindersMutable", logout = false)
fun provideBillRemindersWriteStore(dao: BillReminderDao): MutableStore<String, BillReminder> =
    StoreFactory.createOfflineMutableStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { id: String ->
                dao.observeById(id).map { it?.toDomain() }
            },
            writer = { _: String, reminder: BillReminder ->
                dao.upsert(reminder.toEntity())
            },
            delete = { id: String -> dao.deleteById(id) },
            deleteAll = { dao.deleteAll() },
        ),
    )
