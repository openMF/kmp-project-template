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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.mifos.core.model.banking.Loan
import template.core.base.ui.screen.ScreenContent

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
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Loan Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            title = { Text("Delete loan?") },
            text = { Text("This loan and its tracking history will be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDelete()
                    showDeleteDialog = false
                    onBackClick()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = loan.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            AssistChip(
                onClick = {},
                label = { Text(loanKindLabel(loan.kind)) },
                colors = AssistChipDefaults.assistChipColors(),
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetricRow(label = "Principal remaining", value = formatMoney(loan.principalRemaining))
                MetricRow(label = "Original principal", value = formatMoney(loan.principal))
                MetricRow(label = "Monthly payment (EMI)", value = formatMoney(loan.monthlyPayment))
                MetricRow(label = "Annual rate", value = "${loan.annualRatePercent}%")
                MetricRow(label = "Next due date", value = loan.nextDueDate.toString())
                MetricRow(label = "Tenure", value = "${loan.tenureMonths} months")
                MetricRow(label = "Months remaining", value = "${loan.monthsRemaining}")
                MetricRow(label = "Total paid to date", value = formatMoney(loan.totalPaid))

                val progress = tenureProgress(loan)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Tenure progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% complete",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = onEditClick, modifier = Modifier.weight(1f)) {
                Text("Edit")
            }
            OutlinedButton(onClick = onAmortizationClick, modifier = Modifier.weight(1f)) {
                // TODO(banking-utility-toolkit-06): wire to Amortization Schedule screen once
                //  sub-plan 06 ships. This button stays present as a UX affordance even
                //  before the destination exists.
                Text("Schedule")
            }
            OutlinedButton(onClick = onDeleteClick, modifier = Modifier.weight(1f)) {
                Text("Delete")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
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
