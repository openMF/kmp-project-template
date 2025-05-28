package template.core.base.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import template.core.base.designsystem.config.KptTestTags
import template.core.base.designsystem.core.AlertDialogColorScheme
import template.core.base.designsystem.core.AlertDialogThemeStrategy
import template.core.base.designsystem.core.AlertDialogVariant
import template.core.base.designsystem.core.DefaultAlertDialogColorScheme
import template.core.base.designsystem.core.DialogButton
import template.core.base.designsystem.core.DialogColors
import template.core.base.designsystem.core.DialogContent
import template.core.base.designsystem.core.DialogTheme
import template.core.base.designsystem.core.KptAlertDialogConfiguration
import template.core.base.designsystem.core.ThemeStrategy
import template.core.base.designsystem.theme.KptTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptAlertDialog(
    configuration: KptAlertDialogConfiguration,
    colorScheme: AlertDialogColorScheme = remember { DefaultAlertDialogColorScheme() },
    themeStrategy: ThemeStrategy = remember { AlertDialogThemeStrategy(colorScheme) },
) {
    val theme = themeStrategy.applyTheme(configuration) as? DialogTheme

    // Apply composable defaults
    val finalColors = DialogColors(
        containerColor = configuration.colors?.containerColor ?: theme?.colors?.containerColor
        ?: AlertDialogDefaults.containerColor,
        iconContentColor = configuration.colors?.iconContentColor ?: theme?.colors?.iconContentColor
        ?: AlertDialogDefaults.iconContentColor,
        titleContentColor = configuration.colors?.titleContentColor
            ?: theme?.colors?.titleContentColor ?: AlertDialogDefaults.titleContentColor,
        textContentColor = configuration.colors?.textContentColor ?: theme?.colors?.textContentColor
        ?: AlertDialogDefaults.textContentColor,
    )

    val finalShape = configuration.shape ?: theme?.shape ?: AlertDialogDefaults.shape
    val finalElevation = configuration.elevation?.tonalElevation ?: theme?.elevation?.tonalElevation
    ?: AlertDialogDefaults.TonalElevation

    val finalModifier = configuration.modifier
        .testTag(configuration.testTag ?: KptTestTags.AlertDialog)
        .let { mod ->
            configuration.contentDescription?.let {
                mod.semantics { contentDescription = it }
            } ?: mod
        }

    when (configuration.variant) {
        is AlertDialogVariant.Basic -> {
            BasicAlertDialog(
                onDismissRequest = configuration.onDismissRequest,
                modifier = finalModifier,
                properties = configuration.properties,
            ) {
                configuration.content.customContent?.invoke() ?: DefaultDialogContent(
                    configuration = configuration,
                    colors = finalColors,
                    shape = finalShape,
                    elevation = finalElevation,
                )
            }
        }

        else -> {
            val variantIcon = getVariantIcon(configuration.variant)

            AlertDialog(
                onDismissRequest = configuration.onDismissRequest,
                confirmButton = {
                    TextButton(
                        onClick = {
                            configuration.confirmButton.onClick()
                            configuration.onDismissRequest()
                        },
                        enabled = configuration.confirmButton.enabled,
                    ) {
                        Text(configuration.confirmButton.text)
                    }
                },
                modifier = finalModifier,
                dismissButton = configuration.dismissButton?.let { button ->
                    {
                        TextButton(
                            onClick = {
                                button.onClick()
                                configuration.onDismissRequest()
                            },
                            enabled = button.enabled,
                        ) {
                            Text(button.text)
                        }
                    }
                },
                icon = (configuration.content.icon ?: variantIcon)?.let { icon ->
                    {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = finalColors.iconContentColor ?: Color.Unspecified,
                        )
                    }
                },
                title = configuration.content.title?.let { title ->
                    {
                        Text(
                            text = title,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                text = configuration.content.text?.let { text ->
                    {
                        Text(text = text)
                    }
                },
                shape = finalShape,
                containerColor = finalColors.containerColor ?: Color.Unspecified,
                iconContentColor = finalColors.iconContentColor ?: Color.Unspecified,
                titleContentColor = finalColors.titleContentColor ?: Color.Unspecified,
                textContentColor = finalColors.textContentColor ?: Color.Unspecified,
                tonalElevation = finalElevation,
                properties = configuration.properties,
            )
        }
    }
}

object DialogPresets {
    fun confirmation(
        title: String,
        text: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        confirmText: String = "Confirm",
        dismissText: String = "Cancel",
    ) = KptAlertDialogConfiguration(
        onDismissRequest = onDismiss,
        variant = AlertDialogVariant.Confirmation,
        content = DialogContent(title = title, text = text),
        confirmButton = DialogButton(onClick = onConfirm, text = confirmText),
        dismissButton = DialogButton(onClick = onDismiss, text = dismissText),
    )

    fun error(
        title: String,
        text: String,
        onDismiss: () -> Unit,
        buttonText: String = "OK",
    ) = KptAlertDialogConfiguration(
        onDismissRequest = onDismiss,
        variant = AlertDialogVariant.Error,
        content = DialogContent(title = title, text = text),
        confirmButton = DialogButton(onClick = onDismiss, text = buttonText),
    )

    fun warning(
        title: String,
        text: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        confirmText: String = "Proceed",
        dismissText: String = "Cancel",
    ) = KptAlertDialogConfiguration(
        onDismissRequest = onDismiss,
        variant = AlertDialogVariant.Warning,
        content = DialogContent(title = title, text = text),
        confirmButton = DialogButton(onClick = onConfirm, text = confirmText),
        dismissButton = DialogButton(onClick = onDismiss, text = dismissText),
    )

    fun success(
        title: String,
        text: String,
        onDismiss: () -> Unit,
        buttonText: String = "Great!",
    ) = KptAlertDialogConfiguration(
        onDismissRequest = onDismiss,
        variant = AlertDialogVariant.Success,
        content = DialogContent(title = title, text = text),
        confirmButton = DialogButton(onClick = onDismiss, text = buttonText),
    )

    fun info(
        title: String,
        text: String,
        onDismiss: () -> Unit,
        buttonText: String = "Got it",
    ) = KptAlertDialogConfiguration(
        onDismissRequest = onDismiss,
        variant = AlertDialogVariant.Info,
        content = DialogContent(title = title, text = text),
        confirmButton = DialogButton(onClick = onDismiss, text = buttonText),
    )
}

@Composable
fun KptConfirmationDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onDismiss: () -> Unit,
) {
    KptAlertDialog(
        configuration = DialogPresets.confirmation(
            title, text, onConfirm, onDismiss, confirmText, dismissText,
        ).copy(modifier = modifier),
    )
}

@Composable
fun KptErrorDialog(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    buttonText: String = "OK",
    onDismiss: () -> Unit,
) {
    KptAlertDialog(
        configuration = DialogPresets.error(
            title, text, onDismiss, buttonText,
        ).copy(modifier = modifier),
    )
}

@Composable
fun KptWarningDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "Proceed",
    dismissText: String = "Cancel",
    onDismiss: () -> Unit,
) {
    KptAlertDialog(
        configuration = DialogPresets.warning(
            title, text, onConfirm, onDismiss, confirmText, dismissText,
        ).copy(modifier = modifier),
    )
}

