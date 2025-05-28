/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.core

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties

sealed class AlertDialogVariant : ComponentVariant {
    data object Standard : AlertDialogVariant() {
        override val name = "standard"
    }

    data object Basic : AlertDialogVariant() {
        override val name = "basic"
    }

    data object Confirmation : AlertDialogVariant() {
        override val name = "confirmation"
    }

    data object Error : AlertDialogVariant() {
        override val name = "error"
    }

    data object Warning : AlertDialogVariant() {
        override val name = "warning"
    }

    data object Success : AlertDialogVariant() {
        override val name = "success"
    }

    data object Info : AlertDialogVariant() {
        override val name = "info"
    }
}

@Immutable
data class DialogColors(
    val containerColor: Color? = null,
    val iconContentColor: Color? = null,
    val titleContentColor: Color? = null,
    val textContentColor: Color? = null,
) : ComponentColors

@Immutable
data class DialogElevation(
    val tonalElevation: Dp? = null,
) : ComponentElevation

@Immutable
data class DialogTheme(
    val colors: DialogColors,
    val shape: Shape? = null,
    val elevation: DialogElevation? = null,
) : ComponentTheme

@Immutable
data class DialogContent(
    val title: String? = null,
    val text: String? = null,
    val icon: ImageVector? = null,
    val customContent: (@Composable () -> Unit)? = null,
)

@Immutable
data class DialogButton(
    override val onClick: () -> Unit,
    override val enabled: Boolean = true,
    override val interactionSource: MutableInteractionSource? = null,
    val text: String,
) : Clickable

interface AlertDialogColorScheme {
    fun getColors(variant: AlertDialogVariant): DialogColors
    fun getIcon(variant: AlertDialogVariant): ImageVector?
}

// Default implementation with customizable colors
class DefaultAlertDialogColorScheme(
    private val errorColors: DialogColors = DialogColors(
        containerColor = Color(0xFFFFF5F5),
        iconContentColor = Color(0xFFF44336),
    ),
    private val warningColors: DialogColors = DialogColors(
        containerColor = Color(0xFFFFF8E1),
        iconContentColor = Color(0xFFFF9800),
    ),
    private val successColors: DialogColors = DialogColors(
        containerColor = Color(0xFFF1F8E9),
        iconContentColor = Color(0xFF4CAF50),
    ),
    private val infoColors: DialogColors = DialogColors(
        containerColor = Color(0xFFE3F2FD),
        iconContentColor = Color(0xFF2196F3),
    ),
    private val confirmationColors: DialogColors = DialogColors(
        iconContentColor = Color(0xFF607D8B),
    ),
    private val standardColors: DialogColors = DialogColors(),
) : AlertDialogColorScheme {

    override fun getColors(variant: AlertDialogVariant): DialogColors = when (variant) {
        is AlertDialogVariant.Error -> errorColors
        is AlertDialogVariant.Warning -> warningColors
        is AlertDialogVariant.Success -> successColors
        is AlertDialogVariant.Info -> infoColors
        is AlertDialogVariant.Confirmation -> confirmationColors
        else -> standardColors
    }

    override fun getIcon(variant: AlertDialogVariant): ImageVector? = when (variant) {
        is AlertDialogVariant.Error -> Icons.Default.Error
        is AlertDialogVariant.Warning -> Icons.Default.Warning
        is AlertDialogVariant.Success -> Icons.Default.CheckCircle
        is AlertDialogVariant.Info -> Icons.Default.Info
        is AlertDialogVariant.Confirmation -> Icons.Default.QuestionMark
        else -> null
    }
}

class AlertDialogThemeStrategy(
    private val colorScheme: AlertDialogColorScheme = DefaultAlertDialogColorScheme(),
) : ThemeStrategy {
    override fun applyTheme(component: KptComponent): ComponentTheme {
        if (component !is KptAlertDialogConfiguration) return DialogTheme(DialogColors())

        val variantColors = colorScheme.getColors(component.variant)
        val variantIcon = colorScheme.getIcon(component.variant)

        return DialogTheme(
            colors = variantColors,
            shape = component.shape,
            elevation = component.elevation,
        )
    }
}

