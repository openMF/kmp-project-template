/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.theme

/**
 * Represents the user's preference for dark theme behavior in the CMP design system.
 *
 * Each variant defines how the app's theme should behave in relation to system settings.
 *
 * @property configName A user-friendly name used for display or storage (e.g., in preferences or UI).
 */
enum class DarkThemeConfig(val configName: String) {

    /**
     * Follows the system's dark/light theme setting dynamically.
     */
    FOLLOW_SYSTEM("Follow System"),

    /**
     * Forces the app into light mode regardless of system settings.
     */
    LIGHT("Light"),

    /**
     * Forces the app into dark mode regardless of system settings.
     */
    DARK("Dark"),
    ;

    companion object {
        /**
         * Parses a [String] value into a [DarkThemeConfig], ignoring case.
         *
         * @param value The input string (typically from preferences or user input).
         * @return The matching [DarkThemeConfig], or [FOLLOW_SYSTEM] if no match is found.
         */
        fun fromValue(value: String): DarkThemeConfig {
            return entries.find { it.configName.equals(value, ignoreCase = true) } ?: FOLLOW_SYSTEM
        }
    }
}
