/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import org.jetbrains.compose.ui.tooling.preview.Preview
import template.core.base.designsystem.component.variant.ButtonVariant

@Composable
fun KptBasicDialog(
    visibilityState: BasicDialogState,
    onDismissRequest: () -> Unit,
): Unit = when (visibilityState) {
    BasicDialogState.Hidden -> Unit
    is BasicDialogState.Shown -> {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                KptButton(
                    content = {
                        Text(text = "Ok")
                    },
                    onClick = onDismissRequest,
                    variant = ButtonVariant.TEXT,
                    modifier = Modifier.testTag("AcceptAlertButton"),
                )
            },
            title = visibilityState.title.let {
                {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.testTag("AlertTitleText"),
                    )
                }
            },
            text = {
                Text(
                    text = visibilityState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("AlertContentText"),
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.semantics {
                testTag = "AlertPopup"
            },
        )
    }
}

@Composable
fun KptBasicDialog(
    visibilityState: BasicDialogState,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
): Unit = when (visibilityState) {
    BasicDialogState.Hidden -> Unit
    is BasicDialogState.Shown -> {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                KptButton(
                    content = {
                        Text(text = "Ok")
                    },
                    onClick = onConfirm,
                    variant = ButtonVariant.TEXT,
                    modifier = Modifier.testTag("AcceptAlertButton"),
                )
            },
            dismissButton = {
                KptButton(
                    content = {
                        Text(text = "Cancel")
                    },
                    onClick = onDismissRequest,
                    variant = ButtonVariant.TEXT,
                    modifier = Modifier.testTag("DismissAlertButton"),
                )
            },
            title = visibilityState.title.let {
                {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.testTag("AlertTitleText"),
                    )
                }
            },
            text = {
                Text(
                    text = visibilityState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("AlertContentText"),
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.semantics {
                testTag = "AlertPopup"
            },
        )
    }
}

@Preview
@Composable
private fun KptBasicDialog_preview() {
    MaterialTheme {
        KptBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = "An error has occurred.",
                message = "Username or password is incorrect. Try again.",
            ),
            onDismissRequest = {},
        )
    }
}

/**
 * Models display of a [KptBasicDialog].
 */
sealed class BasicDialogState {

    data object Hidden : BasicDialogState()

    data class Shown(
        val message: String,
        val title: String = "An Error Occurred!",
    ) : BasicDialogState()
}
