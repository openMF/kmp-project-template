/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.calculators.affordability

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.common.formatGrouped
import org.mifos.core.designsystem.component.AmountDisplay
import org.mifos.core.designsystem.component.HeroCard
import org.mifos.core.designsystem.theme.spacing
import template.core.base.designsystem.component.AppCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AffordabilityCalculatorScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AffordabilityCalculatorViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val result by viewModel.affordability.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Affordability") },
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
            AppCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(sp.md),
                    verticalArrangement = Arrangement.spacedBy(sp.md),
                ) {
                    OutlinedTextField(
                        value = state.monthlyIncome.toLong().toString(),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { v ->
                                viewModel.trySendAction(AffordabilityAction.UpdateIncome(v))
                            }
                        },
                        label = { Text("Monthly Income") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.monthlyObligations.toLong().toString(),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { v ->
                                viewModel.trySendAction(AffordabilityAction.UpdateObligations(v))
                            }
                        },
                        label = { Text("Existing Monthly Debt") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = ((state.dtiRatio * 100).toInt()).toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { v ->
                                viewModel.trySendAction(AffordabilityAction.UpdateDti(v / 100.0))
                            }
                        },
                        label = { Text("DTI Ceiling (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.ratePercent.toString(),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { v ->
                                viewModel.trySendAction(AffordabilityAction.UpdateRate(v))
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
                                viewModel.trySendAction(AffordabilityAction.UpdateTenure(v))
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
                    amountText = result.maxPrincipal.formatGrouped(2),
                    label = "Max loan principal",
                    supporting = {
                        Column(verticalArrangement = Arrangement.spacedBy(sp.xs)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("Max EMI", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    result.maxEmi.formatGrouped(2),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            Text(
                                text = result.rationale,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
            }
        }
    }
}
