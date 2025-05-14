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

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import template.core.base.designsystem.component.variant.AlertDialogVariant

/**
 * A unified and theme-aware alert dialog component for the CMP design system.
 *
 * This composable abstracts over the standard Material3 [AlertDialog] and [BasicAlertDialog],
 * allowing developers to choose between a full-featured or minimal implementation via the [variant] parameter.
 *
 * Use [AlertDialogVariant.CUSTOM] to display a highly configurable Material3 alert dialog, or
 * [AlertDialogVariant.BASIC] to render a lightweight dialog using custom [basicContent].
 *
 * @param onDismissRequest Callback invoked when the user attempts to dismiss the dialog.
 * @param confirmButton Composable representing the confirm action button.
 * Required when [variant] is [AlertDialogVariant.CUSTOM].
 * @param modifier Modifier applied to the dialog container. *(Default: [Modifier])*
 * @param dismissButton Optional composable for a dismiss action button. *(Optional)*
 * @param icon Optional composable icon displayed above the title. *(Optional)*
 * @param title Optional composable for the dialog's title. *(Optional)*
 * @param text Optional composable for the dialog's descriptive text. *(Optional)*
 * @param shape Shape of the dialog's container. *(Default: [AlertDialogDefaults.shape])*
 * @param containerColor Background color of the dialog container. *(Default: [AlertDialogDefaults.containerColor])*
 * @param iconContentColor Color used for the icon. *(Default: [AlertDialogDefaults.iconContentColor])*
 * @param titleContentColor Color used for the title text. *(Default: [AlertDialogDefaults.titleContentColor])*
 * @param textContentColor Color used for the body text. *(Default: [AlertDialogDefaults.textContentColor])*
 * @param tonalElevation Elevation used to emphasize the dialog surface.
 * (Default: [AlertDialogDefaults.TonalElevation])*
 * @param properties Platform-specific dialog properties. *(Default: [DialogProperties()])*
 * @param variant Controls whether to show a full-featured ([AlertDialogVariant.CUSTOM])
 * or minimal ([AlertDialogVariant.BASIC]) dialog. *(Default: [AlertDialogVariant.CUSTOM])*
 * @param basicContent Custom composable content to be shown when using [AlertDialogVariant.BASIC].
 * Required when [variant] is [AlertDialogVariant.BASIC]. *(Optional otherwise)*
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CMPAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
    variant: AlertDialogVariant = AlertDialogVariant.CUSTOM,
    basicContent: @Composable (() -> Unit)? = null,
) {
    when (variant) {
        AlertDialogVariant.CUSTOM -> AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = confirmButton,
            dismissButton = dismissButton,
            icon = icon,
            title = title,
            text = text,
            shape = shape,
            containerColor = containerColor,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
            tonalElevation = tonalElevation,
            properties = properties,
            modifier = modifier,
        )

        AlertDialogVariant.BASIC -> basicContent?.let { content ->
            BasicAlertDialog(
                onDismissRequest = onDismissRequest,
                modifier = modifier,
                properties = properties,
            ) {
                content()
            }
        }
    }
}
