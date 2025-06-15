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

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * A data class representing the state of the task calendar screen.
 *
 * This class holds information about the currently selected year, month, day,
 * and the list of weekdays and corresponding days in the month. It is designed to
 * facilitate displaying a calendar-like UI for task management.
 *
 * @param selectedYear The currently selected year. Defaults to the current year.
 * @param selectedMonthIndex The index of the currently selected month (1 for January, 2 for February, etc.).
 * Defaults to the current month.
 * @param selectedDayInMonth The currently selected day in the month, represented as a string.
 * Defaults to the current day of the month.
 * @param weekdaysAndDaysInMonth A list of pairs, where each pair consists of a weekday name (e.g., "Monday")
 * and the corresponding day of the month (e.g., "1"). Defaults to an empty list.
 */
data class TasksUiState(
    val selectedYear: Int = defaultYear,
    val selectedMonthIndex: Int = defaultMonthIndex,
    val selectedDayInMonth: String = defaultDayInMonth.toString(),
    val weekdaysAndDaysInMonth: List<Pair<String, String>> = emptyList(),
) {

    companion object {
        // Default values based on the current date
        private val currentMoment = Clock.System.now()
        private val datetimeInSystemZone: LocalDateTime =
            currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())

        // Default year, current year
        val defaultYear: Int = datetimeInSystemZone.year

        // Default month index, current month (1-indexed)
        val defaultMonthIndex: Int = datetimeInSystemZone.monthNumber - 1

        // Default day in month, current day (as an Int for easier comparison)
        val defaultDayInMonth: Int = datetimeInSystemZone.dayOfMonth
    }

    // List of month names for lookup based on the month index
    private val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    /**
     * Returns the name of the selected month corresponding to [selectedMonthIndex].
     * For example, if [selectedMonthIndex] is 0, it returns "January".
     */
    val selectedMonth: String
        get() = months[selectedMonthIndex]
}
