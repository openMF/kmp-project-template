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

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Utility object for formatting and converting date and time values.
 *
 * Provides methods for handling common date and time formatting operations.
 */
object DateTimeFormatter {

    /**
     * Converts a timestamp in milliseconds to a formatted date string.
     *
     * @param millis The timestamp in milliseconds to be converted.
     * @return A string representing the formatted date in the pattern "MM/dd/yyyy".
     *
     * Example usage:
     * ```
     * val date = DateTimeFormatter.convertMillisToDate(System.currentTimeMillis())
     * println(date) // Output: "12/01/2024" (depending on the current date)
     * ```
     *
     * Note:
     * - The function uses the default locale of the device to ensure region-appropriate formatting.
     * - Be cautious with locale-specific differences in date representations.
     */
    fun convertMillisToDate(millis: Long): String {
        val instant = Instant.fromEpochMilliseconds(millis)
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return "${localDate.monthNumber.toString().padStart(2, '0')}/" +
            "${localDate.dayOfMonth.toString().padStart(2, '0')}/" +
            "${localDate.year}"
    }
}
