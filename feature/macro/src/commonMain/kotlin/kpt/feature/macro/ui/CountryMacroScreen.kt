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
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.store.freshness.FreshnessBand
import kpt.core.base.ui.dashboard.DashboardProgressBar
import kpt.core.base.ui.dashboard.IndependentCardLayout
import kpt.core.base.ui.dashboard.toDashboardProgressState
import kpt.core.data.demo.economic.SupportedCountries
import kpt.core.designsystem.component.StatusChip
import kpt.core.designsystem.component.StatusChipIntent
import kpt.core.designsystem.theme.spacing
import kpt.core.model.demo.economic.IndicatorKind
import kpt.feature.macro.generated.resources.Res
import kpt.feature.macro.generated.resources.screens_macro_back_cd
import kpt.feature.macro.generated.resources.screens_macro_card_captive_portal
import kpt.feature.macro.generated.resources.screens_macro_card_empty
import kpt.feature.macro.generated.resources.screens_macro_card_generic_error
import kpt.feature.macro.generated.resources.screens_macro_card_offline
import kpt.feature.macro.generated.resources.screens_macro_country_title
import kpt.feature.macro.generated.resources.screens_macro_refresh_all_cd
import kpt.feature.macro.generated.resources.screens_macro_stale_chip
import kpt.feature.macro.ui.components.MacroContentBody
import kpt.feature.macro.ui.components.MacroIndicatorCardChrome
import kpt.feature.macro.ui.components.MacroInlineMessage
import kpt.feature.macro.ui.components.MacroLoadingBody
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
        modifier = modifier.testTag(TestTags.CountryMacro.SCREEN),
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

            // Store5 multi-source dashboard, framework-composable form: the three indicators are
            // homogeneous ScreenState<MacroIndicator> cards. IndependentCardLayout owns each card's
            // loading/empty/no-network/error/content dispatch (per-card, so one slow/failed indicator
            // never blanks the others); MacroIndicatorCardChrome supplies the persistent card frame;
            // the compact per-card slots keep the dense look. DashboardProgressBar shows "X of Y loaded".
            val macroStates = listOf(uiState.gdp, uiState.inflation, uiState.unemployment)
            val macroKinds = listOf(IndicatorKind.GDP, IndicatorKind.INFLATION_CPI, IndicatorKind.UNEMPLOYMENT)
            val macroRetries = listOf(MacroAction.RetryGdp, MacroAction.RetryInflation, MacroAction.RetryUnemployment)

            DashboardProgressBar(
                state = macroStates.toDashboardProgressState(),
                modifier = Modifier.padding(horizontal = sp.lg),
            )

            IndependentCardLayout(
                states = macroStates,
                onRetry = { index -> viewModel.trySendAction(macroRetries[index]) },
                cardChrome = { index, card ->
                    MacroIndicatorCardChrome(
                        indicatorKind = macroKinds[index],
                        onClick = { onOpenIndicator(macroKinds[index]) },
                    ) { card() }
                },
                loading = { MacroLoadingBody() },
                empty = { index ->
                    MacroInlineMessage(
                        text = stringResource(Res.string.screens_macro_card_empty),
                        onRetry = { viewModel.trySendAction(macroRetries[index]) },
                    )
                },
                noNetwork = { index, isCaptivePortal ->
                    MacroInlineMessage(
                        text = if (isCaptivePortal) {
                            stringResource(Res.string.screens_macro_card_captive_portal)
                        } else {
                            stringResource(Res.string.screens_macro_card_offline)
                        },
                        onRetry = { viewModel.trySendAction(macroRetries[index]) },
                    )
                },
                error = { index, throwable ->
                    MacroInlineMessage(
                        text = throwable.message ?: stringResource(Res.string.screens_macro_card_generic_error),
                        onRetry = { viewModel.trySendAction(macroRetries[index]) },
                    )
                },
            ) { _, data, _ ->
                MacroContentBody(data)
            }
        }
    }
}
