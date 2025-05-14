/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import template.core.base.designsystem.component.variant.ButtonVariant

/**
 * A versatile and theme-aware button component for the CMP design system.
 *
 * This composable wraps five Material3 button
 * types—[Button], [FilledTonalButton], [ElevatedButton], [OutlinedButton], and [TextButton]—
 * and exposes a unified API via the [variant] parameter to support consistent styling and flexibility.
 *
 * The appropriate button is chosen based on the [ButtonVariant] provided.
 *
 * @param onClick Callback to be invoked when the button is clicked.
 * @param modifier Modifier applied to the button. *(Default: [Modifier])*
 * @param enabled Whether the button is enabled and clickable. *(Default: `true`)*
 * @param variant The style variant of the button to use. *(Default: [ButtonVariant.FILLED])*
 * @param colors Color configuration for the button.
 * (Optional; defaults to the variant's default via [ButtonDefaults])*
 * @param elevation Elevation of the button.
 * (Optional; defaults to the variant's elevation or `null` for flat types like text/outlined)*
 * @param border Border stroke for the button. *(Optional; only applied in outlined default via [ButtonDefaults])*
 * @param shape The shape of the button's container.
 * (Optional; defaults to the variant's shape via [ButtonDefaults])*
 * @param interactionSource The [MutableInteractionSource] representing interaction state. *(Optional)*
 * @param contentPadding Padding values for the button content.
 * (Optional; defaults to the variant's padding via [ButtonDefaults])*
 * @param content Composable content inside the button, scoped to a [RowScope] for alignment flexibility.
 */
@Suppress("CyclomaticComplexMethod")
@Composable
fun CMPButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.FILLED,
    colors: ButtonColors? = null,
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    shape: Shape? = null,
    interactionSource: MutableInteractionSource? = null,
    contentPadding: PaddingValues? = null,
    content: @Composable RowScope.() -> Unit,
) {
    when (variant) {
        ButtonVariant.FILLED -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = colors ?: ButtonDefaults.buttonColors(),
            shape = shape ?: ButtonDefaults.shape,
            elevation = elevation ?: ButtonDefaults.buttonElevation(),
            border = border,
            interactionSource = interactionSource,
            contentPadding = contentPadding ?: ButtonDefaults.ContentPadding,
        ) {
            content()
        }

        ButtonVariant.TONAL -> FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = colors ?: ButtonDefaults.filledTonalButtonColors(),
            shape = shape ?: ButtonDefaults.filledTonalShape,
            elevation = elevation ?: ButtonDefaults.filledTonalButtonElevation(),
            border = border,
            interactionSource = interactionSource,
            contentPadding = contentPadding ?: ButtonDefaults.ContentPadding,
        ) {
            content()
        }

        ButtonVariant.ELEVATED -> ElevatedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = colors ?: ButtonDefaults.elevatedButtonColors(),
            shape = shape ?: ButtonDefaults.elevatedShape,
            elevation = elevation ?: ButtonDefaults.elevatedButtonElevation(),
            border = border,
            interactionSource = interactionSource,
            contentPadding = contentPadding ?: ButtonDefaults.ContentPadding,
        ) {
            content()
        }

        ButtonVariant.OUTLINED -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = colors ?: ButtonDefaults.outlinedButtonColors(),
            elevation = elevation,
            shape = shape ?: ButtonDefaults.outlinedShape,
            border = border ?: ButtonDefaults.outlinedButtonBorder(enabled),
            interactionSource = interactionSource,
            contentPadding = contentPadding ?: ButtonDefaults.ContentPadding,
        ) {
            content()
        }

        ButtonVariant.TEXT -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = colors ?: ButtonDefaults.textButtonColors(),
            shape = shape ?: ButtonDefaults.textShape,
            elevation = elevation,
            border = border,
            interactionSource = interactionSource,
            contentPadding = contentPadding ?: ButtonDefaults.TextButtonContentPadding,
        ) {
            content()
        }
    }
}
