/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.currencyrates.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.freshness.FreshnessIndicator
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.formatDecimal
import kpt.core.designsystem.theme.spacing
import kpt.core.model.demo.currency.ExchangeRates
import kpt.feature.currencyrates.generated.resources.Res
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_converter_amount_label
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_converter_result
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_converter_target_label
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_converter_title
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_converter_unavailable
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_list_back_cd
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_list_base_label
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_list_search_placeholder
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_list_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyRatesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CurrencyRatesViewModel = koinViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val localState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val freshness by viewModel.freshness.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.testTag(TestTags.CurrencyRates.ROOT),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(Res.string.screens_currencyrates_list_title))
                        FreshnessIndicator(
                            signal = freshness,
                            onRefresh = viewModel::onRetry,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_currencyrates_list_back_cd),
                        )
                    }
                },
            )
        },
    ) { padding ->
        val spotState by viewModel.spotConversionRate.collectAsStateWithLifecycle()
        val sp = MaterialTheme.spacing
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Archetype showcase surface: the NETWORK_ONLY (online) / CACHE_ONLY
            // (offline) spot-rate stream is now rendered by a real converter input,
            // turning the previously UI-dead `spotConversionRate` into a reachable
            // feature.
            CurrencyConverterCard(
                amount = localState.converterAmount,
                targetCode = localState.converterTarget,
                spotState = spotState,
                onAmountChange = { viewModel.trySendAction(RatesAction.ConverterAmount(it)) },
                onTargetChange = { viewModel.trySendAction(RatesAction.ConverterTarget(it)) },
                onRetry = viewModel::onRetry,
                modifier = Modifier
                    .padding(horizontal = sp.lg, vertical = sp.sm)
                    .testTag(TestTags.CurrencyRates.CONVERTER),
            )
            ScreenContent(
                state = screenState,
                onRetry = viewModel::onRetry,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            ) { data, _ ->
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.padding(horizontal = sp.lg, vertical = sp.sm),
                        verticalArrangement = Arrangement.spacedBy(sp.xs),
                    ) {
                        AppCard {
                            OutlinedTextField(
                                value = localState.searchQuery,
                                onValueChange = { viewModel.trySendAction(RatesAction.Search(it)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(sp.sm),
                                placeholder = {
                                    Text(stringResource(Res.string.screens_currencyrates_list_search_placeholder))
                                },
                                singleLine = true,
                            )
                        }
                        Text(
                            text = stringResource(
                                Res.string.screens_currencyrates_list_base_label,
                                data.base,
                                data.date.toString(),
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = sp.xs),
                        )
                    }
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(data.rates.entries.toList()) { (code, rate) ->
                            RateItem(code = code, rate = rate)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

/**
 * **Archetype showcase: NETWORK_ONLY + CACHE_ONLY.**
 *
 * A minimal live converter: the user enters an [amount] and a [targetCode], and
 * the result is derived from [spotState] — the connectivity-routed spot-rate
 * stream (`FetchPolicy.NETWORK_ONLY` online / `FetchPolicy.CACHE_ONLY` offline).
 * The spot rate is rendered through the framework's `ScreenContent` so the
 * loading / error / no-network / empty states are handled uniformly; the height
 * is bounded so the card wraps its content inside the scrolling screen.
 */
@Composable
internal fun CurrencyConverterCard(
    amount: String,
    targetCode: String,
    spotState: ScreenState<ExchangeRates>,
    onAmountChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
    onRetry: () -> Unit,
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
                text = stringResource(Res.string.screens_currencyrates_converter_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(sp.sm),
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(Res.string.screens_currencyrates_converter_amount_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = targetCode,
                    onValueChange = { onTargetChange(it.uppercase()) },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(Res.string.screens_currencyrates_converter_target_label)) },
                    singleLine = true,
                )
            }
            ScreenContent(
                state = spotState,
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            ) { rates, _ ->
                val code = targetCode.trim().uppercase()
                val parsed = amount.trim().toDoubleOrNull()
                val rate = rates.rates[code]
                if (parsed != null && rate != null) {
                    Text(
                        text = stringResource(
                            Res.string.screens_currencyrates_converter_result,
                            parsed.formatDecimal(2),
                            rates.base,
                            (parsed * rate).formatDecimal(2),
                            code,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.screens_currencyrates_converter_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
internal fun RateItem(code: String, rate: Double) {
    val sp = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sp.lg, vertical = sp.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = rate.formatDecimal(4),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
