/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home.tasks

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import org.mifos.feature.home.TaskMinderViewModel
import org.mifos.feature.home.extensions.formatDay
import org.mifos.feature.home.model.TaskEntity
import org.mifos.feature.home.service.StorageService

/**
 * The ViewModel for managing the tasks UI state and interacting with the StorageService.
 * It provides logic for selecting dates, updating the tasks, and managing the UI state
 * related to tasks for a specific day, month, and year.
 *
 * @property storageService The service responsible for fetching and updating tasks in the storage.
 */
class TasksViewModel(
    private val storageService: StorageService,
) : TaskMinderViewModel() {

    /**
     * Flow representing the UI state for tasks. It includes the selected
     * date and the list of weekdays and days in the month.
     */
    private val shortWeekdayNames = mapOf(
        DayOfWeek.MONDAY to "Mon",
        DayOfWeek.TUESDAY to "Tue",
        DayOfWeek.WEDNESDAY to "Wed",
        DayOfWeek.THURSDAY to "Thu",
        DayOfWeek.FRIDAY to "Fri",
        DayOfWeek.SATURDAY to "Sat",
        DayOfWeek.SUNDAY to "Sun",
    )

    private var _tasksUiState = MutableStateFlow(TasksUiState())
    val tasksUiState: StateFlow<TasksUiState> = _tasksUiState
        .map { tasksUiState ->
            tasksUiState.copy(
                selectedDayInMonth = tasksUiState.selectedDayInMonth.formatDay(),
                weekdaysAndDaysInMonth = tasksUiState.weekdaysAndDaysInMonth.map { (weekday, day) ->
                    weekday to day.formatDay()
                },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TasksUiState(),
        )

    /**
     * The list of tasks for the selected date. It uses `flatMapLatest` to fetch tasks based
     * on the current selected month, day, and year from the UI state.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<TaskEntity>> =
        tasksUiState.map {
            val selectedMonth = it.selectedMonthIndex.plus(1).toString().formatDay()
            "$selectedMonth/${it.selectedDayInMonth}/${it.selectedYear}"
        }.flatMapLatest { dateString ->
            storageService.getSelectedDayTasks(dateString)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    private val selectedMonthIndex get() = tasksUiState.value.selectedMonthIndex
    private val selectedYear get() = tasksUiState.value.selectedYear

    init {
        updateDaysInMonth()
    }

    /**
     * Updates the selected year and refreshes the days in the month.
     *
     * @param year The new year to be selected.
     */
    fun updateSelectedYear(year: Int) {
        _tasksUiState.value = _tasksUiState.value.copy(selectedYear = year)
        updateDaysInMonth()
    }

    /**
     * Selects the next month and updates the days in the selected month.
     */
    fun selectNextMonth() {
        if (selectedMonthIndex < 11) {
            _tasksUiState.value =
                _tasksUiState.value.copy(selectedMonthIndex = selectedMonthIndex + 1)
            updateDaysInMonth()
        }
    }

    /**
     * Selects the previous month and updates the days in the selected month.
     */
    fun selectPreviousMonth() {
        if (selectedMonthIndex > 0) {
            _tasksUiState.value =
                _tasksUiState.value.copy(selectedMonthIndex = selectedMonthIndex - 1)
            updateDaysInMonth()
        }
    }

    /**
     * Updates the selected day in the month.
     *
     * @param day The new day to be selected.
     */
    fun updateSelectDayInMonth(day: String) {
        _tasksUiState.value = _tasksUiState.value.copy(selectedDayInMonth = day)
    }

    /**
     * Gets the number of days in a given month and year.
     *
     * @param month The month (0-based index) for which to get the number of days.
     * @param year The year for which to get the number of days.
     * @return The number of days in the given month of the given year.
     */
    private fun getDaysInMonth(month: Int, year: Int): Int {
        val currentMonthDate = LocalDate(year, month, 1)
        val nextMonthDate = currentMonthDate.plus(1, DateTimeUnit.MONTH)
        return currentMonthDate.daysUntil(nextMonthDate)
    }

    /**
     * Updates the list of weekdays and days in the selected month.
     * It calculates the weekdays for all the days in the selected month and updates the UI state.
     */
    private fun updateDaysInMonth() {
        val daysInCurrentMonth = getDaysInMonth(year = selectedYear, month = selectedMonthIndex) // month is 1-based

        val weekdaysAndDaysInSelectedMonth = mutableListOf<Pair<String, String>>()

        for (day in 1..daysInCurrentMonth) {
            val date = LocalDate(selectedYear, selectedMonthIndex, day)
            val weekday = shortWeekdayNames[date.dayOfWeek]

            weekdaysAndDaysInSelectedMonth.add(weekday!! to day.toString())
        }

        _tasksUiState.value =
            _tasksUiState.value.copy(weekdaysAndDaysInMonth = weekdaysAndDaysInSelectedMonth)
    }

    /**
     * Flags a task as completed or not completed by toggling its `completed` state.
     *
     * @param task The task to flag as completed or not completed.
     */
    fun flagTask(task: TaskEntity) {
        launchCatching {
            storageService.updateTask(task.copy(completed = !task.completed))
        }
    }

    /**
     * Deletes a task by its ID.
     *
     * @param taskId The ID of the task to be deleted.
     */
    fun deleteTask(taskId: Int) {
        launchCatching {
            storageService.deleteTask(taskId)
        }
    }
}
