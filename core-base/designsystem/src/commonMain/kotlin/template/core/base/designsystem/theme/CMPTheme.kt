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

@Composable
expect fun selectColorScheme(
    isDarkMode: Boolean,
    dynamicColor: Boolean,
    colorScheme: ColorScheme,
): ColorScheme
