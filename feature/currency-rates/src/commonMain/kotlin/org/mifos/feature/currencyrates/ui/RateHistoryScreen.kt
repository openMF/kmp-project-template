/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.currencyrates.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.common.formatDecimal
import org.mifos.core.designsystem.theme.spacing
import template.core.base.designsystem.component.AppCard
import template.core.base.ui.screen.ScreenContent

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RateHistoryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RateHistoryViewModel = koinViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val localState by viewModel.stateFlow.collectAsStateWithLifecycle()

    val currencies = listOf("INR", "EUR", "GBP", "JPY", "AUD")
    val periods = listOf(7, 14, 30, 90)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Rate History") },
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
            onRetry = viewModel::onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { history, _ ->
            val sp = MaterialTheme.spacing
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = sp.lg),
                verticalArrangement = Arrangement.spacedBy(sp.sm),
            ) {
                AppCard(modifier = Modifier.padding(top = sp.sm)) {
                    Column(
                        modifier = Modifier.padding(sp.md),
                        verticalArrangement = Arrangement.spacedBy(sp.sm),
                    ) {
                        Text("Currency", style = MaterialTheme.typography.labelMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(sp.sm)) {
                            currencies.forEach { code ->
                                FilterChip(
                                    selected = localState.targetCurrency == code,
                                    onClick = {
                                        viewModel.trySendAction(HistoryAction.SelectCurrency(code))
                                    },
                                    label = { Text(code) },
                                )
                            }
                        }
                        Text("Period", style = MaterialTheme.typography.labelMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(sp.sm)) {
                            periods.forEach { days ->
                                FilterChip(
                                    selected = localState.periodDays == days,
                                    onClick = {
                                        viewModel.trySendAction(HistoryAction.SelectPeriod(days))
                                    },
                                    label = { Text("${days}d") },
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "USD \u2192 ${history.to} (${history.startDate} to ${history.endDate})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(history.rates) { point ->
                        Text(
                            text = "${point.date}  \u2192  ${point.value.formatDecimal(4)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = sp.sm),
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
