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

import kpt.core.base.database.invalidation.daoFlow
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.banking.dao.LoanDao
import kpt.core.database.banking.entity.LoanEntity
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Build an offline-only [Store] for tracked personal loans.
 *
 * Backed exclusively by [LoanDao] (OFFLINE_LOCAL_ONLY archetype) — loans are
 * user-managed local records with no remote sync. The repository layer writes
 * via [LoanDao.upsert]; this store surfaces the full list reactively.
 *
 * Key: [Unit] — returns all loans sorted by soonest due date (DAO default order).
 * Per-loan observation (`observeById`) is handled directly by the repository.
 *
 * The DAO reader is wrapped with [daoFlow] so wasmJs collectors re-emit after writes
 * even when Room 3 alpha05's async InvalidationTracker fails to fan out (see
 * `core-base/database/.../invalidation/README.md`). On Android/Desktop/iOS the wrap
 * is a microsecond no-op alongside Room's native invalidation.
 */
fun provideLoansStore(dao: LoanDao): Store<Unit, List<LoanEntity>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> daoFlow(LOANS_TABLE) { dao.observeAll() } },
        writer = { _: Unit, loans: List<LoanEntity> -> loans.forEach { dao.upsert(it) } },
        delete = { _: Unit -> dao.deleteAll() },
        deleteAll = { dao.deleteAll() },
    ),
)

/** Room `@Entity(tableName = …)` for [LoanEntity]. Shared with the repository's writes. */
private const val LOANS_TABLE = "banking_loans"
