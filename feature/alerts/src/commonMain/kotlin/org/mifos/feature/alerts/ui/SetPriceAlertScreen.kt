/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.alerts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.model.alerts.AlertDirection
import template.core.base.store.submit.SubmitState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPriceAlertScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SetPriceAlertViewModel = koinViewModel(),
) {
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val submit by viewModel.submitState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("New Price Alert") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = form.coinId,
                onValueChange = viewModel::onCoinIdChange,
                label = { Text("Coin id (e.g. bitcoin)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = submit !is SubmitState.Submitting,
            )

            Text("Direction", style = MaterialTheme.typography.titleSmall)
            AlertDirection.entries.forEach { dir ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = form.direction == dir,
                            onClick = { viewModel.onDirectionChange(dir) },
                            enabled = submit !is SubmitState.Submitting,
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = form.direction == dir,
                        onClick = null,
                        enabled = submit !is SubmitState.Submitting,
                    )
                    Text(
                        text = directionLabel(dir),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            OutlinedTextField(
                value = if (form.targetValue == 0.0) "" else form.targetValue.toString(),
                onValueChange = { input ->
                    viewModel.onTargetValueChange(input.toDoubleOrNull() ?: 0.0)
                },
                label = {
                    Text(
                        if (form.direction == AlertDirection.PCT_CHANGE) "Change (%)" else "Target (USD)",
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = submit !is SubmitState.Submitting,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Enabled", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = form.enabled,
                    onCheckedChange = viewModel::onEnabledChange,
                    enabled = submit !is SubmitState.Submitting,
                )
            }

            Button(
                onClick = viewModel::onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = submit !is SubmitState.Submitting && form.coinId.isNotBlank() && form.targetValue > 0.0,
            ) {
                if (submit is SubmitState.Submitting) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                }
                Text("Save Alert")
            }

            SubmitStatusLine(submit, onRetry = viewModel::onRetry, onDismiss = viewModel::onDismissResult)
        }
    }
}

@Composable
private fun SubmitStatusLine(submit: SubmitState<*>, onRetry: () -> Unit, onDismiss: () -> Unit) {
    when (submit) {
        is SubmitState.Idle -> Unit
        is SubmitState.Submitting -> Text(
            "Saving…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        is SubmitState.Submitted<*> -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Alert saved ✓",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Button(onClick = onDismiss) { Text("New alert") }
            }
        }
        is SubmitState.Failed -> {
            val isOffline = submit.draftSaved
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isOffline) {
                        "Saved offline — will sync when online."
                    } else {
                        "Submit failed: ${submit.error.message ?: "unknown error"}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRetry) { Text("Retry") }
                    Button(onClick = onDismiss) { Text("Dismiss") }
                }
            }
        }
    }
}

private fun directionLabel(d: AlertDirection): String = when (d) {
    AlertDirection.ABOVE -> "Rises above"
    AlertDirection.BELOW -> "Falls below"
    AlertDirection.PCT_CHANGE -> "% change over 24h"
}
