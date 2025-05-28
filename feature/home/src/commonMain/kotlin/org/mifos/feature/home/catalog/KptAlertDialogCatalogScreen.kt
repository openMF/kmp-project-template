/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.mifos.core.designsystem.icon.AppIcons
import template.core.base.designsystem.component.KptAlertDialog
import template.core.base.designsystem.component.KptBrandErrorDialog
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptConfirmationDialog
import template.core.base.designsystem.component.KptErrorDialog
import template.core.base.designsystem.component.KptInfoDialog
import template.core.base.designsystem.component.KptLoadingDialog
import template.core.base.designsystem.component.KptPremiumSuccessDialog
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptSuccessDialog
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.KptWarningDialog
import template.core.base.designsystem.core.AlertDialogVariant
import template.core.base.designsystem.core.DialogButton
import template.core.base.designsystem.core.DialogContent
import template.core.base.designsystem.core.KptAlertDialogConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptAlertDialogCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "Design System Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var showDialog by remember { mutableStateOf<DialogType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "Alert Dialog Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = DialogType.entries.toList()) { dialogType ->
                DialogCatalogItem(
                    dialogType = dialogType,
                    onClick = { showDialog = dialogType },
                )
            }
        }

        // Show the selected dialog
        when (showDialog) {
            DialogType.BASIC -> BasicDialogExample { showDialog = null }
            DialogType.STANDARD -> StandardDialogExample { showDialog = null }
            DialogType.CONFIRMATION -> ConfirmationDialogExample { showDialog = null }
            DialogType.ERROR -> ErrorDialogExample { showDialog = null }
            DialogType.WARNING -> WarningDialogExample { showDialog = null }
            DialogType.SUCCESS -> SuccessDialogExample { showDialog = null }
            DialogType.INFO -> InfoDialogExample { showDialog = null }
            DialogType.LOADING -> LoadingDialogExample { showDialog = null }
            DialogType.CUSTOM -> CustomDialogExample { showDialog = null }
            DialogType.BRAND -> BrandDialogExample { showDialog = null }
            DialogType.PREMIUM -> PremiumDialogExample { showDialog = null }
            null -> {} // No dialog shown
        }
    }
}

@Composable
private fun DialogCatalogItem(
    dialogType: DialogType,
    onClick: () -> Unit,
) {
    KptCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = dialogType.title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dialogType.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class DialogType(
    val title: String,
    val description: String,
) {
    BASIC(
        title = "Basic Dialog",
        description = "Simple dialog with title and message",
    ),
    STANDARD(
        title = "Standard Dialog",
        description = "Standard dialog with title and message",
    ),
    CONFIRMATION(
        title = "Confirmation Dialog",
        description = "Dialog with confirm/cancel actions",
    ),
    ERROR(
        title = "Error Dialog",
        description = "Shows error message with icon",
    ),
    WARNING(
        title = "Warning Dialog",
        description = "Warning message with proceed/cancel options",
    ),
    SUCCESS(
        title = "Success Dialog",
        description = "Success notification with positive action",
    ),
    INFO(
        title = "Info Dialog",
        description = "Information message with acknowledgement",
    ),
    LOADING(
        title = "Loading Dialog",
        description = "Shows progress indicator with optional message",
    ),
    CUSTOM(
        title = "Custom Dialog",
        description = "Fully customizable dialog content",
    ),
    BRAND(
        title = "Brand Dialog",
        description = "Shows progress indicator with optional message",
    ),
    PREMIUM(
        title = "Premium Dialog",
        description = "Fully customizable dialog content",
    ),
}

@Composable
private fun BasicDialogExample(onDismiss: () -> Unit) {
    KptAlertDialog(
        configuration = KptAlertDialogConfiguration(
            onDismissRequest = onDismiss,
            content = DialogContent(
                title = "Basic Dialog",
                text = "This is a simple dialog with just title and message.",
            ),
            confirmButton = DialogButton(
                onClick = { /* Handle confirm */ },
                text = "OK",
            ),
        ),
    )
}

@Composable
private fun StandardDialogExample(onDismiss: () -> Unit) {
    KptAlertDialog(
        title = "Standard Dialog",
        message = "This is a standard dialog with just title and message.",
        onDismiss = onDismiss,
    )
}

@Composable
private fun ConfirmationDialogExample(onDismiss: () -> Unit) {
    KptConfirmationDialog(
        title = "Confirmation Required",
        message = "Please confirm if you want to proceed with this action.",
        onConfirm = { /* Handle confirm */ },
        onDismiss = onDismiss,
    )
}

@Composable
private fun ErrorDialogExample(onDismiss: () -> Unit) {
    KptErrorDialog(
        title = "Error Occurred",
        text = "Something went wrong. Please try again later.",
        onDismiss = onDismiss,
    )
}

@Composable
private fun WarningDialogExample(onDismiss: () -> Unit) {
    KptWarningDialog(
        title = "Warning",
        text = "This action cannot be undone. Proceed with caution.",
        onConfirm = { /* Handle proceed */ },
        onDismiss = onDismiss,
    )
}

@Composable
private fun SuccessDialogExample(onDismiss: () -> Unit) {
    KptSuccessDialog(
        title = "Success!",
        text = "Your action was completed successfully.",
        onDismiss = onDismiss,
    )
}

@Composable
private fun InfoDialogExample(onDismiss: () -> Unit) {
    KptInfoDialog(
        title = "Information",
        text = "Here's some important information you should know.",
        onDismiss = onDismiss,
    )
}

@Composable
private fun LoadingDialogExample(onDismiss: () -> Unit) {
    KptLoadingDialog(
        title = "Processing",
        message = "Please wait while we complete your request...",
        cancellable = true,
        onDismiss = onDismiss,
    )
}

@Composable
private fun CustomDialogExample(onDismiss: () -> Unit) {
    KptAlertDialog(
        configuration = KptAlertDialogConfiguration(
            onDismissRequest = onDismiss,
            variant = AlertDialogVariant.Basic,
            content = DialogContent(
                customContent = {
                    Surface {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = AppIcons.Finance,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Custom Content",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This dialog has completely custom content layout",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                },
            ),
            confirmButton = DialogButton(
                onClick = { /* Handle confirm */ },
                text = "Got it",
            ),
        ),
    )
}

@Composable
private fun BrandDialogExample(onDismiss: () -> Unit) {
    KptBrandErrorDialog(
        title = "Processing",
        message = "Please wait while we complete your request...",
        onDismiss = onDismiss,
    )
}

@Composable
private fun PremiumDialogExample(onDismiss: () -> Unit) {
    KptPremiumSuccessDialog(
        title = "Processing",
        message = "Please wait while we complete your request...",
        onDismiss = onDismiss,
    )
}
