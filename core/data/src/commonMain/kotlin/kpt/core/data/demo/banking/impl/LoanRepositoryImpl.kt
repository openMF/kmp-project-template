/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.banking.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kpt.core.base.database.invalidation.daoFlow
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.demo.banking.LoanRepository
import kpt.core.database.demo.banking.dao.LoanDao
import kpt.core.model.demo.banking.Loan
import kpt.core.store.AppCacheKeys
import kpt.core.store.demo.banking.impl.provideLoanDetailStore
import kpt.core.store.demo.banking.impl.toDomain
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreWriteRequest

/**
 * Local-only impl of [LoanRepository].
 *
 * Read-path contract: [loansStream] builds the offline-local [ScreenDataStream] (CACHE_ONLY) over
 * the domain-emitting [kpt.core.store.demo.banking.impl.provideLoansStore]; read screens consume
 * `.state`. Direct-DAO `Flow` reads (`observeById`, the dashboard aggregates) are wrapped with
 * [daoFlow] and writes with [notifyingWrite] so the wasmJs target's long-lived collectors re-emit
 * after writes even when Room 3 alpha05's async InvalidationTracker fails to fan out (no-op on
 * Android/Desktop/iOS). See `core-base/database/.../invalidation/README.md`.
 */
internal class LoanRepositoryImpl(
    private val loansStore: Store<Unit, List<Loan>>,
    private val loansWriteStore: MutableStore<String, Loan>,
    private val loanDao: LoanDao,
) : LoanRepository {

    override fun loansStream(scope: CoroutineScope): ScreenDataStream<List<Loan>> =
        loansStore.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.LOANS,
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
            isEmpty = { it.isEmpty() },
        )

    // Repository-internal keyed detail store — a single loan as a ScreenDataStream (absent id → Empty).
    private val loanDetailStore = provideLoanDetailStore(loanDao)

    override fun loanDetailStream(id: String, scope: CoroutineScope): ScreenDataStream<Loan> =
        loanDetailStore.asScreenStream(
            key = id,
            cacheKey = AppCacheKeys.loan(id),
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
        )

    override fun observeById(id: String): Flow<Loan?> =
        daoFlow(LOANS_TABLE) { loanDao.observeById(id) }.map { it?.toDomain() }

    override suspend fun getById(id: String): Loan? = loanDao.getById(id)?.toDomain()

    override suspend fun upsert(loan: Loan) {
        // Write through the store — persists to the Room SoT (via the SoT writer); the read store re-emits.
        loansWriteStore.write(
            StoreWriteRequest.of<String, Loan, Any>(key = loan.id, value = loan),
        )
    }

    override suspend fun delete(id: String) {
        // Clear through the store — removes the row from the Room SoT (via the SoT delete).
        loansWriteStore.clear(id)
    }

    override fun observeTotalMonthlyEmi(): Flow<Double> = daoFlow(LOANS_TABLE) { loanDao.observeAll() }.map { rows ->
        rows.sumOf { it.monthlyPayment }
    }

    override fun observeTotalPrincipalRemaining(): Flow<Double> =
        daoFlow(LOANS_TABLE) { loanDao.observeAll() }.map { rows ->
            rows.sumOf { it.principalRemaining }
        }

    override fun observeCount(): Flow<Int> = daoFlow(LOANS_TABLE) { loanDao.count() }

    private companion object {
        /** Room `@Entity(tableName = …)` for [kpt.core.database.demo.banking.entity.LoanEntity]. */
        const val LOANS_TABLE = "banking_loans"
    }
}
