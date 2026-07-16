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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.designsystem.component.HeroCard
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.component.AmountDisplay
import kpt.core.designsystem.component.StatusChip
import kpt.core.designsystem.component.StatusChipIntent
import kpt.core.designsystem.theme.spacing
import kpt.core.model.demo.banking.Loan
import kpt.feature.loans.generated.resources.Res
import kpt.feature.loans.generated.resources.screens_loans_detail_annual_rate_label
import kpt.feature.loans.generated.resources.screens_loans_detail_annual_rate_value
import kpt.feature.loans.generated.resources.screens_loans_detail_back_cd
import kpt.feature.loans.generated.resources.screens_loans_detail_delete_button
import kpt.feature.loans.generated.resources.screens_loans_detail_delete_cancel
import kpt.feature.loans.generated.resources.screens_loans_detail_delete_confirm
import kpt.feature.loans.generated.resources.screens_loans_detail_delete_dialog_message
import kpt.feature.loans.generated.resources.screens_loans_detail_delete_dialog_title
import kpt.feature.loans.generated.resources.screens_loans_detail_edit_button
import kpt.feature.loans.generated.resources.screens_loans_detail_monthly_payment_label
import kpt.feature.loans.generated.resources.screens_loans_detail_months_remaining_value
import kpt.feature.loans.generated.resources.screens_loans_detail_next_due_label
import kpt.feature.loans.generated.resources.screens_loans_detail_original_principal_label
import kpt.feature.loans.generated.resources.screens_loans_detail_principal_remaining_label
import kpt.feature.loans.generated.resources.screens_loans_detail_schedule_button
import kpt.feature.loans.generated.resources.screens_loans_detail_tenure_label
import kpt.feature.loans.generated.resources.screens_loans_detail_tenure_progress_label
import kpt.feature.loans.generated.resources.screens_loans_detail_tenure_progress_value
import kpt.feature.loans.generated.resources.screens_loans_detail_tenure_value
import kpt.feature.loans.generated.resources.screens_loans_detail_title
import kpt.feature.loans.generated.resources.screens_loans_detail_total_paid_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    loanId: String,
    onBackClick: () -> Unit,
    onEditClick: (loanId: String) -> Unit,
    onAmortizationClick: (loanId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanDetailViewModel = koinViewModel(key = loanId) { parametersOf(loanId) },
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag(TestTags.LoanDetail.SCAFFOLD),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screens_loans_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_loans_detail_back_cd),
                        )
                    }
                },
            )
        },
    ) { padding ->
        ScreenContent(
            state = screenState,
            onRetry = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { loan, _ ->
            LoanDetailContent(
                loan = loan,
                onEditClick = { onEditClick(loan.id) },
                onAmortizationClick = { onAmortizationClick(loan.id) },
                onDeleteClick = { showDeleteDialog = true },
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(Res.string.screens_loans_detail_delete_dialog_title)) },
            text = { Text(stringResource(Res.string.screens_loans_detail_delete_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDelete()
                    showDeleteDialog = false
                    onBackClick()
                }) { Text(stringResource(Res.string.screens_loans_detail_delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(Res.string.screens_loans_detail_delete_cancel))
                }
            },
        )
    }
}

@Composable
private fun LoanDetailContent(
    loan: Loan,
    onEditClick: () -> Unit,
    onAmortizationClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val sp = MaterialTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(sp.lg),
        verticalArrangement = Arrangement.spacedBy(sp.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            Text(
                text = loan.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            StatusChip(
                text = loanKindLabel(loan.kind),
                intent = StatusChipIntent.Info,
            )
        }

        HeroCard {
            AmountDisplay(
                amountText = formatMoney(loan.principalRemaining),
                label = stringResource(Res.string.screens_loans_detail_principal_remaining_label),
                supporting = {
                    Text(
                        text = stringResource(
                            Res.string.screens_loans_detail_months_remaining_value,
                            loan.monthsRemaining,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
            )
        }

        AppCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(sp.md),
                verticalArrangement = Arrangement.spacedBy(sp.md),
            ) {
                MetricRow(
                    label = stringResource(Res.string.screens_loans_detail_original_principal_label),
                    value = formatMoney(loan.principal),
                )
                MetricRow(
                    label = stringResource(Res.string.screens_loans_detail_monthly_payment_label),
                    value = formatMoney(loan.monthlyPayment),
                )
                MetricRow(
                    label = stringResource(Res.string.screens_loans_detail_total_paid_label),
                    value = formatMoney(loan.totalPaid),
                )
                MetricRow(
                    label = stringResource(Res.string.screens_loans_detail_annual_rate_label),
                    value = stringResource(
                        Res.string.screens_loans_detail_annual_rate_value,
                        loan.annualRatePercent.toString(),
                    ),
                )
                MetricRow(
                    label = stringResource(Res.string.screens_loans_detail_next_due_label),
                    value = loan.nextDueDate.toString(),
                )
                MetricRow(
                    label = stringResource(Res.string.screens_loans_detail_tenure_label),
                    value = stringResource(
                        Res.string.screens_loans_detail_tenure_value,
                        loan.tenureMonths,
                    ),
                )

                Spacer(modifier = Modifier.height(sp.xs))

                val progress = tenureProgress(loan)
                Column(verticalArrangement = Arrangement.spacedBy(sp.xs)) {
                    Text(
                        text = stringResource(Res.string.screens_loans_detail_tenure_progress_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(
                            Res.string.screens_loans_detail_tenure_progress_value,
                            (progress * 100).toInt(),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(sp.sm),
        ) {
            Button(onClick = onEditClick, modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.screens_loans_detail_edit_button))
            }
            OutlinedButton(onClick = onAmortizationClick, modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.screens_loans_detail_schedule_button))
            }
            OutlinedButton(onClick = onDeleteClick, modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.screens_loans_detail_delete_button))
            }
        }
        Spacer(modifier = Modifier.height(sp.sm))
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun tenureProgress(loan: Loan): Float {
    if (loan.tenureMonths <= 0) return 0f
    val paid = (loan.tenureMonths - loan.monthsRemaining).coerceIn(0, loan.tenureMonths)
    return paid.toFloat() / loan.tenureMonths.toFloat()
}
