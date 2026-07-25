/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.demo.cloudtodo

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Room mirror of a [kpt.core.model.demo.cloudtodo.CloudTodo] (the Store5 MutableStore SoT). */
@Entity(tableName = "cloud_todos")
data class CloudTodoEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val completed: Boolean,
)
