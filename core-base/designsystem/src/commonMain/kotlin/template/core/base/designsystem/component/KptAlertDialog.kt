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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import template.core.base.designsystem.config.KptTestTags
import template.core.base.designsystem.core.AlertDialogColorScheme
import template.core.base.designsystem.core.AlertDialogThemeStrategy
import template.core.base.designsystem.core.AlertDialogVariant
import template.core.base.designsystem.core.DefaultAlertDialogColorScheme
import template.core.base.designsystem.core.DialogAnimationConfig
import template.core.base.designsystem.core.DialogButton
import template.core.base.designsystem.core.DialogColors
import template.core.base.designsystem.core.DialogContent
import template.core.base.designsystem.core.DialogLoadingConfig
import template.core.base.designsystem.core.DialogTheme
import template.core.base.designsystem.core.KptAlertDialogConfiguration
import template.core.base.designsystem.core.KptDialogStyles
import template.core.base.designsystem.core.ThemeStrategy
import template.core.base.designsystem.theme.KptTheme

// Enhanced configuration with new features
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptAlertDialog(
    configuration: KptAlertDialogConfiguration,
    colorScheme: AlertDialogColorScheme = remember { DefaultAlertDialogColorScheme() },
    themeStrategy: ThemeStrategy = remember { AlertDialogThemeStrategy(colorScheme) },
    animationConfig: DialogAnimationConfig = DialogAnimationConfig.Default,
) {
    val theme = themeStrategy.applyTheme(configuration) as? DialogTheme

    // Animation state management
    val visibleState = remember { MutableTransitionState(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        visibleState.targetState = true
    }

    // Error handling wrapper
    val safeConfiguration = remember(configuration) {
        validateConfiguration(configuration)
    }

    // Create dismiss handler
    val handleDismiss: () -> Unit = remember(visibleState, safeConfiguration, animationConfig) {
        {
            scope.launch {
                visibleState.targetState = false
                delay(animationConfig.durationMillis.toLong())
                safeConfiguration.onDismissRequest()
            }
        }
    }

    // Apply composable defaults
    val finalColors = DialogColors(
        containerColor = safeConfiguration.colors?.containerColor ?: theme?.colors?.containerColor
            ?: AlertDialogDefaults.containerColor,
        iconContentColor = safeConfiguration.colors?.iconContentColor
            ?: theme?.colors?.iconContentColor
            ?: AlertDialogDefaults.iconContentColor,
        titleContentColor = safeConfiguration.colors?.titleContentColor
            ?: theme?.colors?.titleContentColor ?: AlertDialogDefaults.titleContentColor,
        textContentColor = safeConfiguration.colors?.textContentColor
            ?: theme?.colors?.textContentColor
            ?: AlertDialogDefaults.textContentColor,
    )

    val finalShape = safeConfiguration.shape ?: theme?.shape ?: AlertDialogDefaults.shape
    val finalElevation =
        safeConfiguration.elevation?.tonalElevation ?: theme?.elevation?.tonalElevation
            ?: AlertDialogDefaults.TonalElevation

    val finalModifier = safeConfiguration.modifier
        .testTag(safeConfiguration.testTag ?: KptTestTags.ALERT_DIALOG)
        .let { mod ->
            safeConfiguration.contentDescription?.let {
                mod.semantics { contentDescription = it }
            } ?: mod
        }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = animationConfig.enterTransition,
        exit = animationConfig.exitTransition,
    ) {
        when (safeConfiguration.variant) {
            is AlertDialogVariant.Basic -> {
                BasicAlertDialog(
                    onDismissRequest = {
                        if (!safeConfiguration.isLoading) {
                            handleDismiss()
                        }
                    },
                    modifier = finalModifier,
                    properties = safeConfiguration.properties,
                ) {
                    safeConfiguration.content.customContent?.invoke() ?: EnhancedDialogContent(
                        configuration = safeConfiguration,
                        colors = finalColors,
                        shape = finalShape,
                        elevation = finalElevation,
                        onDismiss = handleDismiss,
                        isLoading = safeConfiguration.isLoading,
                    )
                }
            }

            else -> {
                val variantIcon = getVariantIcon(safeConfiguration.variant)

                AlertDialog(
                    onDismissRequest = {
                        if (!safeConfiguration.isLoading) {
                            handleDismiss()
                        }
                    },
                    confirmButton = {
                        DialogActionButton(
                            button = safeConfiguration.confirmButton,
                            isLoading = safeConfiguration.isLoading,
                            enabled = !safeConfiguration.isLoading,
                            onDismiss = handleDismiss,
                        )
                    },
                    modifier = finalModifier,
                    dismissButton = safeConfiguration.dismissButton?.let { button ->
                        {
                            DialogActionButton(
                                button = button,
                                enabled = !safeConfiguration.isLoading,
                                onDismiss = handleDismiss,
                            )
                        }
                    },
                    icon = (safeConfiguration.content.icon ?: variantIcon)?.let { icon ->
                        {
                            Box(contentAlignment = Alignment.Center) {
                                if (safeConfiguration.isLoading && safeConfiguration.loadingConfig.showIconWithLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        strokeWidth = 2.dp,
                                    )
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = finalColors.iconContentColor ?: Color.Unspecified,
                                    modifier = Modifier.size(if (safeConfiguration.isLoading) 24.dp else 32.dp),
                                )
                            }
                        }
                    },
                    title = safeConfiguration.content.title?.let { title ->
                        {
                            Text(
                                text = title,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    },
                    text = {
                        Column {
                            safeConfiguration.content.text?.let { text ->
                                Text(text = text)
                            }

                            if (safeConfiguration.isLoading && safeConfiguration.loadingConfig.message != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (!safeConfiguration.loadingConfig.showIconWithLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = safeConfiguration.loadingConfig.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    },
                    shape = finalShape,
                    containerColor = finalColors.containerColor ?: Color.Unspecified,
                    iconContentColor = finalColors.iconContentColor ?: Color.Unspecified,
                    titleContentColor = finalColors.titleContentColor ?: Color.Unspecified,
                    textContentColor = finalColors.textContentColor ?: Color.Unspecified,
                    tonalElevation = finalElevation,
                    properties = DialogProperties(
                        dismissOnBackPress = safeConfiguration.properties.dismissOnBackPress && !safeConfiguration.isLoading,
                        dismissOnClickOutside = safeConfiguration.properties.dismissOnClickOutside && !safeConfiguration.isLoading,
                    ),
                )
            }
        }
    }
}

// Simplified API functions for common use cases
@Composable
fun KptAlertDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmText: String = "OK",
    variant: AlertDialogVariant = AlertDialogVariant.Standard,
    icon: ImageVector? = null,
) {
    KptAlertDialog(
        configuration = KptAlertDialogConfiguration(
            onDismissRequest = onDismiss,
            variant = variant,
            content = DialogContent(title = title, text = message, icon = icon),
            confirmButton = DialogButton(onClick = {}, text = confirmText),
        ),
    )
}

@Composable
fun KptConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onDismiss: () -> Unit,
) {
    KptAlertDialog(
        configuration = DialogPresets.confirmation(
            title,
            text = message,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            confirmText = confirmText,
            dismissText = dismissText,
            isDestructive = isDestructive,
        ).copy(modifier = modifier),
    )
}

@Composable
fun KptLoadingDialog(
    modifier: Modifier = Modifier,
    title: String = "Loading",
    message: String? = "Please wait...",
    onDismiss: (() -> Unit) = {},
    cancellable: Boolean = false,
) {
    KptAlertDialog(
        configuration = KptAlertDialogConfiguration(
            onDismissRequest = {
                if (cancellable) onDismiss()
            },
            variant = AlertDialogVariant.Basic,
            content = DialogContent(
                customContent = {
                    LoadingDialogContent(title = title, message = message)
                },
            ),
            confirmButton = DialogButton(onClick = {}, text = ""),
            isLoading = true,
            properties = DialogProperties(
                dismissOnBackPress = cancellable,
                dismissOnClickOutside = cancellable,
            ),
            loadingConfig = DialogLoadingConfig(
                message = message ?: "Loading...",
                showIconWithLoading = true,
                allowDismissWhileLoading = cancellable,
            ),
            modifier = modifier,
        ),
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
            title,
            text,
            onDismiss,
            buttonText,
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
            title,
            text,
            onConfirm,
            onDismiss,
            confirmText,
            dismissText,
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
            title,
            text,
            onDismiss,
            buttonText,
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
            title,
            text,
            onDismiss,
            buttonText,
        ).copy(modifier = modifier),
    )
}

@Composable
fun KptBrandErrorDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
) {
    KptAlertDialog(
        configuration = KptAlertDialogConfiguration(
            variant = AlertDialogVariant.Error,
            content = DialogContent(
                title = title,
                text = message,
            ),
            confirmButton = DialogButton(
                text = if (onRetry != null) "Retry" else "OK",
                onClick = onRetry ?: {},
            ),
            dismissButton = if (onRetry != null) {
                DialogButton(
                    text = "Cancel",
                    onClick = {},
                )
            } else {
                null
            },
            onDismissRequest = onDismiss,
            colors = KptDialogStyles.BrandError,
        ),
    )
}

