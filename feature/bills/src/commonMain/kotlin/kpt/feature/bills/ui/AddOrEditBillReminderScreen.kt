/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.bills.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.store.submit.SubmitState
import kpt.core.designsystem.theme.spacing
import kpt.core.model.demo.banking.BillCategory
import kpt.core.model.demo.banking.Recurrence
import kpt.feature.bills.generated.resources.Res
import kpt.feature.bills.generated.resources.screens_bills_add_title
import kpt.feature.bills.generated.resources.screens_bills_addedit_amount_label
import kpt.feature.bills.generated.resources.screens_bills_addedit_back_cd
import kpt.feature.bills.generated.resources.screens_bills_addedit_category_title
import kpt.feature.bills.generated.resources.screens_bills_addedit_dismiss_button
import kpt.feature.bills.generated.resources.screens_bills_addedit_due_day_label
import kpt.feature.bills.generated.resources.screens_bills_addedit_enabled_label
import kpt.feature.bills.generated.resources.screens_bills_addedit_name_label
import kpt.feature.bills.generated.resources.screens_bills_addedit_recurrence_title
import kpt.feature.bills.generated.resources.screens_bills_addedit_remind_days_before_label
import kpt.feature.bills.generated.resources.screens_bills_addedit_retry_button
import kpt.feature.bills.generated.resources.screens_bills_addedit_save_button
import kpt.feature.bills.generated.resources.screens_bills_addedit_status_add_another
import kpt.feature.bills.generated.resources.screens_bills_addedit_status_failed
import kpt.feature.bills.generated.resources.screens_bills_addedit_status_failed_unknown
import kpt.feature.bills.generated.resources.screens_bills_addedit_status_offline
import kpt.feature.bills.generated.resources.screens_bills_addedit_status_saved
import kpt.feature.bills.generated.resources.screens_bills_addedit_status_saving
import kpt.feature.bills.generated.resources.screens_bills_edit_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
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
    val title = stringResource(
        if (billId == null) Res.string.screens_bills_add_title else Res.string.screens_bills_edit_title,
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_bills_addedit_back_cd),
                        )
                    }
                },
            )
        },
    ) { padding ->
        val sp = MaterialTheme.spacing
        val isSubmitting = submit is SubmitState.Submitting
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(sp.lg),
            verticalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            BasicInfoSection(
                form = form,
                isSubmitting = isSubmitting,
                onNameChange = viewModel::onNameChange,
                onAmountChange = viewModel::onAmountChange,
                onDueDayChange = viewModel::onDueDayChange,
            )

            RecurrenceSection(
                selected = form.recurrence,
                isSubmitting = isSubmitting,
                onChange = viewModel::onRecurrenceChange,
            )

            CategorySection(
                selected = form.category,
                isSubmitting = isSubmitting,
                onChange = viewModel::onCategoryChange,
            )

            ReminderSettingsSection(
                reminderDaysBefore = form.reminderDaysBefore,
                enabled = form.enabled,
                isSubmitting = isSubmitting,
                onReminderDaysBeforeChange = viewModel::onReminderDaysBeforeChange,
                onEnabledChange = viewModel::onEnabledChange,
            )

            Button(
                onClick = viewModel::onSubmit,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.AddOrEditBill.SAVE_BUTTON),
                enabled = submit !is SubmitState.Submitting &&
                    form.name.isNotBlank() &&
                    form.amount > 0.0,
            ) {
                if (submit is SubmitState.Submitting) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                }
                Text(stringResource(Res.string.screens_bills_addedit_save_button))
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
private fun SubmitStatusLine(submit: SubmitState<*>, onRetry: () -> Unit, onDismiss: () -> Unit) {
    when (submit) {
        is SubmitState.Idle -> Unit
        is SubmitState.Submitting -> Text(
            text = stringResource(Res.string.screens_bills_addedit_status_saving),
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
                    text = stringResource(Res.string.screens_bills_addedit_status_saved),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Button(onClick = onDismiss) {
                    Text(stringResource(Res.string.screens_bills_addedit_status_add_another))
                }
            }
        }
        is SubmitState.Failed -> {
            val isOffline = submit.draftSaved
            val unknown = stringResource(Res.string.screens_bills_addedit_status_failed_unknown)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isOffline) {
                        stringResource(Res.string.screens_bills_addedit_status_offline)
                    } else {
                        stringResource(
                            Res.string.screens_bills_addedit_status_failed,
                            submit.error.message ?: unknown,
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRetry) {
                        Text(stringResource(Res.string.screens_bills_addedit_retry_button))
                    }
                    Button(onClick = onDismiss) {
                        Text(stringResource(Res.string.screens_bills_addedit_dismiss_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun BasicInfoSection(
    form: BillReminderFormState,
    isSubmitting: Boolean,
    onNameChange: (String) -> Unit,
    onAmountChange: (Double) -> Unit,
    onDueDayChange: (Int) -> Unit,
) {
    val sp = MaterialTheme.spacing
    AppCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(sp.md),
            verticalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            OutlinedTextField(
                value = form.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(Res.string.screens_bills_addedit_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = if (form.amount == 0.0) "" else form.amount.toString(),
                onValueChange = { value -> onAmountChange(value.toDoubleOrNull() ?: 0.0) },
                label = { Text(stringResource(Res.string.screens_bills_addedit_amount_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = form.dueDay.toString(),
                onValueChange = { value -> onDueDayChange(value.toIntOrNull() ?: 1) },
                label = { Text(stringResource(Res.string.screens_bills_addedit_due_day_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
            )
        }
    }
}

@Composable
private fun RecurrenceSection(selected: Recurrence, isSubmitting: Boolean, onChange: (Recurrence) -> Unit) {
    val sp = MaterialTheme.spacing
    AppCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(sp.md),
            verticalArrangement = Arrangement.spacedBy(sp.sm),
        ) {
            Text(
                text = stringResource(Res.string.screens_bills_addedit_recurrence_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Recurrence.entries.forEach { rec ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected == rec,
                            onClick = { onChange(rec) },
                            enabled = !isSubmitting,
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selected == rec,
                        onClick = null,
                        enabled = !isSubmitting,
                    )
                    Text(text = recurrenceLabel(rec), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun CategorySection(selected: BillCategory, isSubmitting: Boolean, onChange: (BillCategory) -> Unit) {
    val sp = MaterialTheme.spacing
    AppCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(sp.md),
            verticalArrangement = Arrangement.spacedBy(sp.sm),
        ) {
            Text(
                text = stringResource(Res.string.screens_bills_addedit_category_title),
                style = MaterialTheme.typography.titleSmall,
            )
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(sp.sm),
                verticalArrangement = Arrangement.spacedBy(sp.sm),
            ) {
                BillCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = selected == cat,
                        onClick = { onChange(cat) },
                        label = {
                            Text(
                                cat.name.lowercase().replaceFirstChar { it.uppercase() },
                                maxLines = 1,
                            )
                        },
                        enabled = !isSubmitting,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderSettingsSection(
    reminderDaysBefore: Int,
    enabled: Boolean,
    isSubmitting: Boolean,
    onReminderDaysBeforeChange: (Int) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
) {
    val sp = MaterialTheme.spacing
    AppCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(sp.md),
            verticalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            OutlinedTextField(
                value = reminderDaysBefore.toString(),
                onValueChange = { value -> onReminderDaysBeforeChange(value.toIntOrNull() ?: 1) },
                label = { Text(stringResource(Res.string.screens_bills_addedit_remind_days_before_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.screens_bills_addedit_enabled_label),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    enabled = !isSubmitting,
                )
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
