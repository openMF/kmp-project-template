/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home.task

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mifos.feature.home.AddEditTaskRoute
import org.mifos.feature.home.TaskMinderViewModel
import org.mifos.feature.home.extensions.toClockPattern
import org.mifos.feature.home.model.TaskEntity
import org.mifos.feature.home.service.StorageService
import org.mifos.feature.home.utils.DateTimeFormatter

/**
 * ViewModel for managing the task editing screen. This ViewModel is responsible for
 * fetching, updating, and saving task details as well as handling UI-related state,
 * such as alert toggling and task save status.
 *
 * @property storageService Service responsible for storing and retrieving tasks.
 */
class EditTaskViewModel(
    savedStateHandle: SavedStateHandle,
    private val storageService: StorageService,
) : TaskMinderViewModel() {

    /**
     * Holds the current task being edited.
     * Exposed as a StateFlow for Compose to observe.
     */
    private var _task: MutableStateFlow<TaskEntity> = MutableStateFlow(TaskEntity())
    val task: StateFlow<TaskEntity> get() = _task.asStateFlow()

    /**
     * Indicates whether the task has been saved.
     * Exposed as a StateFlow for Compose to observe.
     */
    private val _isTaskSaved: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isTaskSaved: StateFlow<Boolean> get() = _isTaskSaved

    init {
        // Initializes the task by fetching it from the storage service if a task ID is provided
        val taskId = savedStateHandle.toRoute<AddEditTaskRoute>().taskId
        if (taskId != null) {
            launchCatching {
                _task.value = storageService.getTask(taskId) ?: TaskEntity()
            }
        }
    }

    /**
     * Updates the task's due date.
     *
     * @param date The new due date in milliseconds.
     */
    fun onDateChange(date: Long) {
        _task.value = _task.value.copy(dueDate = DateTimeFormatter.convertMillisToDate(date))
    }

    /**
     * Updates the task's due time.
     *
     * @param hour The new hour for the task's due time.
     * @param min The new minute for the task's due time.
     */
    fun onTimeChange(hour: Int, min: Int) {
        val newDueTime = "${hour.toClockPattern()}:${min.toClockPattern()}"
        _task.value = _task.value.copy(dueTime = newDueTime)
    }

    /**
     * Updates the task's title.
     *
     * @param newValue The new title for the task.
     */
    fun onTitleChange(newValue: String) {
        _task.value = _task.value.copy(title = newValue)
    }

    /**
     * Updates the task's description.
     *
     * @param newValue The new description for the task.
     */
    fun onDescriptionChange(newValue: String) {
        _task.value = _task.value.copy(description = newValue)
    }

    /**
     * Updates the task's priority.
     *
     * @param newValue The new priority for the task.
     */
    fun onPriorityChange(newValue: String) {
        _task.value = _task.value.copy(priority = newValue)
    }

    /**
     * Saves the current task.
     * If the task is new (id is blank), it adds the task, otherwise it updates the task.
     */
    fun onSaveTask() {
        launchCatching {
            val editedTask = _task.value
            if (editedTask.id == 0) {
                storageService.addTask(editedTask)
            } else {
                storageService.updateTask(editedTask)
            }
            _isTaskSaved.value = true
        }
    }

    /**
     * Resets the task saved status to false after the task is saved.
     */
    fun resetTaskSaved() {
        _isTaskSaved.value = false
    }
}
