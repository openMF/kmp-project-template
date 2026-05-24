/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.calculators.comparison

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.common.formatGrouped
import org.mifos.core.model.emi.EmiResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanComparisonScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanComparisonViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val analysis by viewModel.analysis.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Compare Loans") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Side-by-side editable input columns.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.scenarios.forEachIndexed { idx, scenario ->
                    ScenarioInputColumn(
                        index = idx,
                        scenario = scenario,
                        onChange = { updated ->
                            viewModel.trySendAction(
                                LoanComparisonAction.UpdateScenario(idx, updated),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Text("Side-by-side analysis", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                analysis.results.forEachIndexed { idx, result ->
                    ScenarioResultCard(
                        title = "Scenario ${idx + 1}",
                        result = result,
                        isCheapest = idx == analysis.cheapestIndex,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScenarioInputColumn(
    index: Int,
    scenario: LoanScenario,
    onChange: (LoanScenario) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "Scenario ${index + 1}",
            style = MaterialTheme.typography.titleSmall,
        )
        OutlinedTextField(
            value = scenario.principal.toLong().toString(),
            onValueChange = {
                it.toDoubleOrNull()?.let { v -> onChange(scenario.copy(principal = v)) }
            },
            label = { Text("Principal") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = scenario.ratePercent.toString(),
            onValueChange = {
                it.toDoubleOrNull()?.let { v -> onChange(scenario.copy(ratePercent = v)) }
            },
            label = { Text("Rate (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = scenario.tenureMonths.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { v -> onChange(scenario.copy(tenureMonths = v)) }
            },
            label = { Text("Months") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ScenarioResultCard(
    title: String,
    result: EmiResult,
    isCheapest: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = if (isCheapest) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            )
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                title + if (isCheapest) " · best" else "",
                style = MaterialTheme.typography.titleSmall,
            )
            Text("EMI ${result.emi.formatGrouped(2)}")
            Text("Interest ${result.totalInterest.formatGrouped(2)}")
            Text("Total ${result.totalPayment.formatGrouped(2)}")
        }
    }
}
