package org.mifos.core.designsystem.theme

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Spacing tokens
data class MifosSpacing(
    val none: Dp = 0.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 40.dp,
)

// Corner radius tokens
data class MifosRadius(
    val none: Dp = 0.dp,
    val sm: Dp = 4.dp,
    val md: Dp = 8.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 28.dp,
)

// Elevation tokens
data class MifosElevation(
    val none: Dp = 0.dp,
    val sm: Dp = 1.dp,
    val md: Dp = 3.dp,
    val lg: Dp = 6.dp,
    val xl: Dp = 12.dp,
)

val LocalSpacing: ProvidableCompositionLocal<MifosSpacing> = staticCompositionLocalOf { MifosSpacing() }

val LocalElevation: ProvidableCompositionLocal<MifosElevation> = staticCompositionLocalOf {
    MifosElevation()
}

val LocalRadius: ProvidableCompositionLocal<MifosRadius> = staticCompositionLocalOf { MifosRadius() }
