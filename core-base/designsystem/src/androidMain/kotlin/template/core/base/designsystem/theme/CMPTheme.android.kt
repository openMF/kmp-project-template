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

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Android-specific implementation of [selectColorScheme] that applies dynamic theming
 * based on the device's capabilities and user preferences.
 *
 * If [dynamicColor] is enabled and the device is running Android 12 (API level 31) or higher,
 * this function will return a dynamic [ColorScheme] generated from the user's wallpaper colors.
 * Otherwise, it falls back to the provided [colorScheme].
 *
 * @param isDarkMode Whether the dark theme is currently enabled.
 * @param dynamicColor Whether dynamic theming is enabled.
 * @param colorScheme The fallback [ColorScheme] to use when dynamic theming is unavailable.
 * @return A [ColorScheme] appropriate for the current configuration.
 */
@Composable
actual fun selectColorScheme(
    isDarkMode: Boolean,
    dynamicColor: Boolean,
    colorScheme: ColorScheme,
): ColorScheme {
    return when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> colorScheme
    }
}
