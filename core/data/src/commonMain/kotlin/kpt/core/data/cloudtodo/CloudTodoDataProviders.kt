/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.cloudtodo

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.DataProvider
import kpt.core.base.data.infra.NetworkMonitor
import kpt.core.base.database.infra.dao.BookkeeperDao
import kpt.core.base.observability.CrashReporter
import kpt.core.base.store.infra.impl.RoomBookkeeper
import kpt.core.database.cloudtodo.CloudTodoDao
import kpt.core.database.cloudtodo.toDomain
import kpt.core.store.cloudtodo.impl.CLOUD_TODO_KEY_PREFIX
import kpt.core.store.cloudtodo.impl.CloudTodoKey
import kpt.core.store.cloudtodo.impl.CloudTodoSyncOrchestrator
import org.mobilenativefoundation.store.store5.Bookkeeper

/**
 * MUTABLE (offline-write) archetype wiring.
 *
 * The bookkeeper records failed writes for retry-on-reconnect and is injected into the core/store
 * MutableStore via Koin. Its `keySerializer` must stay in step with the prefix the store uses — the
 * lambda lives here, in a function body, rather than in an annotation.
 */
@DataProvider
fun provideCloudTodoBookkeeper(dao: BookkeeperDao): Bookkeeper<CloudTodoKey> =
    RoomBookkeeper(dao = dao, keySerializer = { "$CLOUD_TODO_KEY_PREFIX${it.id}" })

/**
 * Eager: drains the cloud-todo write backlog on the offline -> online edge.
 *
 * The sibling features drain a SubmitOutbox of PAYLOADS via OfflineSubmitSyncer; the MutableStore
 * path records KEYS in the bookkeeper instead, so it needs this store-side counterpart. Without it
 * the bookkeeper recorded every failed offline write and nothing ever retried them (S5-SYNC).
 *
 * `crashReporter` is nullable on purpose — it resolves with `getOrNull()`, so a fork that installs
 * no reporter still constructs the graph.
 */
@DataProvider(createdAtStart = true)
fun provideCloudTodoSyncOrchestrator(
    scope: CoroutineScope,
    networkMonitor: NetworkMonitor,
    bookkeeperDao: BookkeeperDao,
    bookkeeper: Bookkeeper<CloudTodoKey>,
    todoDao: CloudTodoDao,
    repository: CloudTodoRepository,
    crashReporter: CrashReporter?,
): CloudTodoSyncOrchestrator = CloudTodoSyncOrchestrator(
    scope = scope,
    networkMonitor = networkMonitor,
    bookkeeperDao = bookkeeperDao,
    bookkeeper = bookkeeper,
    loadLocal = { key -> todoDao.getById(key.id)?.toDomain() },
    writeBlock = { todo -> repository.toggleCompleted(todo) },
    onReplayError = { t -> crashReporter?.recordException(t) },
).also { it.start() }
