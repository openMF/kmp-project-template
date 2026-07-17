/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.affordability

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.designsystem.component.HeroCard
import kpt.core.common.formatGrouped
import kpt.core.designsystem.component.AmountDisplay
import kpt.core.designsystem.theme.spacing
import kpt.feature.calculators.TestTags
import kpt.feature.calculators.generated.resources.Res
import kpt.feature.calculators.generated.resources.screens_calc_affordability_back_cd
import kpt.feature.calculators.generated.resources.screens_calc_affordability_dti_label
import kpt.feature.calculators.generated.resources.screens_calc_affordability_income_label
import kpt.feature.calculators.generated.resources.screens_calc_affordability_max_emi_label
import kpt.feature.calculators.generated.resources.screens_calc_affordability_max_principal_label
import kpt.feature.calculators.generated.resources.screens_calc_affordability_obligations_label
import kpt.feature.calculators.generated.resources.screens_calc_affordability_rate_label
import kpt.feature.calculators.generated.resources.screens_calc_affordability_tenure_label
import kpt.feature.calculators.generated.resources.screens_calc_affordability_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
        modifier = modifier.testTag(TestTags.Affordability.SCREEN),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screens_calc_affordability_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_calc_affordability_back_cd),
                        )
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
                        label = { Text(stringResource(Res.string.screens_calc_affordability_income_label)) },
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
                        label = { Text(stringResource(Res.string.screens_calc_affordability_obligations_label)) },
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
                        label = { Text(stringResource(Res.string.screens_calc_affordability_dti_label)) },
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
                        label = { Text(stringResource(Res.string.screens_calc_affordability_rate_label)) },
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
                        label = { Text(stringResource(Res.string.screens_calc_affordability_tenure_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            HeroCard {
                AmountDisplay(
                    amountText = result.maxPrincipal.formatGrouped(2),
                    label = stringResource(Res.string.screens_calc_affordability_max_principal_label),
                    supporting = {
                        Column(verticalArrangement = Arrangement.spacedBy(sp.xs)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = stringResource(Res.string.screens_calc_affordability_max_emi_label),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
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
