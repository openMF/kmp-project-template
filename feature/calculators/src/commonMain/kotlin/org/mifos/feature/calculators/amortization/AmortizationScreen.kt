/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.calculators.amortization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.mifos.core.common.formatGrouped
import org.mifos.core.domain.calc.AmortizationRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmortizationScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    loanId: String? = null,
    viewModel: AmortizationViewModel = koinViewModel { parametersOf(loanId) },
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val schedule by viewModel.schedule.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val title = state.sourceLoanName?.let { "Amortization · $it" } ?: "Amortization"
                    Text(title)
                },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.principal.toLong().toString(),
                onValueChange = {
                    it.toDoubleOrNull()
                        ?.let { v -> viewModel.trySendAction(AmortizationAction.UpdatePrincipal(v)) }
                },
                label = { Text("Principal") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.ratePercent.toString(),
                onValueChange = {
                    it.toDoubleOrNull()
                        ?.let { v -> viewModel.trySendAction(AmortizationAction.UpdateRate(v)) }
                },
                label = { Text("Annual Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.tenureMonths.toString(),
                onValueChange = {
                    it.toIntOrNull()
                        ?.let { v -> viewModel.trySendAction(AmortizationAction.UpdateTenure(v)) }
                },
                label = { Text("Tenure (months)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("Summary", style = MaterialTheme.typography.titleSmall)
                    Text("Monthly EMI: ${summary.emi.formatGrouped(2)}")
                    Text("Total Interest: ${summary.totalInterest.formatGrouped(2)}")
                    Text("Total Payable: ${summary.totalPayment.formatGrouped(2)}")
                }
            }

            AmortizationHeader()
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(schedule, key = { it.installmentNumber }) { row ->
                    AmortizationRowItem(row)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun AmortizationHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("#", style = MaterialTheme.typography.labelMedium)
        Text("Principal", style = MaterialTheme.typography.labelMedium)
        Text("Interest", style = MaterialTheme.typography.labelMedium)
        Text("Balance", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun AmortizationRowItem(row: AmortizationRow) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(row.installmentNumber.toString())
        Text(row.principalPaid.formatGrouped(2))
        Text(row.interestPaid.formatGrouped(2))
        Text(row.balanceRemaining.coerceAtLeast(0.0).formatGrouped(2))
    }
}
