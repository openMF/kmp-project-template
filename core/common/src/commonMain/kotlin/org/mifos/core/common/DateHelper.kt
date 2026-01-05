/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.common

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

object DateHelper {

    @OptIn(ExperimentalTime::class)
    fun isDarkModeBasedOnTime(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
    ): Boolean {
        val now = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .time

        val currentMinutes = now.hour * 60 + now.minute
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute

        return if (startMinutes < endMinutes) {
            // Same-day range (e.g., 06:00 → 18:00)
            currentMinutes in startMinutes until endMinutes
        } else {
            // Cross-midnight range (e.g., 18:00 → 06:00)
            currentMinutes !in endMinutes..<startMinutes
        }
    }

    fun formatTimeRange(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
    ): String {
        return "${format(startHour, startMinute)} - ${format(endHour, endMinute)}"
    }

    fun format(hour: Int, minute: Int): String {
        val period = if (hour < 12) "AM" else "PM"
        val hour12 = when (hour % 12) {
            0 -> 12
            else -> hour % 12
        }
        return "$hour12:${minute.toString().padStart(2, '0')} $period"
    }
}
