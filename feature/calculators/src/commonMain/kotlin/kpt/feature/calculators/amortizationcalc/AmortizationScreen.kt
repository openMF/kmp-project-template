/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.amortizationcalc

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.designsystem.component.HeroCard
import kpt.core.common.formatGrouped
import kpt.core.designsystem.component.AmountDisplay
import kpt.core.designsystem.theme.spacing
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.model.demo.banking.AmortizationRow
import kpt.feature.calculators.TestTags
import kpt.feature.calculators.generated.resources.Res
import kpt.feature.calculators.generated.resources.screens_calc_amortization_back_cd
import kpt.feature.calculators.generated.resources.screens_calc_amortization_emi_label
import kpt.feature.calculators.generated.resources.screens_calc_amortization_header_balance
import kpt.feature.calculators.generated.resources.screens_calc_amortization_header_interest
import kpt.feature.calculators.generated.resources.screens_calc_amortization_header_month
import kpt.feature.calculators.generated.resources.screens_calc_amortization_header_principal
import kpt.feature.calculators.generated.resources.screens_calc_amortization_interest_total_supporting
import kpt.feature.calculators.generated.resources.screens_calc_amortization_principal_label
import kpt.feature.calculators.generated.resources.screens_calc_amortization_rate_label
import kpt.feature.calculators.generated.resources.screens_calc_amortization_tenure_label
import kpt.feature.calculators.generated.resources.screens_calc_amortization_title
import kpt.feature.calculators.generated.resources.screens_calc_amortization_title_with_source
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmortizationScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    loanId: String? = null,
    viewModel: AmortizationViewModel = koinViewModel { parametersOf(loanId) },
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val breakdownState by viewModel.breakdownState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.testTag(TestTags.Amortization.SCREEN),
        topBar = {
            TopAppBar(
                title = {
                    val title = state.sourceLoanName?.let {
                        stringResource(Res.string.screens_calc_amortization_title_with_source, it)
                    } ?: stringResource(Res.string.screens_calc_amortization_title)
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_calc_amortization_back_cd),
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
                        label = { Text(stringResource(Res.string.screens_calc_amortization_principal_label)) },
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
                        label = { Text(stringResource(Res.string.screens_calc_amortization_rate_label)) },
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
                        label = { Text(stringResource(Res.string.screens_calc_amortization_tenure_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Store5 read surface: schedule + summary arrive as ONE ScreenState, rendered
            // through the same ScreenContent wrapper as every other read screen.
            ScreenContent(
                state = breakdownState,
                onRetry = viewModel::onRetry,
            ) { breakdown, _ ->
                Column(verticalArrangement = Arrangement.spacedBy(sp.md)) {
                    HeroCard {
                        AmountDisplay(
                            amountText = breakdown.summary.emi.formatGrouped(2),
                            label = stringResource(Res.string.screens_calc_amortization_emi_label),
                            supporting = {
                                Text(
                                    text = stringResource(
                                        Res.string.screens_calc_amortization_interest_total_supporting,
                                        breakdown.summary.totalInterest.formatGrouped(2),
                                        breakdown.summary.totalPayment.formatGrouped(2),
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                        )
                    }

                    AmortizationHeader()
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(breakdown.rows, key = { it.month }) { row ->
                            AmortizationRowItem(row)
                            HorizontalDivider()
                        }
                    }
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
        Text(
            stringResource(Res.string.screens_calc_amortization_header_month),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            stringResource(Res.string.screens_calc_amortization_header_principal),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            stringResource(Res.string.screens_calc_amortization_header_interest),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            stringResource(Res.string.screens_calc_amortization_header_balance),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun AmortizationRowItem(row: AmortizationRow) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(row.month.toString())
        Text(row.principal.formatGrouped(2))
        Text(row.interest.formatGrouped(2))
        Text(row.balance.coerceAtLeast(0.0).formatGrouped(2))
    }
}
