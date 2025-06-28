/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.mifos.feature.home.model.TaskEntity
import template.core.base.datastore.cache.CacheManager
import template.core.base.datastore.cache.LruCacheManager

private const val TASKS_KEY = "ALL_TASKS"
class StorageServiceImpl(
    private val cache: CacheManager<Int, TaskEntity> = LruCacheManager(),
    private val taskListCache: CacheManager<String, List<TaskEntity>> = LruCacheManager(),
) : StorageService {

    private val tasksFlow = MutableStateFlow<List<TaskEntity>>(emptyList())

    init {
        // Load initial data into flow
        val initial = getAllTasks()
        tasksFlow.value = initial
        initial.forEach { cache.put(it.id, it) }
    }

    private fun getAllTasks(): MutableList<TaskEntity> {
        return (taskListCache.get(TASKS_KEY) ?: emptyList()).toMutableList()
    }

    private fun updateAllTasks(tasks: List<TaskEntity>) {
        taskListCache.clear()
        taskListCache.put(TASKS_KEY, tasks)
        cache.clear()
        tasks.forEach { cache.put(it.id, it) }
        tasksFlow.value = tasks // notify observers
    }

    override fun getSelectedDayTasks(selectedDate: String): Flow<List<TaskEntity>> {
        return tasksFlow.map { tasks ->
            tasks.filter { it.dueDate == selectedDate }
        }
    }

    override suspend fun getTask(taskId: Int): TaskEntity? {
        return cache.get(taskId) ?: getAllTasks().find { it.id == taskId }
    }

    override suspend fun addTask(task: TaskEntity) {
        val tasks = getAllTasks()

        // If id == 0, treat as new task and generate unique id
        val actualTask = if (task.id == 0) {
            val nextId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
            task.copy(id = nextId)
        } else {
            task
        }

        // Replace if exists
        val updatedTasks = tasks.map {
            if (it.id == actualTask.id) actualTask else it
        }.let {
            if (it.any { task -> task.id == actualTask.id }) {
                it
            } else {
                it + actualTask
            }
        }

        updateAllTasks(updatedTasks)
    }

    override suspend fun updateTask(task: TaskEntity) {
        // Simply reuse addTask, which already handles replace-by-id logic
        addTask(task)
    }

    override suspend fun deleteTask(taskId: Int) {
        val updatedTasks = getAllTasks().filterNot { it.id == taskId }
        updateAllTasks(updatedTasks)
    }
}
