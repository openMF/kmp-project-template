/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.demo.cloudtodo

/**
 * A cloud-synced todo — the toolkit's MUTABLE (offline-write) Store5 archetype showcase.
 * Reads come from jsonplaceholder `GET /todos/{id}`; toggling `completed` writes back via
 * the Store5 [org.mobilenativefoundation.store.store5.Updater] (`PUT /todos/{id}`), and a
 * failed write (offline) is recorded by the RoomBookkeeper for retry on reconnect. Pure Kotlin.
 */
data class CloudTodo(
    val id: Int,
    val title: String,
    val completed: Boolean,
)
