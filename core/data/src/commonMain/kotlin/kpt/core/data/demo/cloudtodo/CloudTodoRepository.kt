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

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.demo.cloudtodo.CloudTodo

/**
 * Read + offline-write surface over the cloud-todo [org.mobilenativefoundation.store.store5.MutableStore].
 * The read side is a normal `asScreenStream`; [toggleCompleted] runs the Store5 write path
 * (Updater PUT + Bookkeeper offline-queue) — the toolkit's only MUTABLE-archetype consumer.
 */
interface CloudTodoRepository {
    fun todoStream(id: Int, scope: CoroutineScope): ScreenDataStream<CloudTodo>

    /** Flips `completed` and writes back through the MutableStore (Updater → server; Bookkeeper on failure). */
    suspend fun toggleCompleted(todo: CloudTodo)

    /**
     * Mark [todo] complete with the `OnlineRequired` policy — the network PUT is awaited first and the
     * server record ingested; when offline the mutation is [kpt.core.base.store.mutation.MutationResult.Blocked]
     * and nothing is written locally (unlike the optimistic [toggleCompleted]). The demo reference for
     * network-first mutations that must not show an unconfirmed local state (payments, approvals).
     */
    suspend fun completeOnline(todo: CloudTodo): kpt.core.base.store.mutation.MutationResult<CloudTodo>
}
