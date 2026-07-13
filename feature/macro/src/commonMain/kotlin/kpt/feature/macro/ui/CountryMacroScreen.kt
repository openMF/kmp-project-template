/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.macro.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import kpt.core.base.store.freshness.FreshnessBand
import kpt.core.data.demo.economic.SupportedCountries
import kpt.core.designsystem.component.StatusChip
import kpt.core.designsystem.component.StatusChipIntent
import kpt.core.designsystem.theme.spacing
import kpt.core.model.demo.economic.IndicatorKind
import kpt.feature.macro.generated.resources.Res
import kpt.feature.macro.generated.resources.screens_macro_back_cd
import kpt.feature.macro.generated.resources.screens_macro_country_title
import kpt.feature.macro.generated.resources.screens_macro_refresh_all_cd
import kpt.feature.macro.generated.resources.screens_macro_stale_chip
import kpt.feature.macro.ui.components.IndicatorCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Country Macro Snapshot — the toolkit's offline-first multi-source-combine
 * showcase. Three indicator cards (GDP / Inflation / Unemployment) load
 * independently, each with its own retry. A country chip in the TopAppBar
 * routes to the picker.
 *
 * The Scaffold's TopAppBar surfaces:
 * - The current country's flag + name as an AssistChip → tap to open picker
 * - A refresh icon → triggers [MacroAction.RefreshAll]
 * - A STALE badge (when [MacroUiState.overallBand] is Stale/VeryStale) so users
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
                title = { Text(stringResource(Res.string.screens_macro_country_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_macro_back_cd),
                        )
                    }
                },
                actions = {
                    // Country chip — drops the flag emoji prefix because regional-
                    // indicator code points (U+1F1E6..U+1F1FF) are not in the
                    // project's Outfit font and render as tofu on wasmJs. The
                    // chip's affordance (clickable, accent-tinted) already signals
                    // "tap to change country"; the country name alone is sufficient.
                    StatusChip(
                        text = country?.name ?: uiState.countryCode,
                        intent = StatusChipIntent.Info,
                        modifier = Modifier
                            .padding(end = sp.sm)
                            .clickable(onClick = onPickCountry),
                    )
                    IconButton(onClick = { viewModel.trySendAction(MacroAction.RefreshAll) }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(Res.string.screens_macro_refresh_all_cd),
                        )
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
            if (uiState.overallBand == FreshnessBand.Stale || uiState.overallBand == FreshnessBand.VeryStale) {
                StatusChip(
                    text = stringResource(Res.string.screens_macro_stale_chip),
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
