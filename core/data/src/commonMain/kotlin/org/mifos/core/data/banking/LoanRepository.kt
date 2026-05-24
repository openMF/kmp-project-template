/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.banking

import kotlinx.coroutines.flow.Flow
import org.mifos.core.model.banking.Loan

/**
 * User's personal loan portfolio — purely local persistence, no remote sync.
 *
 * Backs the B1 Loan Tracker feature. Reads are reactive [Flow]s; writes are
 * `suspend` and go through [upsert] / [delete]. Edit UX wraps these in
 * `DraftSubmitHandler` so the "saving…" badge + retry-on-failure polish
 * applies even though the "submit" is a local commit with no network call.
 */
interface LoanRepository {

    /** Observe all loans, soonest-due first. */
    fun observeAll(): Flow<List<Loan>>

    /** Observe a single loan. Emits `null` after deletion. */
    fun observeById(id: String): Flow<Loan?>

    /** One-shot read. `null` if the loan was never saved or has been deleted. */
    suspend fun getById(id: String): Loan?

    /** Insert-or-replace by [Loan.id]. Idempotent. */
    suspend fun upsert(loan: Loan)

    /** Delete by id. No-op if absent. */
    suspend fun delete(id: String)

    /**
     * Sum of every loan's [Loan.monthlyPayment]. Useful for the dashboard
     * "Total Monthly EMI" tile. Emits whenever any loan changes.
     */
    fun observeTotalMonthlyEmi(): Flow<Double>

    /**
     * Sum of every loan's [Loan.principalRemaining]. Useful for the dashboard
     * "Total Outstanding" tile. Emits whenever any loan changes.
     */
    fun observeTotalPrincipalRemaining(): Flow<Double>

    /** Reactive row count — used by the dashboard badge. */
    fun observeCount(): Flow<Int>
}