@Composable
fun KptPremiumSuccessDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    actionText: String = "Continue",
) {
    KptAlertDialog(
        configuration = KptAlertDialogConfiguration(
            variant = AlertDialogVariant.Success,
            content = DialogContent(
                title = title,
                text = message,
            ),
            confirmButton = DialogButton(
                text = actionText,
                onClick = {},
            ),
            onDismissRequest = onDismiss,
            colors = KptDialogStyles.PremiumSuccess,
        ),
    )
}

@Composable
private fun EnhancedDialogContent(
    configuration: KptAlertDialogConfiguration,
    colors: DialogColors,
    shape: Shape,
    elevation: Dp,
    onDismiss: () -> Unit,
    isLoading: Boolean,
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
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 3.dp,
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colors.iconContentColor ?: Color.Unspecified,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
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

            if (isLoading && configuration.loadingConfig.message != null) {
                Text(
                    text = configuration.loadingConfig.message,
                    style = KptTheme.typography.bodySmall,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            configuration.content.customContent?.invoke()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                configuration.dismissButton?.let { button ->
                    DialogActionButton(
                        button = button,
                        enabled = !isLoading || configuration.loadingConfig.allowDismissWhileLoading,
                        onDismiss = onDismiss,
                    )
                }

                DialogActionButton(
                    button = configuration.confirmButton,
                    isLoading = isLoading,
                    enabled = !isLoading,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun DialogActionButton(
    button: DialogButton,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
) {
    TextButton(
        onClick = {
            button.onClick()
            if (!isLoading) {
                onDismiss()
            }
        },
        enabled = enabled && button.enabled,
        modifier = modifier,
    ) {
        if (isLoading && button.text == "OK") {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Text(button.text)
        }
    }
}

@Composable
private fun LoadingDialogContent(
    title: String,
    message: String?,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 3.dp,
            )
            Text(
                text = title,
                style = KptTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            message?.let {
                Text(
                    text = it,
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// Helper functions and validation
private fun validateConfiguration(config: KptAlertDialogConfiguration): KptAlertDialogConfiguration {
    return config.copy(
        content = config.content.copy(
            title = config.content.title?.takeIf { it.isNotBlank() },
            text = config.content.text?.takeIf { it.isNotBlank() },
        ),
        confirmButton = config.confirmButton.copy(
            text = config.confirmButton.text.ifBlank { "OK" },
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

object DialogPresets {
    @Composable
    fun confirmation(
        title: String,
        text: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        confirmText: String = "Confirm",
        dismissText: String = "Cancel",
        isDestructive: Boolean = false,
    ) = KptAlertDialogConfiguration(
        onDismissRequest = onDismiss,
        variant = if (isDestructive) AlertDialogVariant.Warning else AlertDialogVariant.Confirmation,
        content = DialogContent(title = title, text = text),
        confirmButton = DialogButton(onClick = onConfirm, text = confirmText),
        dismissButton = DialogButton(onClick = onDismiss, text = dismissText),
        colors = if (isDestructive) {
            DialogColors(
                containerColor = KptTheme.colorScheme.errorContainer,
                iconContentColor = KptTheme.colorScheme.error,
            )
        } else {
            null
        },
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
