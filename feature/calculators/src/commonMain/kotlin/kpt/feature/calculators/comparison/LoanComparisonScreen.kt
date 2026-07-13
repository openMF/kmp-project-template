/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.comparison

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.designsystem.component.HeroCard
import kpt.core.common.formatGrouped
import kpt.core.designsystem.component.AmountDisplay
import kpt.core.designsystem.component.StatusChip
import kpt.core.designsystem.component.StatusChipIntent
import kpt.core.designsystem.theme.spacing
import kpt.core.model.demo.emi.EmiResult
import kpt.feature.calculators.generated.resources.Res
import kpt.feature.calculators.generated.resources.screens_calc_compare_back_cd
import kpt.feature.calculators.generated.resources.screens_calc_compare_best_badge
import kpt.feature.calculators.generated.resources.screens_calc_compare_best_emi_label
import kpt.feature.calculators.generated.resources.screens_calc_compare_best_emi_value
import kpt.feature.calculators.generated.resources.screens_calc_compare_helper_text
import kpt.feature.calculators.generated.resources.screens_calc_compare_principal_label
import kpt.feature.calculators.generated.resources.screens_calc_compare_rate_label
import kpt.feature.calculators.generated.resources.screens_calc_compare_result_emi
import kpt.feature.calculators.generated.resources.screens_calc_compare_result_interest
import kpt.feature.calculators.generated.resources.screens_calc_compare_result_total
import kpt.feature.calculators.generated.resources.screens_calc_compare_savings_text
import kpt.feature.calculators.generated.resources.screens_calc_compare_scenario_label
import kpt.feature.calculators.generated.resources.screens_calc_compare_tenure_label
import kpt.feature.calculators.generated.resources.screens_calc_compare_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Compare Loans — full-width vertical-stack redesign.
 *
 * Original (broken) layout was 3 side-by-side columns squeezed into a phone width: input
 * fields wrapped letter-by-letter, the "Best" badge collapsed the entire scenario card,
 * and the results table became unreadable. New layout:
 *
 *  1. HeroCard summary at top — cheapest EMI as the headline, with delta over the most
 *     expensive scenario so users immediately see the value of comparing.
 *  2. Each scenario gets its own full-width card with: header (Scenario N + Best badge) +
 *     inputs in a single row (3 short fields fit cleanly at full width) + result row at
 *     the bottom (EMI / Interest / Total).
 *
 * Tested at 360dp phone width — no truncation, no wrap-by-character.
 */
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
                title = {
                    Text(
                        text = stringResource(Res.string.screens_calc_compare_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_calc_compare_back_cd),
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
            ComparisonHero(analysis = analysis)

            state.scenarios.forEachIndexed { idx, scenario ->
                val result = analysis.results.getOrNull(idx)
                ScenarioCard(
                    index = idx,
                    scenario = scenario,
                    result = result,
                    isCheapest = idx == analysis.cheapestIndex && analysis.results.size > 1,
                    onChange = { updated ->
                        viewModel.trySendAction(
                            LoanComparisonAction.UpdateScenario(idx, updated),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun ComparisonHero(analysis: LoanComparisonAnalysis) {
    val sp = MaterialTheme.spacing
    val cheapest = analysis.results.getOrNull(analysis.cheapestIndex)
    val mostExpensive = analysis.results.maxByOrNull { it.totalPayment }
    val savings = if (cheapest != null && mostExpensive != null) {
        mostExpensive.totalPayment - cheapest.totalPayment
    } else {
        0.0
    }
    HeroCard {
        AmountDisplay(
            amountText = cheapest?.emi?.formatGrouped(2)?.let {
                stringResource(Res.string.screens_calc_compare_best_emi_value, it)
            } ?: "—",
            label = stringResource(
                Res.string.screens_calc_compare_best_emi_label,
                analysis.cheapestIndex + 1,
            ),
            supporting = {
                Column(verticalArrangement = Arrangement.spacedBy(sp.xs)) {
                    if (savings > 0) {
                        Text(
                            text = stringResource(
                                Res.string.screens_calc_compare_savings_text,
                                savings.formatGrouped(2),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Text(
                        text = stringResource(Res.string.screens_calc_compare_helper_text),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }
}

@Composable
private fun ScenarioCard(
    index: Int,
    scenario: LoanScenario,
    result: EmiResult?,
    isCheapest: Boolean,
    onChange: (LoanScenario) -> Unit,
) {
    val sp = MaterialTheme.spacing
    AppCard(
        accentColor = if (isCheapest) MaterialTheme.colorScheme.secondary else null,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(sp.md)) {
            // Header row — scenario label + Best badge.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(sp.sm),
            ) {
                Text(
                    text = stringResource(Res.string.screens_calc_compare_scenario_label, index + 1),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    modifier = Modifier.weight(1f),
                )
                if (isCheapest) {
                    StatusChip(
                        text = stringResource(Res.string.screens_calc_compare_best_badge),
                        intent = StatusChipIntent.Success,
                    )
                }
            }

            // Inputs — full width each. Fits cleanly on every phone.
            OutlinedTextField(
                value = scenario.principal.toLong().toString(),
                onValueChange = {
                    it.toDoubleOrNull()?.let { v -> onChange(scenario.copy(principal = v)) }
                },
                label = { Text(stringResource(Res.string.screens_calc_compare_principal_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Rate + Months side-by-side — both short numerics, fit comfortably.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(sp.sm),
            ) {
                OutlinedTextField(
                    value = scenario.ratePercent.toString(),
                    onValueChange = {
                        it.toDoubleOrNull()?.let { v ->
                            onChange(scenario.copy(ratePercent = v))
                        }
                    },
                    label = { Text(stringResource(Res.string.screens_calc_compare_rate_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = scenario.tenureMonths.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { v -> onChange(scenario.copy(tenureMonths = v)) }
                    },
                    label = { Text(stringResource(Res.string.screens_calc_compare_tenure_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            // Result strip — EMI / Interest / Total at a glance.
            if (result != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    ResultStrip(result = result, highlight = isCheapest)
                }
            }
        }
    }
}

@Composable
private fun ResultStrip(result: EmiResult, highlight: Boolean) {
    val sp = MaterialTheme.spacing
    val container = if (highlight) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val onContainer = if (highlight) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    androidx.compose.material3.Surface(
        color = container,
        contentColor = onContainer,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = sp.md, vertical = sp.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ResultCell(
                label = stringResource(Res.string.screens_calc_compare_result_emi),
                value = result.emi.formatGrouped(2),
            )
            ResultCell(
                label = stringResource(Res.string.screens_calc_compare_result_interest),
                value = result.totalInterest.formatGrouped(2),
            )
            ResultCell(
                label = stringResource(Res.string.screens_calc_compare_result_total),
                value = result.totalPayment.formatGrouped(2),
            )
        }
    }
}

@Composable
private fun ResultCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}
