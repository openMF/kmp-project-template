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

@Immutable
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
) : KptComponent, Styleable

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