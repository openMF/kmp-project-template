/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.cloudtodo

import kpt.core.model.cloudtodo.CloudTodo

/**
 * Row → domain. Called from the Store's `SourceOfTruth.reader`, which is what keeps the entity type
 * out of every layer above `core/store`.
 */
fun CloudTodoEntity.toDomain(): CloudTodo = CloudTodo(id = id, title = title, completed = completed)

/**
 * Domain → row. Called from the `SourceOfTruth.writer`.
 */
fun CloudTodo.toEntity(): CloudTodoEntity = CloudTodoEntity(id = id, title = title, completed = completed)
