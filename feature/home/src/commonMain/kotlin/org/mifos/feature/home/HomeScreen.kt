/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.common.formatDecimal
import org.mifos.core.common.formatGrouped
import org.mifos.core.model.banking.BillReminder
import org.mifos.feature.home.ui.HomeAction
import org.mifos.feature.home.ui.HomeViewModel
import org.mifos.feature.home.ui.LoansSummary
import org.mifos.feature.home.ui.RatesQuickView
import template.core.base.store.screen.ScreenState
import template.core.base.ui.screen.ScreenContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onSettingsClick: () -> Unit,
    onNavigateToLoans: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToRates: () -> Unit,
    onNavigateToExchangeRates: () -> Unit,
    onNavigateToRateHistory: () -> Unit,
    onNavigateToMacro: () -> Unit,
    onNavigateToEmi: () -> Unit,
    onNavigateToAffordability: () -> Unit,
    onNavigateToAmortization: () -> Unit,
    onNavigateToLoanComparison: () -> Unit,
    onNavigateToLoanCalcWizard: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Money Toolkit") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Live widgets — combine showcase. Each card observes a slice of
            // HomeUiState and renders its own per-card ScreenState transitions.
            LoansSummaryCard(
                state = state.loans,
                onSeeAll = onNavigateToLoans,
            )

            UpcomingBillsCard(
                state = state.bills,
                onSeeAll = onNavigateToBills,
            )

            RatesQuickCard(
                state = state.rates,
                onRetry = { viewModel.trySendAction(HomeAction.RetryRates) },
                onSeeAll = onNavigateToRates,
            )

            ExchangeRateCard(
                state = state.exchangeRate,
                onRetry = { viewModel.trySendAction(HomeAction.RetryExchangeRate) },
                onSeeAll = onNavigateToExchangeRates,
            )

            // Quick access — the rest of the toolkit at one tap.
            Text(
                text = "Quick access",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            FeatureCard(
                title = "EMI Calculator",
                subtitle = "Compute monthly EMI for any loan",
                icon = Icons.Default.Calculate,
                onClick = onNavigateToEmi,
            )
            FeatureCard(
                title = "Affordability",
                subtitle = "How much loan can I afford?",
                icon = Icons.Default.Savings,
                onClick = onNavigateToAffordability,
            )
            FeatureCard(
                title = "Amortization",
                subtitle = "Full payment schedule per loan",
                icon = Icons.AutoMirrored.Default.TrendingUp,
                onClick = onNavigateToAmortization,
            )
            FeatureCard(
                title = "Compare Loans",
                subtitle = "Side-by-side total cost analysis",
                icon = Icons.Default.Compare,
                onClick = onNavigateToLoanComparison,
            )
            FeatureCard(
                title = "Loan Wizard",
                subtitle = "Step-by-step affordability planner",
                icon = Icons.Default.Tune,
                onClick = onNavigateToLoanCalcWizard,
            )
            FeatureCard(
                title = "Currency Rates",
                subtitle = "Live exchange rates by base currency",
                icon = Icons.Default.CurrencyExchange,
                onClick = onNavigateToExchangeRates,
            )
            FeatureCard(
                title = "Rate History",
                subtitle = "Historical FX charts",
                icon = Icons.AutoMirrored.Default.ReceiptLong,
                onClick = onNavigateToRateHistory,
            )
            FeatureCard(
                title = "Country Macro",
                subtitle = "GDP, CPI, unemployment by country",
                icon = Icons.Default.Public,
                onClick = onNavigateToMacro,
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LoansSummaryCard(
    state: ScreenState<LoansSummary>,
    onSeeAll: () -> Unit,
) {
    WidgetCard(
        title = "My Loans",
        icon = Icons.Default.AccountBalance,
        onSeeAll = onSeeAll,
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(96.dp)) {
            ScreenContent(
                state = state,
                onRetry = {},
                modifier = Modifier.fillMaxSize(),
                empty = { WidgetEmpty("Track your first loan — tap See all.") },
            ) { summary, _ ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SummaryRow(
                        label = "Monthly EMI",
                        value = "$${summary.totalMonthlyEmi.formatGrouped(2)}",
                    )
                    SummaryRow(
                        label = "Outstanding",
                        value = "$${summary.totalOutstanding.formatGrouped(2)}",
                    )
                    SummaryRow(
                        label = "Active loans",
                        value = summary.count.toString(),
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingBillsCard(
    state: ScreenState<List<BillReminder>>,
    onSeeAll: () -> Unit,
) {
    WidgetCard(
        title = "Upcoming Bills",
        icon = Icons.Default.NotificationsActive,
        onSeeAll = onSeeAll,
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            ScreenContent(
                state = state,
                onRetry = {},
                modifier = Modifier.fillMaxSize(),
                empty = { WidgetEmpty("No bills due in the next 7 days.") },
            ) { bills, _ ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val total = bills.sumOf { it.amount }
                    SummaryRow(
                        label = "Total due (next 7d)",
                        value = "$${total.formatGrouped(2)}",
                    )
                    bills.take(BILLS_PREVIEW_LIMIT).forEach { bill ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = bill.name,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "Day ${bill.dueDay}  ·  $${bill.amount.formatGrouped(2)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatesQuickCard(
    state: ScreenState<RatesQuickView>,
    onRetry: () -> Unit,
    onSeeAll: () -> Unit,
) {
    WidgetCard(
        title = "Today's Rates",
        icon = Icons.AutoMirrored.Default.TrendingUp,
        onSeeAll = onSeeAll,
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(96.dp)) {
            ScreenContent(
                state = state,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            ) { rates, _ ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SummaryRow(
                        label = "Federal Funds",
                        value = "${rates.fedFundsPercent.formatDecimal(2)}%",
                    )
                    SummaryRow(
                        label = "30Y Mortgage",
                        value = "${rates.mortgage30YPercent.formatDecimal(2)}%",
                    )
                }
            }
        }
    }
}

@Composable
private fun ExchangeRateCard(
    state: ScreenState<org.mifos.core.model.currency.ExchangeRates>,
    onRetry: () -> Unit,
    onSeeAll: () -> Unit,
) {
    WidgetCard(
        title = "USD Exchange",
        icon = Icons.Default.CurrencyExchange,
        onSeeAll = onSeeAll,
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            ScreenContent(
                state = state,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            ) { rates, _ ->
                val keyRates = listOf("EUR", "GBP", "INR")
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    keyRates.forEach { code ->
                        val value = rates.rates[code] ?: return@forEach
                        SummaryRow(
                            label = "USD → $code",
                            value = value.formatGrouped(4),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetCard(
    title: String,
    icon: ImageVector,
    onSeeAll: () -> Unit,
    content: @Composable () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onSeeAll),
                )
            }
            content()
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun WidgetEmpty(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private const val BILLS_PREVIEW_LIMIT = 3
