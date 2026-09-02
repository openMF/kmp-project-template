/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.cloudtodo.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.demo.cloudtodo.CloudTodoRepository
import kpt.core.model.demo.cloudtodo.CloudTodo
import kpt.core.store.AppCacheKeys
import kpt.core.store.demo.cloudtodo.impl.CloudTodoKey
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreWriteRequest

/**
 * @param readStore `createStore` read half — drives `asScreenStream`.
 * @param writeStore `createMutableStore` write half — drives the Updater (PUT) + Bookkeeper.
 *   Both are backed by the same `cloud_todos` Room table, so a write is reflected in the read stream.
 */
class CloudTodoRepositoryImpl(
    private val readStore: Store<CloudTodoKey, CloudTodo>,
    private val writeStore: MutableStore<CloudTodoKey, CloudTodo>,
    private val gateway: MutationGateway,
) : CloudTodoRepository {

    override fun todoStream(id: Int, scope: CoroutineScope): ScreenDataStream<CloudTodo> =
        readStore.asScreenStream(
            key = CloudTodoKey(id),
            cacheKey = AppCacheKeys.cloudTodo(id),
            scope = scope,
        )

    override suspend fun toggleCompleted(todo: CloudTodo) {
        val updated = todo.copy(completed = !todo.completed)
        // MutableStore.write drives the Updater (PUT); on failure the Bookkeeper records it for
        // retry on reconnect. The optimistic local value is persisted to the Room SoT immediately,
        // so the read store's asScreenStream re-emits it (same `cloud_todos` table).
        writeStore.write(
            StoreWriteRequest.of<CloudTodoKey, CloudTodo, Any>(
                key = CloudTodoKey(updated.id),
                value = updated,
            ),
        )
    }

    override suspend fun completeOnline(todo: CloudTodo): MutationResult<CloudTodo> =
        // OnlineRequired: the gateway awaits the network PUT (via writeStore's Updater) and ingests the
        // server record; offline it returns Blocked WITHOUT writing locally — no unconfirmed optimistic
        // state, unlike toggleCompleted. The single write door owns the connectivity + ingest decision.
        gateway.upsert(
            store = writeStore,
            key = CloudTodoKey(todo.id),
            value = todo.copy(completed = true),
            policy = MutationPolicy.OnlineRequired,
        )
}
