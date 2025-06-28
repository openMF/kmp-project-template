/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home.model

/**
 * Represents a task with associated details.
 *
 * @property id Unique identifier for the task.
 * @property title Title or name of the task.
 * @property priority Priority level of the task (e.g., HIGH, MEDIUM, LOW).
 * @property dueDate The date by which the task should be completed.
 * @property dueTime The time by which the task should be completed.
 * @property description A detailed description of the task.
 * @property completed Indicates whether the task has been completed.
 * @property alert Indicates whether the user should receive an alert for this task.
 * @property userId Identifier of the user associated with the task.
 */
data class TaskEntity(
    val id: Int = 0,
    val title: String = "",
    val priority: String = "",
    val dueDate: String = "",
    val dueTime: String = "",
    val description: String = "",
    val completed: Boolean = false,
    val alert: Boolean = false,
    val userId: String = "",
)