// Enhanced configuration with new properties
fun KptAlertDialogConfiguration.withLoading(
    isLoading: Boolean = true,
    loadingMessage: String? = null,
): KptAlertDialogConfiguration = this.copy(
    isLoading = isLoading,
    loadingConfig = DialogLoadingConfig(message = loadingMessage),
)

fun KptAlertDialogConfiguration.withAnimation(
    animationConfig: DialogAnimationConfig,
): KptAlertDialogConfiguration = this.copy(
    animationConfig = animationConfig,
)

// Extension to the existing configuration
data class KptAlertDialogConfiguration(
    override val testTag: String? = null,
    override val contentDescription: String? = null,
    override val modifier: Modifier = Modifier,
    override val colors: DialogColors? = null,
    override val shape: Shape? = null,
    override val elevation: DialogElevation? = null,
    val variant: AlertDialogVariant = AlertDialogVariant.Standard,
    val onDismissRequest: () -> Unit,
    val content: DialogContent = DialogContent(),
    val confirmButton: DialogButton,
    val dismissButton: DialogButton? = null,
    val properties: DialogProperties = DialogProperties(),
    val isLoading: Boolean = false,
    val loadingConfig: DialogLoadingConfig = DialogLoadingConfig(),
    val animationConfig: DialogAnimationConfig = DialogAnimationConfig.Default,
) : KptComponent, Styleable

// Animation configuration
data class DialogAnimationConfig(
    val enterTransition: androidx.compose.animation.EnterTransition,
    val exitTransition: androidx.compose.animation.ExitTransition,
    val durationMillis: Int = 300,
) {
    companion object {
        val Default = DialogAnimationConfig(
            enterTransition = fadeIn() + scaleIn(
                initialScale = 0.9f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ),
            exitTransition = fadeOut() + scaleOut(targetScale = 0.9f),
        )

        val Slide = DialogAnimationConfig(
            enterTransition = fadeIn() + androidx.compose.animation.slideInVertically(
                initialOffsetY = { it / 4 },
            ),
            exitTransition = fadeOut() + androidx.compose.animation.slideOutVertically(
                targetOffsetY = { it / 4 },
            ),
        )

        val None = DialogAnimationConfig(
            enterTransition = androidx.compose.animation.EnterTransition.None,
            exitTransition = androidx.compose.animation.ExitTransition.None,
            durationMillis = 0,
        )
    }
}

// Loading configuration
data class DialogLoadingConfig(
    val message: String? = null,
    val showIconWithLoading: Boolean = false,
    val allowDismissWhileLoading: Boolean = false,
)

// Dialog style presets
object KptDialogStyles {

    // Brand-specific error style
    val BrandError = DialogColors(
        containerColor = Color(0xFFFFEBEE),
        iconContentColor = Color(0xFFD32F2F),
        titleContentColor = Color(0xFFB71C1C),
        textContentColor = Color(0xFF424242),
    )

    // Soft warning style
    val SoftWarning = DialogColors(
        containerColor = Color(0xFFFFF9C4),
        iconContentColor = Color(0xFFF57C00),
        titleContentColor = Color(0xFFE65100),
        textContentColor = Color(0xFF5D4037),
    )

    // Premium success style
    val PremiumSuccess = DialogColors(
        containerColor = Color(0xFFE0F2F1),
        iconContentColor = Color(0xFF00796B),
        titleContentColor = Color(0xFF004D40),
        textContentColor = Color(0xFF37474F),
    )

    // Dark mode style
    val DarkMode = DialogColors(
        containerColor = Color(0xFF121212),
        iconContentColor = Color(0xFFBB86FC),
        titleContentColor = Color(0xFFFFFFFF),
        textContentColor = Color(0xFFE0E0E0),
    )

    // Minimal style
    val Minimal = DialogColors(
        containerColor = Color(0xFFFAFAFA),
        iconContentColor = Color(0xFF757575),
        titleContentColor = Color(0xFF212121),
        textContentColor = Color(0xFF616161),
    )
}
