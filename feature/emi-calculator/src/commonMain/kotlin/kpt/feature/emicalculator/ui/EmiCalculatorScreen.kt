/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.emicalculator.ui

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
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.designsystem.component.HeroCard
import kpt.core.common.formatGrouped
import kpt.core.designsystem.component.AmountDisplay
import kpt.core.designsystem.theme.spacing
import kpt.feature.emicalculator.generated.resources.Res
import kpt.feature.emicalculator.generated.resources.screens_emicalculator_back_cd
import kpt.feature.emicalculator.generated.resources.screens_emicalculator_monthly_emi_label
import kpt.feature.emicalculator.generated.resources.screens_emicalculator_principal_label
import kpt.feature.emicalculator.generated.resources.screens_emicalculator_rate_label
import kpt.feature.emicalculator.generated.resources.screens_emicalculator_tenure_label
import kpt.feature.emicalculator.generated.resources.screens_emicalculator_title
import kpt.feature.emicalculator.generated.resources.screens_emicalculator_total_interest_label
import kpt.feature.emicalculator.generated.resources.screens_emicalculator_total_payment_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiCalculatorScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmiCalculatorViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val emiState by viewModel.emiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.testTag(TestTags.EmiCalculator.SCREEN),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screens_emicalculator_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_emicalculator_back_cd),
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
                        value = state.principal.toLong().toString(),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { v ->
                                viewModel.trySendAction(EmiAction.UpdatePrincipal(v))
                            }
                        },
                        label = { Text(stringResource(Res.string.screens_emicalculator_principal_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = state.ratePercent.toString(),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { v ->
                                viewModel.trySendAction(EmiAction.UpdateRate(v))
                            }
                        },
                        label = { Text(stringResource(Res.string.screens_emicalculator_rate_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = state.tenureMonths.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { v ->
                                viewModel.trySendAction(EmiAction.UpdateTenure(v))
                            }
                        },
                        label = { Text(stringResource(Res.string.screens_emicalculator_tenure_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Store5 read surface: the calculator renders through the SAME ScreenContent
            // wrapper as every other read screen — Loading / Content / Empty (inputs not
            // yet complete) / Error are the store's states, not hand-rolled null checks.
            ScreenContent(
                state = emiState,
                onRetry = viewModel::onRetry,
            ) { result, _ ->
                HeroCard {
                    AmountDisplay(
                        amountText = result.emi.formatGrouped(2),
                        label = stringResource(Res.string.screens_emicalculator_monthly_emi_label),
                        supporting = {
                            Column(verticalArrangement = Arrangement.spacedBy(sp.xs)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        stringResource(Res.string.screens_emicalculator_total_payment_label),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        result.totalPayment.formatGrouped(2),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        stringResource(Res.string.screens_emicalculator_total_interest_label),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        result.totalInterest.formatGrouped(2),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
