/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.banking

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.database.demo.banking.dao.LoanDao
import kpt.core.database.demo.banking.entity.LoanEntity
import kpt.core.store.demo.banking.impl.provideLoansStore
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Verifies [provideLoansStore] constructability with an in-memory [FakeLoanDao].
 */
class LoansStoreTest {

    @Test
    fun storeIsConstructable() {
        val store = provideLoansStore(dao = FakeLoanDao())
        assertNotNull(store)
    }
}

// ---------------------------------------------------------------------------
// Fake DAO — in-memory, MutableStateFlow-backed
// ---------------------------------------------------------------------------

private class FakeLoanDao : LoanDao {

    private val loans = MutableStateFlow<List<LoanEntity>>(emptyList())

    override fun observeAll(): Flow<List<LoanEntity>> = loans

    override fun observeById(id: String): Flow<LoanEntity?> = MutableStateFlow(loans.value.firstOrNull { it.id == id })

    override suspend fun getById(id: String): LoanEntity? = loans.value.firstOrNull { it.id == id }

    override fun count(): Flow<Int> = MutableStateFlow(loans.value.size)

    override suspend fun upsert(entity: LoanEntity) {
        loans.value = loans.value
            .filterNot { it.id == entity.id }
            .plus(entity)
    }

    override suspend fun deleteById(id: String) {
        loans.value = loans.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        loans.value = emptyList()
    }
}
