/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.amortization

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
import kpt.core.common.formatGrouped
import kpt.core.designsystem.component.AmountDisplay
import kpt.core.designsystem.theme.spacing
import kpt.core.domain.calc.AmortizationRow
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.designsystem.component.HeroCard

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
        val sp = MaterialTheme.spacing
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = sp.lg),
            verticalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            AppCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(sp.md),
                    verticalArrangement = Arrangement.spacedBy(sp.md),
                ) {
                    OutlinedTextField(
                        value = state.principal.toLong().toString(),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { v ->
                                viewModel.trySendAction(AmortizationAction.UpdatePrincipal(v))
                            }
                        },
                        label = { Text("Principal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.ratePercent.toString(),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { v ->
                                viewModel.trySendAction(AmortizationAction.UpdateRate(v))
                            }
                        },
                        label = { Text("Annual Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.tenureMonths.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { v ->
                                viewModel.trySendAction(AmortizationAction.UpdateTenure(v))
                            }
                        },
                        label = { Text("Tenure (months)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            HeroCard {
                AmountDisplay(
                    amountText = summary.emi.formatGrouped(2),
                    label = "Monthly EMI",
                    supporting = {
                        Text(
                            text = "Interest ${summary.totalInterest.formatGrouped(2)} · " +
                                "Total ${summary.totalPayment.formatGrouped(2)}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                )
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
