/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.cloudtodo.impl

import kotlinx.coroutines.flow.map
import kpt.core.base.store.infra.ConflictStrategy
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.demo.cloudtodo.CloudTodoDao
import kpt.core.database.demo.cloudtodo.CloudTodoEntity
import kpt.core.database.demo.cloudtodo.toDomain
import kpt.core.database.demo.cloudtodo.toEntity
import kpt.core.model.demo.cloudtodo.CloudTodo
import kpt.core.network.demo.cloudtodo.api.JsonPlaceholderApi
import kpt.core.network.demo.cloudtodo.dto.TodoDto
import org.mobilenativefoundation.store.store5.Bookkeeper
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult

/**
 * cloud-todo READ store (`createStore`) — the offline-first read half of the MUTABLE archetype.
 * In Store5 5.1 `MutableStore` is NOT a `Store` subtype (separate read hierarchy), so the
 * framework's `Store.asScreenStream` read pipeline can't consume a `MutableStore` directly.
 * The canonical shape is therefore a read `Store` + a write [MutableStore] over the SAME Room
 * table (`cloud_todos`): the write store's SoT writer persists here, this read store's SoT reader
 * observes it, so a `store.write(...)` is reflected in the read stream via Room. `GET /todos/{id}`.
 */
fun provideCloudTodoReadStore(
    api: JsonPlaceholderApi,
    dao: CloudTodoDao,
): Store<CloudTodoKey, CloudTodo> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: CloudTodoKey -> api.getTodo(key.id).toDomain() },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: CloudTodoKey -> dao.observeById(key.id).map { it?.toDomain() } },
        writer = { _: CloudTodoKey, todo: CloudTodo -> dao.upsert(todo.toEntity()) },
        delete = { key: CloudTodoKey -> dao.deleteById(key.id) },
        deleteAll = { dao.deleteAll() },
    ),
)

/**
 * cloud-todo — the toolkit's **MUTABLE (offline-write) Store5 archetype** showcase, and the only
 * demo that writes back to a server (jsonplaceholder `/todos` accepts PUT). Reads come through the
 * [Fetcher] (`GET /todos/{id}`) + Room [SourceOfTruth]; a `store.write(...)` runs the [Updater]
 * (`PUT /todos/{id}`), and a failed write (offline) is recorded by the injected [Bookkeeper]
 * (`RoomBookkeeper`) for retry on reconnect. This is the ONE place `StoreFactory.createMutableStore`
 * is wired end-to-end — every other demo store is read-side (`createStore` / `createMemoryStore` /
 * `createOfflineStore`).
 *
 * Conflict handling: Store5's `Updater` does NOT auto-consume a [ConflictStrategy] (see
 * `StoreFactory.createMutableStore` KDoc), so the strategy is applied HERE inside the [Updater] —
 * we resolve the server echo against the client value via [CloudTodoConflictResolver] and write the
 * resolved value back. Its default [ConflictStrategy.ClientWins] keeps the user's offline toggle
 * authoritative (offline-first); the resolver also reports whether the two sides genuinely
 * diverged, which is what a `MutationResult.Conflicted` outcome is derived from.
 */
fun provideCloudTodoStore(
    api: JsonPlaceholderApi,
    dao: CloudTodoDao,
    bookkeeper: Bookkeeper<CloudTodoKey>,
): MutableStore<CloudTodoKey, CloudTodo> {
    val conflictResolver = CloudTodoConflictResolver()

    val converter = Converter.Builder<TodoDto, CloudTodoEntity, CloudTodo>()
        // fetch -> SoT: network DTO mapped through domain to the Room entity.
        .fromNetworkToLocal { network: TodoDto -> network.toDomain().toEntity() }
        // write -> SoT: the domain output mapped to the Room entity.
        .fromOutputToLocal { output: CloudTodo -> output.toEntity() }
        .build()

    val updater = Updater.by<CloudTodoKey, CloudTodo, TodoDto>(
        post = { key: CloudTodoKey, value: CloudTodo ->
            val serverEcho = api.updateTodo(key.id, TodoDto.fromDomain(value)).toDomain()
            val resolution = conflictResolver.resolve(server = serverEcho, client = value)
            UpdaterResult.Success.Typed(TodoDto.fromDomain(resolution.value))
        },
    )

    return StoreFactory.createMutableStore(
        fetcher = Fetcher.of { key: CloudTodoKey -> api.getTodo(key.id) },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: CloudTodoKey -> dao.observeById(key.id).map { it?.toDomain() } },
            writer = { _: CloudTodoKey, local: CloudTodoEntity -> dao.upsert(local) },
            delete = { key: CloudTodoKey -> dao.deleteById(key.id) },
            deleteAll = { dao.deleteAll() },
        ),
        converter = converter,
        updater = updater,
        bookkeeper = bookkeeper,
        // conflictStrategy is NOT passed to the factory — it is applied inside `updater` above,
        // the only place both the server echo and the client value exist. The factory used to
        // accept it as an ignored parameter; that decoration is gone.
    )
}