@Composable
fun KptSuccessDialog(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    buttonText: String = "Great!",
    onDismiss: () -> Unit,
) {
    KptAlertDialog(
        configuration = DialogPresets.success(
            title, text, onDismiss, buttonText,
        ).copy(modifier = modifier),
    )
}

@Composable
fun KptInfoDialog(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    buttonText: String = "Got it",
    onDismiss: () -> Unit,
) {
    KptAlertDialog(
        configuration = DialogPresets.info(
            title, text, onDismiss, buttonText,
        ).copy(modifier = modifier),
    )
}

@Composable
fun KptLoadingDialog(
    modifier: Modifier = Modifier,
    title: String = "Loading...",
    text: String? = null,
    onDismiss: () -> Unit = {},
    cancellable: Boolean = false,
) {
    KptAlertDialog(
        configuration = KptAlertDialogConfiguration(
            onDismissRequest = {
                if (cancellable) onDismiss()
            },
            variant = AlertDialogVariant.Basic,
            modifier = modifier,
            properties = DialogProperties(
                dismissOnBackPress = cancellable,
                dismissOnClickOutside = cancellable,
            ),
            content = DialogContent(
                customContent = {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = title,
                            style = KptTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        text?.let {
                            Text(
                                text = it,
                                style = KptTheme.typography.bodyMedium,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            ),
            confirmButton = DialogButton(onClick = {}, text = ""),
        ),
    )
}

private fun getVariantIcon(variant: AlertDialogVariant): ImageVector? = when (variant) {
    is AlertDialogVariant.Error -> Icons.Default.Error
    is AlertDialogVariant.Warning -> Icons.Default.Warning
    is AlertDialogVariant.Success -> Icons.Default.CheckCircle
    is AlertDialogVariant.Info -> Icons.Default.Info
    is AlertDialogVariant.Confirmation -> Icons.Default.QuestionMark
    else -> null
}

@Composable
private fun DefaultDialogContent(
    configuration: KptAlertDialogConfiguration,
    colors: DialogColors,
    shape: Shape,
    elevation: Dp,
) {
    Card(
        modifier = Modifier.padding(16.dp),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            (configuration.content.icon ?: getVariantIcon(configuration.variant))?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.iconContentColor ?: Color.Unspecified,
                    modifier = Modifier.size(48.dp),
                )
            }

            configuration.content.title?.let { title ->
                Text(
                    text = title,
                    style = KptTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            configuration.content.text?.let { text ->
                Text(
                    text = text,
                    style = KptTheme.typography.bodyMedium,
                )
            }

            configuration.content.customContent?.invoke()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                configuration.dismissButton?.let { button ->
                    TextButton(
                        onClick = {
                            button.onClick()
                            configuration.onDismissRequest()
                        },
                        enabled = button.enabled,
                    ) {
                        Text(button.text)
                    }
                }

                TextButton(
                    onClick = {
                        configuration.confirmButton.onClick()
                        configuration.onDismissRequest()
                    },
                    enabled = configuration.confirmButton.enabled,
                ) {
                    Text(configuration.confirmButton.text)
                }
            }
        }
    }
}