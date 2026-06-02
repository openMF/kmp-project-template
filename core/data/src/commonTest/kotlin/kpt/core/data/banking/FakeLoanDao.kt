/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.banking

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kpt.core.database.banking.dao.LoanDao
import kpt.core.database.banking.entity.LoanEntity

/**
 * In-memory fake of [LoanDao] for repository-level unit tests.
 *
 * Preserves the natural sort order (nextDueDate asc, then createdAtMs asc) so
 * tests can assert on ordering without standing up an in-memory Room.
 */
internal class FakeLoanDao : LoanDao {

    private val state = MutableStateFlow<List<LoanEntity>>(emptyList())

    override fun observeAll(): Flow<List<LoanEntity>> = state.map { rows ->
        rows.sortedWith(compareBy({ it.nextDueDate }, { it.createdAtMs }))
    }

    override fun observeById(id: String): Flow<LoanEntity?> =
        state.map { rows -> rows.firstOrNull { it.id == id } }

    override suspend fun getById(id: String): LoanEntity? =
        state.value.firstOrNull { it.id == id }

    override fun count(): Flow<Int> = state.map { it.size }

    override suspend fun upsert(entity: LoanEntity) {
        state.value = state.value.filterNot { it.id == entity.id } + entity
    }

    override suspend fun deleteById(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}
