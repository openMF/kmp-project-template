/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.cloudtodo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.demo.cloudtodo.impl.CloudTodoRepositoryImpl
import kpt.core.data.infra.testMutationGateway
import kpt.core.database.demo.cloudtodo.CloudTodoDao
import kpt.core.database.demo.cloudtodo.CloudTodoEntity
import kpt.core.model.demo.cloudtodo.CloudTodo
import kpt.core.network.demo.cloudtodo.api.JsonPlaceholderApi
import kpt.core.network.demo.cloudtodo.dto.CloudTodoDto
import kpt.core.store.demo.cloudtodo.impl.CloudTodoKey
import kpt.core.store.demo.cloudtodo.impl.provideCloudTodoReadStore
import kpt.core.store.demo.cloudtodo.impl.provideCloudTodoStore
import org.mobilenativefoundation.store.store5.Bookkeeper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Locks the MUTABLE-archetype demo repo's gateway-routed write path — the one place a demo repository
 * routes a mutation through the [kpt.core.base.store.mutation.MutationGateway] (`OnlineRequired`) rather
 * than a plain `MutableStore.write`. Exercises the REAL gateway seam via [testMutationGateway] (no fake
 * gateway), so the OnlineRequired contract is asserted end-to-end at the repository boundary.
 */
class CloudTodoRepositoryTest {

    private val api = FakeJsonPlaceholderApi()
    private val dao = FakeCloudTodoDao()
    private val bookkeeper = FakeBookkeeper<CloudTodoKey>()

    private fun repo(online: Boolean) = CloudTodoRepositoryImpl(
        readStore = provideCloudTodoReadStore(api, dao),
        writeStore = provideCloudTodoStore(api, dao, bookkeeper),
        gateway = testMutationGateway(isOnline = online),
    )

    @Test
    fun completeOnline_whenOffline_isBlocked_andWritesNothing() = runTest {
        val r = repo(online = false)
            .completeOnline(CloudTodo(id = 1, title = "buy milk", completed = false))
        val blocked = assertIs<MutationResult.Blocked>(r)
        assertEquals(BlockReason.OFFLINE, blocked.reason)
        assertFalse(api.updateCalled, "OnlineRequired offline must not touch the network")
        assertNull(dao.getById(1), "OnlineRequired offline must not write locally (no optimistic state)")
    }

    @Test
    fun completeOnline_whenOnline_appliesAndMarksCompleted() = runTest {
        val r = repo(online = true)
            .completeOnline(CloudTodo(id = 2, title = "pay rent", completed = false))
        val applied = assertIs<MutationResult.Applied<CloudTodo>>(r)
        assertTrue(applied.value.completed, "the applied value is the completed todo")
        assertTrue(applied.synced, "an online OnlineRequired write is synced")
        assertTrue(api.updateCalled, "OnlineRequired online must reach the network")
        assertEquals(true, dao.getById(2)?.completed, "the write-through persists the completed row")
    }
}

private class FakeCloudTodoDao : CloudTodoDao {
    private val rows = mutableMapOf<Int, CloudTodoEntity>()
    override fun observeById(id: Int): Flow<CloudTodoEntity?> = flow { emit(rows[id]) }
    override suspend fun getById(id: Int): CloudTodoEntity? = rows[id]
    override suspend fun upsert(entity: CloudTodoEntity) {
        rows[entity.id] = entity
    }
    override suspend fun deleteById(id: Int) {
        rows.remove(id)
    }
    override suspend fun deleteAll() {
        rows.clear()
    }
}

private class FakeJsonPlaceholderApi : JsonPlaceholderApi {
    var updateCalled = false
    override suspend fun getTodo(id: Int): CloudTodoDto =
        CloudTodoDto(id = id, title = "todo-$id", completed = false)
    override suspend fun updateTodo(id: Int, todo: CloudTodoDto): CloudTodoDto {
        updateCalled = true
        return todo // the server echoes the write back
    }
}

private class FakeBookkeeper<K : Any> : Bookkeeper<K> {
    private val failed = mutableMapOf<K, Long>()
    override suspend fun getLastFailedSync(key: K): Long? = failed[key]
    override suspend fun setLastFailedSync(key: K, timestamp: Long): Boolean {
        failed[key] = timestamp
        return true
    }
    override suspend fun clear(key: K): Boolean = failed.remove(key) != null
    override suspend fun clearAll(): Boolean {
        failed.clear()
        return true
    }
}
