package org.mifos.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Provider composable for injecting design tokens into the composition tree.
 */
@Composable
fun MifosDesignSystemProvider(
    spacing: MifosSpacing = MifosSpacing(),
    radius: MifosRadius = MifosRadius(),
    elevation: MifosElevation = MifosElevation(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSpacing provides spacing,
        LocalRadius provides radius,
        LocalElevation provides elevation,
        content = content
    )
}