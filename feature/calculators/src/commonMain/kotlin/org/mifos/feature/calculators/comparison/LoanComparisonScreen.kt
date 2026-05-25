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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.common.formatGrouped
import org.mifos.core.designsystem.component.AppCard
import org.mifos.core.designsystem.component.StatusChip
import org.mifos.core.designsystem.component.StatusChipIntent
import org.mifos.core.designsystem.theme.spacing
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
        val sp = MaterialTheme.spacing
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(sp.sm),
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

            Text(
                "Side-by-side analysis",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(sp.sm),
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
    val sp = MaterialTheme.spacing
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(sp.sm),
            verticalArrangement = Arrangement.spacedBy(sp.sm),
        ) {
            Text(
                "Scenario ${index + 1}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
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
}

@Composable
private fun ScenarioResultCard(
    title: String,
    result: EmiResult,
    isCheapest: Boolean,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.spacing
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(sp.sm),
            verticalArrangement = Arrangement.spacedBy(sp.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(sp.xs),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f),
                )
                if (isCheapest) {
                    StatusChip(text = "Best", intent = StatusChipIntent.Success)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("EMI", style = MaterialTheme.typography.bodySmall)
                Text(result.emi.formatGrouped(2), style = MaterialTheme.typography.bodySmall)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Interest", style = MaterialTheme.typography.bodySmall)
                Text(result.totalInterest.formatGrouped(2), style = MaterialTheme.typography.bodySmall)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Total", style = MaterialTheme.typography.bodySmall)
                Text(result.totalPayment.formatGrouped(2), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
