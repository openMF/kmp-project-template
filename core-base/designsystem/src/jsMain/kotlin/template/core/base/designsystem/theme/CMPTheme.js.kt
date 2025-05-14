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

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Default or platform-specific implementation of [selectColorScheme] that returns the provided [colorScheme] as-is.
 *
 * This implementation does not apply dynamic theming or platform-specific logic.
 * It is typically used for platforms where dynamic color support is not available or not needed.
 *
 * @param isDarkMode Whether the dark theme is currently enabled.
 * @param dynamicColor Whether dynamic theming is enabled (ignored in this implementation).
 * @param colorScheme The [ColorScheme] to return unchanged.
 * @return The input [colorScheme] without modification.
 */
@Composable
actual fun selectColorScheme(
    isDarkMode: Boolean,
    dynamicColor: Boolean,
    colorScheme: ColorScheme,
): ColorScheme {
    return colorScheme
}
