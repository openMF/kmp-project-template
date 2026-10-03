/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.jsonplaceholder.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kpt.core.model.cloudtodo.CloudTodo

/** Wire shape for jsonplaceholder `/todos`. */
@Serializable
data class CloudTodoDto(
    /** Todo id, and the Store key. jsonplaceholder serves ids 1–200; anything else 404s. */
    @SerialName("id") val id: Int,
    /** Todo text. */
    @SerialName("title") val title: String,
    /** Whether it is done — the field the MUTABLE archetype writes back. */
    @SerialName("completed") val completed: Boolean,
    /** Owning user. Defaults to 1 because jsonplaceholder has no auth and the demo needs a stable owner. */
    @SerialName("userId") val userId: Int = 1,
) {
    /** Maps to the domain model, dropping [userId]. */
    fun toDomain(): CloudTodo = CloudTodo(id = id, title = title, completed = completed)

    /** Mapping the other way. */
    companion object {
        /** Builds the wire shape for a `PUT`, restoring the default [userId]. */
        fun fromDomain(todo: CloudTodo): CloudTodoDto =
            CloudTodoDto(id = todo.id, title = todo.title, completed = todo.completed)
    }
}
