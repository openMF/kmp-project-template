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
import kpt.core.database.banking.dao.LoanDao
import kpt.core.database.banking.entity.LoanEntity
import kpt.core.model.banking.Loan
import org.mobilenativefoundation.store.store5.MutableStore
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
 * The DAO reader is a plain Room `Flow`; Room's InvalidationTracker re-emits to live collectors after writes
 * even when Room 3 alpha05's async InvalidationTracker fails to fan out (see
 * `core-base/database/.../invalidation/README.md`). On Android/Desktop/iOS the wrap
 * is a microsecond no-op alongside Room's native invalidation.
 */
@StoreProvider(id = "loans")
@CacheKey(name = "LIST", key = "loans")
@CacheKey(fn = "item", key = "loan:{id}", params = ["id:String"])
fun provideLoansStore(dao: LoanDao): Store<Unit, List<Loan>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        // Emit the DOMAIN model — the entity→domain map lives in the SourceOfTruth (read-path contract).
        reader = { _: Unit ->
            dao.observeAll().map { rows -> rows.map(LoanEntity::toDomain) }
        },
        writer = { _: Unit, loans: List<Loan> -> loans.forEach { dao.upsert(it.toEntity()) } },
        delete = { _: Unit -> dao.deleteAll() },
        deleteAll = { dao.deleteAll() },
    ),
)

/**
 * Keyed per-loan detail [Store] (`Store<String, Loan>`) — emits the DOMAIN [Loan] for a single id,
 * or no-data (→ Empty) when absent. Backs `LoanRepository.loanDetailStream` so single-loan read
 * screens (e.g. the amortization schedule) consume a `ScreenDataStream` instead of hand-folding a
 * `Flow<Loan?>`. Repository-internal (not DI-registered) — reads the same DAO the list store clears.
 */
// store-binding: internal — repository-internal; LoanRepository calls it directly and clears via the list store's table
/**
 * Single loan by id — `OFFLINE_LOCAL_ONLY`, Room is the authority.
 */
fun provideLoanDetailStore(dao: LoanDao): Store<String, Loan> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { id: String -> dao.observeById(id).map { it?.toDomain() } },
        writer = { _: String, _: Loan -> Unit },
        delete = { id: String -> dao.deleteById(id) },
        deleteAll = { dao.deleteAll() },
    ),
)

/**
 * Per-item WRITE store for loans (keyed by loan id). Every mutation flows through `store.write` /
 * `store.clear`, so the repository never touches the DAO — the SoT writer/delete are the single DAO
 * callers. Local-only ([StoreFactory.createOfflineMutableStore] — no-op Updater); the writer/delete
 * are plain DAO writes so the paired [provideLoansStore] read collectors re-emit on wasmJs.
 */
@StoreProvider(id = "loansMutable", logout = false)
fun provideLoansWriteStore(dao: LoanDao): MutableStore<String, Loan> =
    StoreFactory.createOfflineMutableStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { id: String ->
                dao.observeById(id).map { it?.toDomain() }
            },
            writer = { _: String, loan: Loan ->
                dao.upsert(loan.toEntity())
            },
            delete = { id: String -> dao.deleteById(id) },
            deleteAll = { dao.deleteAll() },
        ),
    )
