package template.core.base.designsystem.component.variant

import androidx.compose.ui.graphics.vector.ImageVector

sealed class FloatingActionButtonVariant(
    icon: ImageVector,
    text: String? = null,
) {
    data class Default(val icon: ImageVector) : FloatingActionButtonVariant(icon)

    data class Expanded(val text: String, val icon: ImageVector) :
        FloatingActionButtonVariant(icon, text)

    data class Small(val icon: ImageVector): FloatingActionButtonVariant(icon)
}