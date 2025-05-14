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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Applies the CMP (Custom Mifos Platform) theme to the composable hierarchy.
 *
 * This function configures the Material3 [MaterialTheme] using the provided [ColorPalette].
 * Depending on the [isDarkMode] flag, it will use either a light or dark color scheme, and optionally apply
 * dynamic colors if supported and enabled via the [dynamicColor] flag. The actual color scheme used can
 * be modified through the [selectColorScheme] expect function to allow platform-specific implementations.
 *
 * @param colorPalette The custom color palette used to configure the color scheme.
 * @param isDarkMode Whether the theme should use a dark color scheme. Defaults to `false`.
 * @param dynamicColor Whether dynamic colors should be enabled (e.g., on Android 12+). Defaults to `true`.
 * @param content The composable content to which the theme will be applied.
 */
@Composable
fun CMPTheme(
    colorPalette: ColorPalette,
    isDarkMode: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val selectedColorScheme = if (isDarkMode) {
        lightColorScheme(
            primary = colorPalette.primary,
            onPrimary = colorPalette.onPrimary,
            primaryContainer = colorPalette.primaryContainer,
            onPrimaryContainer = colorPalette.onPrimaryContainer,
            secondary = colorPalette.secondary,
            onSecondary = colorPalette.onSecondary,
            secondaryContainer = colorPalette.onSecondaryContainer,
            onSecondaryContainer = colorPalette.onSecondaryContainer,
            tertiary = colorPalette.tertiary,
            onTertiary = colorPalette.onTertiary,
            tertiaryContainer = colorPalette.tertiaryContainer,
            onTertiaryContainer = colorPalette.onTertiaryContainer,
            error = colorPalette.error,
            onError = colorPalette.onError,
            errorContainer = colorPalette.errorContainer,
            onErrorContainer = colorPalette.onErrorContainer,
            background = colorPalette.background,
            onBackground = colorPalette.onBackground,
            surface = colorPalette.surface,
            onSurface = colorPalette.onSurface,
            surfaceVariant = colorPalette.surfaceVariant,
            onSurfaceVariant = colorPalette.surfaceVariant,
            outline = colorPalette.outline,
            outlineVariant = colorPalette.outlineVariant,
            scrim = colorPalette.scrim,
            inverseSurface = colorPalette.inverseOnSurface,
            inverseOnSurface = colorPalette.inverseOnSurface,
            inversePrimary = colorPalette.inversePrimary,
            surfaceDim = colorPalette.surfaceDim,
            surfaceBright = colorPalette.surfaceBright,
            surfaceContainerLowest = colorPalette.surfaceContainerLowest,
            surfaceContainerLow = colorPalette.surfaceContainerLow,
            surfaceContainer = colorPalette.surfaceContainer,
            surfaceContainerHigh = colorPalette.surfaceContainerHigh,
            surfaceContainerHighest = colorPalette.surfaceContainerHighest,
        )
    } else {
        darkColorScheme(
            primary = colorPalette.primary,
            onPrimary = colorPalette.onPrimary,
            primaryContainer = colorPalette.primaryContainer,
            onPrimaryContainer = colorPalette.onPrimaryContainer,
            secondary = colorPalette.secondary,
            onSecondary = colorPalette.onSecondary,
            secondaryContainer = colorPalette.onSecondaryContainer,
            onSecondaryContainer = colorPalette.onSecondaryContainer,
            tertiary = colorPalette.tertiary,
            onTertiary = colorPalette.onTertiary,
            tertiaryContainer = colorPalette.tertiaryContainer,
            onTertiaryContainer = colorPalette.onTertiaryContainer,
            error = colorPalette.error,
            onError = colorPalette.onError,
            errorContainer = colorPalette.errorContainer,
            onErrorContainer = colorPalette.onErrorContainer,
            background = colorPalette.background,
            onBackground = colorPalette.onBackground,
            surface = colorPalette.surface,
            onSurface = colorPalette.onSurface,
            surfaceVariant = colorPalette.surfaceVariant,
            onSurfaceVariant = colorPalette.surfaceVariant,
            outline = colorPalette.outline,
            outlineVariant = colorPalette.outlineVariant,
            scrim = colorPalette.scrim,
            inverseSurface = colorPalette.inverseOnSurface,
            inverseOnSurface = colorPalette.inverseOnSurface,
            inversePrimary = colorPalette.inversePrimary,
            surfaceDim = colorPalette.surfaceDim,
            surfaceBright = colorPalette.surfaceBright,
            surfaceContainerLowest = colorPalette.surfaceContainerLowest,
            surfaceContainerLow = colorPalette.surfaceContainerLow,
            surfaceContainer = colorPalette.surfaceContainer,
            surfaceContainerHigh = colorPalette.surfaceContainerHigh,
            surfaceContainerHighest = colorPalette.surfaceContainerHighest,
        )
    }

    val colorScheme = selectColorScheme(
        isDarkMode = isDarkMode,
        dynamicColor = dynamicColor,
        colorScheme = selectedColorScheme,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = appTypography(),
        content = content,
    )
}

/**
 * Platform-specific function to modify or override the selected [ColorScheme].
 *
 * This allows platforms such as Android, iOS, Web or desktop to provide custom logic for
 * selecting a final [ColorScheme] based on the current dark mode setting or whether
 * dynamic color support is enabled.
 *
 * @param isDarkMode Whether the dark theme is enabled.
 * @param dynamicColor Whether dynamic colors should be used (if supported by the platform).
 * @param colorScheme The base color scheme derived from the given [ColorPalette].
 * @return A [ColorScheme] to be used with [MaterialTheme].
 */
@Composable
expect fun selectColorScheme(
    isDarkMode: Boolean,
    dynamicColor: Boolean,
    colorScheme: ColorScheme,
): ColorScheme
