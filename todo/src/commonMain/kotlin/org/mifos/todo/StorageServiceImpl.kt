/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.todo

import kotlinx.coroutines.flow.Flow
import org.mifos.core.database.dao.TaskDao
import org.mifos.core.database.entity.TaskEntity

/**
 * Implementation of [StorageService] using Room database for storing and managing tasks.
 *
 * @param taskDao The [TaskDao] instance for performing database operations related to tasks.
 */
class StorageServiceImpl(
    private val taskDao: TaskDao,
) : StorageService {

    /**
     * Retrieves a list of tasks for a specific date for the currently authenticated user.
     *
     * @param selectedDate The date for which to retrieve tasks, formatted as a string ["MM/dd/yyyy"].
     * @return A [Flow] emitting a list of tasks matching the specified date.
     */
    override fun getSelectedDayTasks(selectedDate: String): Flow<List<TaskEntity>> {
        return taskDao.getSelectedDayTasks(selectedDate)
    }

    /**
     * Retrieves a task by its unique identifier.
     *
     * @param taskId The unique identifier of the task.
     * @return The [TaskEntity] if found, or `null` if the task does not exist.
     */
    override suspend fun getTask(taskId: Int): TaskEntity? =
        taskDao.getTask(taskId)

    /**
     * Adds a new task to the database for the currently authenticated user.
     *
     * @param task The task to be added.
     */
    override suspend fun addTask(task: TaskEntity) =
        taskDao.addTask(task)

    /**
     * Updates an existing task in the database.
     *
     * @param task The task with updated details. The task must have a valid `id` field.
     */
    override suspend fun updateTask(task: TaskEntity) =
        taskDao.updateTask(task)

    /**
     * Deletes a task by its unique identifier from the database.
     *
     * @param taskId The unique identifier of the task to be deleted.
     */
    override suspend fun deleteTask(taskId: Int) {
        taskDao.deleteTask(taskId)
    }
}
