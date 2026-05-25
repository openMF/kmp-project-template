/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.loans.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.mifos.core.model.banking.LoanKind
import template.core.base.store.submit.SubmitState

/**
 * Combined add/edit screen.
 *
 * `loanId == null` → add a brand-new loan. `loanId != null` → edit. The same VM handles both
 * via `EditLoanViewModel(loanId)` + Koin parametersOf, and the same DraftSubmitHandler shape
 * applies: success → "Saved ✓", offline → "Saved offline — will sync when online."
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditLoanScreen(
    loanId: String?,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    // Key behaviour:
    //   • Edit (loanId non-null): stable key per loan id → VM survives configuration changes.
    //   • Add  (loanId null):     pass null so Koin uses the per-NavBackStackEntry key,
    //                              giving each "+ New loan" tap a fresh VM. Without this,
    //                              the previous Add VM is reused with state stuck in
    //                              SubmitState.Submitted and the form pre-filled with the
    //                              prior loan's data — so the next Save looks like a no-op.
    viewModel: EditLoanViewModel = koinViewModel(key = loanId) {
        parametersOf(loanId)
    },
) {
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val submit = ui.submit

    LaunchedEffect(submit) {
        if (submit is SubmitState.Submitted<*>) {
            onSaved()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (loanId == null) "Add Loan" else "Edit Loan") },
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
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = form.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Loan name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = submit !is SubmitState.Submitting,
            )

            LoanKindDropdown(
                value = form.kind,
                onChange = viewModel::onKindChange,
                enabled = submit !is SubmitState.Submitting,
            )

            DoubleField(
                label = "Original principal",
                value = form.principal,
                onChange = viewModel::onPrincipalChange,
                enabled = submit !is SubmitState.Submitting,
            )
            DoubleField(
                label = "Principal remaining",
                value = form.principalRemaining,
                onChange = viewModel::onPrincipalRemainingChange,
                enabled = submit !is SubmitState.Submitting,
            )
            DoubleField(
                label = "Annual interest rate (%)",
                value = form.annualRatePercent,
                onChange = viewModel::onAnnualRatePercentChange,
                enabled = submit !is SubmitState.Submitting,
            )
            IntField(
                label = "Tenure (months)",
                value = form.tenureMonths,
                onChange = viewModel::onTenureMonthsChange,
                enabled = submit !is SubmitState.Submitting,
            )
            IntField(
                label = "Months remaining",
                value = form.monthsRemaining,
                onChange = viewModel::onMonthsRemainingChange,
                enabled = submit !is SubmitState.Submitting,
            )
            DateField(
                label = "Next due date (YYYY-MM-DD)",
                value = form.nextDueDate,
                onChange = viewModel::onNextDueDateChange,
                enabled = submit !is SubmitState.Submitting,
            )
            DoubleField(
                label = "Total paid to date",
                value = form.totalPaid,
                onChange = viewModel::onTotalPaidChange,
                enabled = submit !is SubmitState.Submitting,
            )

            ComputedPreviewCard(
                monthlyEmi = form.previewMonthlyEmi,
                totalInterest = form.previewTotalInterest,
            )

            Button(
                onClick = viewModel::onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = form.isValid && submit !is SubmitState.Submitting,
            ) {
                if (submit is SubmitState.Submitting) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                }
                Text(if (loanId == null) "Save Loan" else "Update Loan")
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
private fun LoanKindDropdown(
    value: LoanKind,
    onChange: (LoanKind) -> Unit,
    enabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = loanKindLabel(value),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Kind") },
            trailingIcon = {
                IconButton(onClick = { if (enabled) expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Choose loan kind",
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LoanKind.entries.forEach { kind ->
                DropdownMenuItem(
                    text = { Text(loanKindLabel(kind)) },
                    onClick = {
                        onChange(kind)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun DoubleField(
    label: String,
    value: Double,
    onChange: (Double) -> Unit,
    enabled: Boolean,
) {
    OutlinedTextField(
        value = if (value == 0.0) "" else value.toString(),
        onValueChange = { input -> onChange(input.toDoubleOrNull() ?: 0.0) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
    )
}

@Composable
private fun IntField(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    enabled: Boolean,
) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { input -> onChange(input.toIntOrNull() ?: 0) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
    )
}

@Composable
private fun DateField(
    label: String,
    value: LocalDate,
    onChange: (LocalDate) -> Unit,
    enabled: Boolean,
) {
    // Simple ISO date input — keeps the screen cross-platform without bikeshedding the date
    // picker per the plan's Risks table. Forks can swap in a Material date picker later.
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { input ->
            runCatching { LocalDate.parse(input) }.getOrNull()?.let(onChange)
        },
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
    )
}

@Composable
private fun ComputedPreviewCard(monthlyEmi: Double, totalInterest: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = "Computed preview", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Monthly EMI", style = MaterialTheme.typography.bodyMedium)
                Text(formatMoney(monthlyEmi), style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Total interest", style = MaterialTheme.typography.bodyMedium)
                Text(formatMoney(totalInterest), style = MaterialTheme.typography.bodyMedium)
            }
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
            text = "Saving…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        is SubmitState.Submitted<*> -> Text(
            text = "Saved ✓",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        is SubmitState.Failed -> {
            val offline = submit.draftSaved
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (offline) {
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
