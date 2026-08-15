/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loans.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import kpt.core.base.designsystem.component.HeroCard
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.ui.draft.DraftResolutionPrompt
import kpt.core.base.ui.submit.MutationScreenContent
import kpt.core.designsystem.component.AmountDisplay
import kpt.core.model.demo.banking.LoanKind
import kpt.feature.loans.generated.resources.Res
import kpt.feature.loans.generated.resources.screens_loans_add_submit
import kpt.feature.loans.generated.resources.screens_loans_add_title
import kpt.feature.loans.generated.resources.screens_loans_addedit_annual_rate_label
import kpt.feature.loans.generated.resources.screens_loans_addedit_back_cd
import kpt.feature.loans.generated.resources.screens_loans_addedit_dismiss_button
import kpt.feature.loans.generated.resources.screens_loans_addedit_draft_resume_discard
import kpt.feature.loans.generated.resources.screens_loans_addedit_draft_resume_message
import kpt.feature.loans.generated.resources.screens_loans_addedit_draft_resume_resume
import kpt.feature.loans.generated.resources.screens_loans_addedit_draft_resume_start_fresh
import kpt.feature.loans.generated.resources.screens_loans_addedit_draft_resume_title
import kpt.feature.loans.generated.resources.screens_loans_addedit_kind_cd
import kpt.feature.loans.generated.resources.screens_loans_addedit_kind_label
import kpt.feature.loans.generated.resources.screens_loans_addedit_monthly_emi_label
import kpt.feature.loans.generated.resources.screens_loans_addedit_months_remaining_label
import kpt.feature.loans.generated.resources.screens_loans_addedit_name_label
import kpt.feature.loans.generated.resources.screens_loans_addedit_next_due_date_label
import kpt.feature.loans.generated.resources.screens_loans_addedit_principal_label
import kpt.feature.loans.generated.resources.screens_loans_addedit_principal_remaining_label
import kpt.feature.loans.generated.resources.screens_loans_addedit_retry_button
import kpt.feature.loans.generated.resources.screens_loans_addedit_status_failed
import kpt.feature.loans.generated.resources.screens_loans_addedit_status_failed_unknown
import kpt.feature.loans.generated.resources.screens_loans_addedit_status_offline
import kpt.feature.loans.generated.resources.screens_loans_addedit_status_saved
import kpt.feature.loans.generated.resources.screens_loans_addedit_status_saving
import kpt.feature.loans.generated.resources.screens_loans_addedit_tenure_label
import kpt.feature.loans.generated.resources.screens_loans_addedit_total_interest_preview
import kpt.feature.loans.generated.resources.screens_loans_addedit_total_paid_label
import kpt.feature.loans.generated.resources.screens_loans_edit_submit
import kpt.feature.loans.generated.resources.screens_loans_edit_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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

    // Three-case resume: on entry, if a saved draft exists for this loan, let the user choose to
    // resume it, discard it, or start fresh (the draft stays recoverable in Settings → Sync & Drafts).
    if (ui.hasResumableDraft) {
        DraftResolutionPrompt(
            onResume = viewModel::onResume,
            onDiscard = viewModel::onDiscardSavedDraft,
            onStartFresh = viewModel::onStartFresh,
            onDismiss = viewModel::onStartFresh,
            title = stringResource(Res.string.screens_loans_addedit_draft_resume_title),
            message = stringResource(Res.string.screens_loans_addedit_draft_resume_message),
            resumeLabel = stringResource(Res.string.screens_loans_addedit_draft_resume_resume),
            discardLabel = stringResource(Res.string.screens_loans_addedit_draft_resume_discard),
            startFreshLabel = stringResource(Res.string.screens_loans_addedit_draft_resume_start_fresh),
        )
    }

    Scaffold(
        modifier = modifier.testTag(TestTags.AddOrEditLoan.SCAFFOLD),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (loanId == null) {
                                Res.string.screens_loans_add_title
                            } else {
                                Res.string.screens_loans_edit_title
                            },
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_loans_addedit_back_cd),
                        )
                    }
                },
            )
        },
    ) { padding ->
        // Framework mutation-screen form: MutationScreenContent owns the read/load gate
        // (ScreenContent over ui.screen — Loading while an existing loan hydrates), the
        // Submitting scrim overlay, and the terminal result-handler (onSubmitted → navigate on
        // success). The persistent inline status line is supplied via the `submitStatus` slot so
        // the "Saved" / "Failed / retry / offline" affordances render in-place.
        MutationScreenContent(
            state = ui,
            onRetry = viewModel::onRetry,
            onSubmitted = { onSaved() },
            modifier = Modifier.padding(padding),
            submitStatus = { submitState ->
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    SubmitStatusLine(
                        submit = submitState,
                        onRetry = viewModel::onRetry,
                        onDismiss = viewModel::onDismissResult,
                    )
                }
            },
        ) { _, _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = form.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text(stringResource(Res.string.screens_loans_addedit_name_label)) },
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
                    label = stringResource(Res.string.screens_loans_addedit_principal_label),
                    value = form.principal,
                    onChange = viewModel::onPrincipalChange,
                    enabled = submit !is SubmitState.Submitting,
                )
                DoubleField(
                    label = stringResource(Res.string.screens_loans_addedit_principal_remaining_label),
                    value = form.principalRemaining,
                    onChange = viewModel::onPrincipalRemainingChange,
                    enabled = submit !is SubmitState.Submitting,
                )
                DoubleField(
                    label = stringResource(Res.string.screens_loans_addedit_annual_rate_label),
                    value = form.annualRatePercent,
                    onChange = viewModel::onAnnualRatePercentChange,
                    enabled = submit !is SubmitState.Submitting,
                )
                IntField(
                    label = stringResource(Res.string.screens_loans_addedit_tenure_label),
                    value = form.tenureMonths,
                    onChange = viewModel::onTenureMonthsChange,
                    enabled = submit !is SubmitState.Submitting,
                )
                IntField(
                    label = stringResource(Res.string.screens_loans_addedit_months_remaining_label),
                    value = form.monthsRemaining,
                    onChange = viewModel::onMonthsRemainingChange,
                    enabled = submit !is SubmitState.Submitting,
                )
                DateField(
                    label = stringResource(Res.string.screens_loans_addedit_next_due_date_label),
                    value = form.nextDueDate,
                    onChange = viewModel::onNextDueDateChange,
                    enabled = submit !is SubmitState.Submitting,
                )
                DoubleField(
                    label = stringResource(Res.string.screens_loans_addedit_total_paid_label),
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
                    Text(
                        stringResource(
                            if (loanId == null) {
                                Res.string.screens_loans_add_submit
                            } else {
                                Res.string.screens_loans_edit_submit
                            },
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun LoanKindDropdown(value: LoanKind, onChange: (LoanKind) -> Unit, enabled: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = loanKindLabel(value),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(stringResource(Res.string.screens_loans_addedit_kind_label)) },
            trailingIcon = {
                IconButton(onClick = { if (enabled) expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = stringResource(Res.string.screens_loans_addedit_kind_cd),
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
private fun DoubleField(label: String, value: Double, onChange: (Double) -> Unit, enabled: Boolean) {
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
private fun IntField(label: String, value: Int, onChange: (Int) -> Unit, enabled: Boolean) {
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
private fun DateField(label: String, value: LocalDate, onChange: (LocalDate) -> Unit, enabled: Boolean) {
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
    HeroCard {
        AmountDisplay(
            amountText = formatMoney(monthlyEmi),
            label = stringResource(Res.string.screens_loans_addedit_monthly_emi_label),
            supporting = {
                Text(
                    text = stringResource(
                        Res.string.screens_loans_addedit_total_interest_preview,
                        formatMoney(totalInterest),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
        )
    }
}

@Composable
private fun SubmitStatusLine(submit: SubmitState<*>, onRetry: () -> Unit, onDismiss: () -> Unit) {
    when (submit) {
        is SubmitState.Idle -> Unit
        is SubmitState.Submitting -> Text(
            text = stringResource(Res.string.screens_loans_addedit_status_saving),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        is SubmitState.Submitted<*> -> Text(
            text = stringResource(Res.string.screens_loans_addedit_status_saved),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        is SubmitState.Failed -> {
            val offline = submit.draftSaved
            val unknownError = stringResource(Res.string.screens_loans_addedit_status_failed_unknown)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (offline) {
                        stringResource(Res.string.screens_loans_addedit_status_offline)
                    } else {
                        stringResource(
                            Res.string.screens_loans_addedit_status_failed,
                            submit.error.message ?: unknownError,
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRetry) {
                        Text(stringResource(Res.string.screens_loans_addedit_retry_button))
                    }
                    Button(onClick = onDismiss) {
                        Text(stringResource(Res.string.screens_loans_addedit_dismiss_button))
                    }
                }
            }
        }
    }
}
