/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home

import kotlinx.coroutines.flow.Flow

/**
 * A service interface for managing tasks stored in a remote database.
 */
interface StorageService {

    /**
     * Retrieves a list of tasks for the specified date.
     *
     * @param selectedDate The date for which to retrieve tasks, formatted as a string.
     * @return A [Flow] emitting a list of tasks matching the specified date.
     */
    fun getSelectedDayTasks(selectedDate: String): Flow<List<TaskEntity>>

    /**
     * Retrieves a task by its unique identifier.
     *
     * @param taskId The unique identifier of the task.
     * @return The [TaskEntity] if found, or `null` if not found.
     */
    suspend fun getTask(taskId: Int): TaskEntity?

    /**
     * Adds a new task to the database.
     *
     * @param task The task to be added.
     */
    suspend fun addTask(task: TaskEntity)

    /**
     * Updates an existing task in the database.
     *
     * @param task The task with updated details.
     */
    suspend fun updateTask(task: TaskEntity)

    /**
     * Deletes a task by its unique identifier.
     *
     * @param taskId The unique identifier of the task to be deleted.
     */
    suspend fun deleteTask(taskId: Int)
}
