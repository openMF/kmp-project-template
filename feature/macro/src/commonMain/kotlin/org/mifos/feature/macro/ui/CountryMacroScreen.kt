/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.macro.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ExperimentalMaterial3Api
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
import org.koin.core.parameter.parametersOf
import org.mifos.core.data.economic.SupportedCountries
import org.mifos.core.designsystem.component.StatusChip
import org.mifos.core.designsystem.component.StatusChipIntent
import org.mifos.core.designsystem.theme.spacing
import org.mifos.core.model.economic.IndicatorKind
import org.mifos.feature.macro.ui.components.IndicatorCard
import template.core.base.store.screen.DataFreshness

/**
 * Country Macro Snapshot — the toolkit's offline-first multi-source-combine
 * showcase. Three indicator cards (GDP / Inflation / Unemployment) load
 * independently, each with its own retry. A country chip in the TopAppBar
 * routes to the picker.
 *
 * The Scaffold's TopAppBar surfaces:
 * - The current country's flag + name as an AssistChip → tap to open picker
 * - A refresh icon → triggers [MacroAction.RefreshAll]
 * - A STALE badge (when [MacroUiState.overallFreshness] is STALE) so users
 *   know they're looking at cached data without per-card inspection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryMacroScreen(
    countryCode: String,
    onBackClick: () -> Unit,
    onPickCountry: () -> Unit,
    onOpenIndicator: (IndicatorKind) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CountryMacroViewModel = koinViewModel { parametersOf(countryCode) },
) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val country = SupportedCountries.findByCode(uiState.countryCode)

    val sp = MaterialTheme.spacing
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Country Macro") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    StatusChip(
                        text = "${country?.flagEmoji.orEmpty()} ${country?.name ?: uiState.countryCode}",
                        intent = StatusChipIntent.Info,
                        modifier = Modifier
                            .padding(end = sp.sm)
                            .clickable(onClick = onPickCountry),
                    )
                    IconButton(onClick = { viewModel.trySendAction(MacroAction.RefreshAll) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh all")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = sp.sm)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(sp.sm),
        ) {
            if (uiState.overallFreshness == DataFreshness.STALE) {
                StatusChip(
                    text = "STALE — showing cached data",
                    intent = StatusChipIntent.Warning,
                    modifier = Modifier.padding(horizontal = sp.lg),
                )
            }
            IndicatorCard(
                indicatorKind = IndicatorKind.GDP,
                state = uiState.gdp,
                onRetry = { viewModel.trySendAction(MacroAction.RetryGdp) },
                onClick = { onOpenIndicator(IndicatorKind.GDP) },
            )
            IndicatorCard(
                indicatorKind = IndicatorKind.INFLATION_CPI,
                state = uiState.inflation,
                onRetry = { viewModel.trySendAction(MacroAction.RetryInflation) },
                onClick = { onOpenIndicator(IndicatorKind.INFLATION_CPI) },
            )
            IndicatorCard(
                indicatorKind = IndicatorKind.UNEMPLOYMENT,
                state = uiState.unemployment,
                onRetry = { viewModel.trySendAction(MacroAction.RetryUnemployment) },
                onClick = { onOpenIndicator(IndicatorKind.UNEMPLOYMENT) },
            )
        }
    }
}
