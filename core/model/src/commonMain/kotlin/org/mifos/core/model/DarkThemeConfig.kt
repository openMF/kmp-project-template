/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.model

enum class DarkThemeConfig(val configName: String) {
    FOLLOW_SYSTEM("Follow System"),
    LIGHT("Light"),
    DARK("Dark"),
    ;

    companion object {
        fun fromString(value: String): DarkThemeConfig {
            return entries.find { it.configName.equals(value, ignoreCase = true) } ?: FOLLOW_SYSTEM
        }
    }
}
