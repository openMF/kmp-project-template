package template.core.base.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import template.core.base.designsystem.component.variant.AlertDialogVariant
import template.core.base.designsystem.config.KptTestTags

/**
 * A customizable dialog for the KPT design system, supporting both
 * Material3 AlertDialog and BasicAlertDialog.
 *
 * @param onDismissRequest Callback for dialog dismissal.
 * @param confirmButton Composable for the confirm button.
 * @param modifier Modifier for the dialog. *(Default: [Modifier])*
 * @param dismissButton Optional dismiss button.
 * @param icon Optional icon above title.
 * @param title Optional title composable.
 * @param text Optional body text.
 * @param shape Dialog shape. *(Default: design token or
 *    [AlertDialogDefaults.shape])*
 * @param containerColor Background color. *(Default:
 *    [AlertDialogDefaults.containerColor])*
 * @param iconContentColor Icon color. *(Default:
 *    [AlertDialogDefaults.iconContentColor])*
 * @param titleContentColor Title color. *(Default:
 *    [AlertDialogDefaults.titleContentColor])*
 * @param textContentColor Text color. *(Default:
 *    [AlertDialogDefaults.textContentColor])*
 * @param tonalElevation Elevation. *(Default: design token or
 *    [AlertDialogDefaults.TonalElevation])*
 * @param properties Platform-specific properties. *(Default:
 *    [DialogProperties()])*
 * @param variant Dialog style. *(Default: [AlertDialogVariant.CUSTOM])*
 * @param basicContent Custom content for `BASIC` variant.
 * @param testTag Tag for UI testing. *(Optional)*
 * @param contentDescription Content description for accessibility.
 *    *(Optional)*
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptAlertDialog(
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
    testTag: String? = null,
    contentDescription: String? = null,
    variant: AlertDialogVariant = AlertDialogVariant.CUSTOM,
    basicContent: @Composable (() -> Unit)? = null,
) {
    val dialogModifier = modifier
        .then(Modifier.testTag(testTag ?: KptTestTags.AlertDialog))
        .then(
            if (contentDescription != null) Modifier.semantics {
                this.contentDescription = contentDescription
            } else Modifier,
        )

    when (variant) {
        AlertDialogVariant.CUSTOM -> AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = confirmButton,
            modifier = dialogModifier,
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
        )

        AlertDialogVariant.BASIC -> BasicAlertDialog(
            onDismissRequest = onDismissRequest,
            modifier = dialogModifier,
            properties = properties,
            content = basicContent ?: {},
        )
    }
}