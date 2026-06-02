/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.wizard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kpt.core.base.store.submit.SubmitOutbox
import kpt.core.base.store.submit.SubmitOutboxEntry
import kpt.core.base.store.submit.SubmitOutboxStatus

/**
 * In-memory fake of [SubmitOutbox] for unit tests.
 *
 * Mirrors the contract used by [LoanCalcWizardViewModel]:
 * - `saveByUniqueKey` upserts on `(formKey, uniqueKey)`.
 * - `getPendingByUniqueKey` reads the stored payload.
 * - `deleteByUniqueKey` removes it.
 *
 * Other methods return safe defaults — the wizard only exercises the three
 * methods above.
 */
internal class FakeSubmitOutbox<P> : SubmitOutbox<P> {

    private val state = MutableStateFlow<List<SubmitOutboxEntry<P>>>(emptyList())
    private var nextId: Long = 1

    override suspend fun save(formKey: String, payload: P): Long =
        upsert(formKey = formKey, uniqueKey = null, payload = payload)

    override suspend fun saveByUniqueKey(formKey: String, uniqueKey: String, payload: P): Long =
        upsert(formKey = formKey, uniqueKey = uniqueKey, payload = payload)

    private fun upsert(formKey: String, uniqueKey: String?, payload: P): Long {
        val existing = state.value.firstOrNull {
            it.formKey == formKey && it.uniqueKey == uniqueKey && it.status == SubmitOutboxStatus.PENDING
        }
        val id = existing?.id ?: nextId++
        val entry = SubmitOutboxEntry(
            id = id,
            formKey = formKey,
            uniqueKey = uniqueKey,
            payload = payload,
            status = SubmitOutboxStatus.PENDING,
            createdAtMs = 0L,
            errorMessage = null,
        )
        state.value = state.value.filterNot { it.id == id } + entry
        return id
    }

    override suspend fun getPending(formKey: String): SubmitOutboxEntry<P>? =
        state.value.firstOrNull {
            it.formKey == formKey && it.uniqueKey == null && it.status == SubmitOutboxStatus.PENDING
        }

    override suspend fun getPendingByUniqueKey(
        formKey: String,
        uniqueKey: String,
    ): SubmitOutboxEntry<P>? =
        state.value.firstOrNull {
            it.formKey == formKey &&
                it.uniqueKey == uniqueKey &&
                it.status == SubmitOutboxStatus.PENDING
        }

    override fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?> =
        state.map { rows ->
            rows.firstOrNull {
                it.formKey == formKey &&
                    it.uniqueKey == null &&
                    it.status == SubmitOutboxStatus.PENDING
            }
        }

    override fun observePendingByUniqueKey(
        formKey: String,
        uniqueKey: String,
    ): Flow<SubmitOutboxEntry<P>?> =
        state.map { rows ->
            rows.firstOrNull {
                it.formKey == formKey &&
                    it.uniqueKey == uniqueKey &&
                    it.status == SubmitOutboxStatus.PENDING
            }
        }

    override fun observeAllByFormKey(formKey: String): Flow<List<SubmitOutboxEntry<P>>> =
        state.map { rows ->
            rows.filter {
                it.formKey == formKey && it.status != SubmitOutboxStatus.SUBMITTED
            }
        }

    override suspend fun getAllPending(): List<SubmitOutboxEntry<P>> =
        state.value.filter { it.status == SubmitOutboxStatus.PENDING }

    override suspend fun markRetrying(id: Long) {
        mutate(id) { copy(status = SubmitOutboxStatus.RETRYING) }
    }

    override suspend fun markSubmitted(id: Long) {
        mutate(id) { copy(status = SubmitOutboxStatus.SUBMITTED) }
    }

    override suspend fun markFailed(id: Long, error: String?) {
        mutate(id) { copy(status = SubmitOutboxStatus.FAILED, errorMessage = error) }
    }

    override suspend fun deleteByFormKey(formKey: String) {
        state.value = state.value.filterNot { it.formKey == formKey }
    }

    override suspend fun deleteByUniqueKey(formKey: String, uniqueKey: String) {
        state.value = state.value.filterNot {
            it.formKey == formKey && it.uniqueKey == uniqueKey
        }
    }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }

    private fun mutate(id: Long, mutator: SubmitOutboxEntry<P>.() -> SubmitOutboxEntry<P>) {
        state.value = state.value.map { if (it.id == id) it.mutator() else it }
    }
}
