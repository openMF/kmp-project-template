package template.core.base.designsystem.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import template.core.base.designsystem.component.variant.FloatingActionButtonVariant
import template.core.base.designsystem.config.KptTestTags

/**
 * KPT Floating Action Button with slot API and design token support.
 *
 * @param onClick Callback for FAB click.
 * @param modifier Modifier for the FAB.
 * @param containerColor Background color.
 * @param contentColor Foreground color.
 * @param elevation Elevation of the FAB.
 * @param shape Shape of the FAB.
 * @param interactionSource Interaction source.
 * @param testTag Tag for UI testing.
 * @param contentDescription Content description for accessibility.
 * @param iconSlot Slot for the icon.
 * @param textSlot Slot for text (for extended FAB).
 * @param extended Whether to use extended FAB.
 * @param small Whether to use small FAB.
 */
@Composable
fun KptFloatingActionButton(
    variant: FloatingActionButtonVariant,
    modifier: Modifier = Modifier,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    shape: Shape = FloatingActionButtonDefaults.shape,
    interactionSource: MutableInteractionSource? = null,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    when (variant) {
        is FloatingActionButtonVariant.Small -> SmallFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = elevation,
            shape = shape,
            interactionSource = interactionSource,
            content = {
                Icon(
                    imageVector = variant.icon,
                    contentDescription = KptTestTags.FloatingActionButton,
                )
            },
        )
        is FloatingActionButtonVariant.Default -> FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = elevation,
            shape = shape,
            interactionSource = interactionSource,
            content = {
                Icon(
                    imageVector = variant.icon,
                    contentDescription = KptTestTags.FloatingActionButton,
                )
            },
        )

        is FloatingActionButtonVariant.Expanded -> ExtendedFloatingActionButton(
            icon = {
                Icon(
                    imageVector = variant.icon,
                    contentDescription = KptTestTags.FloatingActionButton,
                )
            },
            text = {
                Text(text = variant.text)
            },
            shape = shape,
            onClick = onClick,
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
            interactionSource = interactionSource,
            elevation = elevation,
        )
    }
} 