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

import org.mifos.core.database.banking.dao.LoanDao
import org.mifos.core.database.banking.entity.LoanEntity
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import template.core.base.store.infra.StoreFactory

/**
 * Build an offline-only [Store] for tracked personal loans.
 *
 * Backed exclusively by [LoanDao] (OFFLINE_LOCAL_ONLY archetype) — loans are
 * user-managed local records with no remote sync. The repository layer writes
 * via [LoanDao.upsert]; this store surfaces the full list reactively.
 *
 * Key: [Unit] — returns all loans sorted by soonest due date (DAO default order).
 * Per-loan observation (`observeById`) is handled directly by the repository.
 */
fun provideLoansStore(
    dao: LoanDao,
): Store<Unit, List<LoanEntity>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.observeAll() },
        writer = { _: Unit, loans: List<LoanEntity> -> loans.forEach { dao.upsert(it) } },
        delete = { _: Unit -> dao.deleteAll() },
        deleteAll = { dao.deleteAll() },
    ),
)
