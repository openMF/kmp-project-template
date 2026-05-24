/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.bills.ui

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
import androidx.compose.material3.FilterChip
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
import org.koin.core.parameter.parametersOf
import org.mifos.core.model.banking.BillCategory
import org.mifos.core.model.banking.Recurrence
import template.core.base.store.submit.SubmitState

/**
 * Add-or-edit screen for a single bill reminder.
 *
 * - When [billId] is `null` the screen creates a brand-new row on submit.
 * - When [billId] is non-null the `EditBillReminderViewModel` rehydrates the form from
 *   the repository on first composition.
 *
 * Submit-state surfacing matches the alerts screen pattern: a status line under the
 * submit button shows Saving / Saved / Failed-with-retry, and the Save button + every
 * input are disabled while a submit is in flight.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditBillReminderScreen(
    onBackClick: () -> Unit,
    billId: String?,
    modifier: Modifier = Modifier,
    viewModel: EditBillReminderViewModel = koinViewModel { parametersOf(billId) },
) {
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val submit = ui.submit
    val title = if (billId == null) "New bill reminder" else "Edit bill reminder"

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
                value = form.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Name (e.g. Electricity)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = submit !is SubmitState.Submitting,
            )

            OutlinedTextField(
                value = if (form.amount == 0.0) "" else form.amount.toString(),
                onValueChange = { value -> viewModel.onAmountChange(value.toDoubleOrNull() ?: 0.0) },
                label = { Text("Amount") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = submit !is SubmitState.Submitting,
            )

            OutlinedTextField(
                value = form.dueDay.toString(),
                onValueChange = { value -> viewModel.onDueDayChange(value.toIntOrNull() ?: 1) },
                label = { Text("Due day of month (1-31)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = submit !is SubmitState.Submitting,
            )

            Text("Recurrence", style = MaterialTheme.typography.titleSmall)
            Recurrence.entries.forEach { rec ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = form.recurrence == rec,
                            onClick = { viewModel.onRecurrenceChange(rec) },
                            enabled = submit !is SubmitState.Submitting,
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = form.recurrence == rec,
                        onClick = null,
                        enabled = submit !is SubmitState.Submitting,
                    )
                    Text(text = recurrenceLabel(rec), modifier = Modifier.padding(start = 8.dp))
                }
            }

            Text("Category", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BillCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = form.category == cat,
                        onClick = { viewModel.onCategoryChange(cat) },
                        label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        enabled = submit !is SubmitState.Submitting,
                    )
                }
            }

            OutlinedTextField(
                value = form.reminderDaysBefore.toString(),
                onValueChange = { value ->
                    viewModel.onReminderDaysBeforeChange(value.toIntOrNull() ?: 1)
                },
                label = { Text("Remind days before") },
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
                enabled = submit !is SubmitState.Submitting &&
                    form.name.isNotBlank() &&
                    form.amount > 0.0,
            ) {
                if (submit is SubmitState.Submitting) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                }
                Text("Save")
            }

            SubmitStatusLine(
                submit = submit,
                onRetry = viewModel::onRetry,
                onDismiss = viewModel::onDismissResult,
            )
        }
    }
}

@Composable
private fun SubmitStatusLine(
    submit: SubmitState<*>,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
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
                    text = "Saved ✓",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Button(onClick = onDismiss) { Text("Add another") }
            }
        }
        is SubmitState.Failed -> {
            val isOffline = submit.draftSaved
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isOffline) {
                        "Saved offline — will sync when online."
                    } else {
                        "Save failed: ${submit.error.message ?: "unknown error"}"
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

private fun recurrenceLabel(rec: Recurrence): String = when (rec) {
    Recurrence.MONTHLY -> "Monthly"
    Recurrence.QUARTERLY -> "Quarterly"
    Recurrence.ANNUALLY -> "Annually"
    Recurrence.ONCE -> "One-time"
}
