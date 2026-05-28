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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.common.formatDecimal
import org.mifos.core.common.formatGrouped
import org.mifos.core.designsystem.component.AmountDisplay
import org.mifos.core.designsystem.component.FreshnessIndicator
import org.mifos.core.designsystem.component.FreshnessState
import org.mifos.core.designsystem.component.MoneyText
import org.mifos.core.designsystem.component.MoneyTone
import org.mifos.core.designsystem.component.RateBadge
import org.mifos.core.designsystem.component.RateDirection
import org.mifos.core.designsystem.component.SectionHeader
import org.mifos.core.designsystem.component.Urgency
import org.mifos.core.designsystem.component.UrgencyDot
import org.mifos.core.designsystem.theme.spacing
import org.mifos.core.model.banking.BillReminder
import org.mifos.feature.home.ui.HomeAction
import org.mifos.feature.home.ui.HomeViewModel
import org.mifos.feature.home.ui.LoansSummary
import org.mifos.feature.home.ui.RatesQuickView
import template.core.base.designsystem.component.AppCard
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
    val sp = MaterialTheme.spacing

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Money Toolkit",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = sp.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            Spacer(Modifier.height(sp.xs))

            // ── Hero: at-a-glance financial snapshot ─────────────────────────
            HeroSnapshot(state.loans)

            // ── Loans carousel — scroll through every active loan ────────────
            LoansCarousel(state.loans, onLoanClick = onNavigateToLoans)

            // ── Quick stats grid (Bills + Rates) ─────────────────────────────
            SectionHeader(title = "This week")

            BillsQuickCard(
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

            // ── Tools grid ───────────────────────────────────────────────────
            SectionHeader(
                title = "Tools",
                supporting = "Calculators, comparisons, macro indicators",
            )
            FeatureRow(
                title = "EMI Calculator",
                subtitle = "Monthly payment for any loan",
                icon = Icons.Default.Calculate,
                onClick = onNavigateToEmi,
            )
            FeatureRow(
                title = "Affordability",
                subtitle = "How much can I borrow safely?",
                icon = Icons.Default.Savings,
                onClick = onNavigateToAffordability,
            )
            FeatureRow(
                title = "Amortization",
                subtitle = "Full payment schedule",
                icon = Icons.AutoMirrored.Default.TrendingUp,
                onClick = onNavigateToAmortization,
            )
            FeatureRow(
                title = "Compare Loans",
                subtitle = "Side-by-side total cost",
                icon = Icons.Default.Compare,
                onClick = onNavigateToLoanComparison,
            )
            FeatureRow(
                title = "Loan Wizard",
                subtitle = "Step-by-step planner",
                icon = Icons.Default.Tune,
                onClick = onNavigateToLoanCalcWizard,
            )
            FeatureRow(
                title = "Currency Rates",
                subtitle = "Live exchange rates",
                icon = Icons.Default.CurrencyExchange,
                onClick = onNavigateToExchangeRates,
            )
            FeatureRow(
                title = "Rate History",
                subtitle = "Historical FX charts",
                icon = Icons.AutoMirrored.Default.ReceiptLong,
                onClick = onNavigateToRateHistory,
            )
            FeatureRow(
                title = "Country Macro",
                subtitle = "GDP, CPI, unemployment",
                icon = Icons.Default.Public,
                onClick = onNavigateToMacro,
            )

            Spacer(Modifier.height(sp.lg))
        }
    }
}

/**
 * Hero card — the dashboard's first impression. Renders the user's outstanding-loans
 * snapshot on a primary-gradient surface so it pops above the rest of the page.
 */
@Composable
private fun HeroSnapshot(state: ScreenState<LoansSummary>) {
    template.core.base.designsystem.component.HeroCard {
        ScreenContent(
            state = state,
            onRetry = {},
            modifier = Modifier.fillMaxWidth(),
            empty = {
                Column {
                    AmountDisplay(
                        amountText = "$0.00",
                        label = "Outstanding balance",
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                    Text(
                        text = "No active loans — start by adding one.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
        ) { summary, _ ->
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
                AmountDisplay(
                    amountText = "$${summary.totalOutstanding.formatGrouped(2)}",
                    label = "Total outstanding",
                    supporting = {
                        Text(
                            text = "${summary.count} active loan${if (summary.count == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    HeroStat(
                        label = "Monthly EMI",
                        value = "$${summary.totalMonthlyEmi.formatGrouped(2)}",
                    )
                    HeroStat(
                        label = "Loans",
                        value = summary.count.toString(),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

/**
 * Horizontally-scrollable carousel of per-loan tiles. Renders nothing when the loans state
 * isn't [ScreenState.Content] OR when the user has no loans yet — the [HeroSnapshot] empty-state
 * already prompts them to add one. Tapping any tile navigates to the full Personal Loans screen.
 */
@Composable
private fun LoansCarousel(
    state: ScreenState<LoansSummary>,
    onLoanClick: () -> Unit,
) {
    val content = state as? ScreenState.Content ?: return
    val loans = content.data.loans
    if (loans.isEmpty()) return

    val sp = MaterialTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(sp.sm)) {
        Text(
            text = "Your loans · ${loans.size}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(sp.md),
            contentPadding = PaddingValues(end = sp.md),
        ) {
            items(items = loans, key = { it.id }) { loan ->
                LoanCarouselTile(loan = loan, onClick = onLoanClick)
            }
        }
    }
}

/** Compact per-loan tile used by [LoansCarousel]. Sized to fit 2–2.5 cards on a phone screen. */
@Composable
private fun LoanCarouselTile(
    loan: org.mifos.core.model.banking.Loan,
    onClick: () -> Unit,
) {
    val sp = MaterialTheme.spacing
    val progress = if (loan.principal > 0) {
        (1.0 - (loan.principalRemaining / loan.principal)).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    AppCard(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        accentColor = loanKindAccent(loan.kind),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(sp.xs)) {
            Text(
                text = loan.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Text(
                text = "Remaining",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            org.mifos.core.designsystem.component.MoneyText(
                text = "$${loan.principalRemaining.formatGrouped(2)}",
                tone = org.mifos.core.designsystem.component.MoneyTone.Negative,
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(sp.xs))
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )
            Text(
                text = "EMI $${loan.monthlyPayment.formatGrouped(2)} · ${(progress * 100).toInt()}% paid",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Maps each [org.mifos.core.model.banking.LoanKind] to a distinct accent stripe colour so
 * users can scan a list of loans and identify mortgage vs. auto vs. personal vs. business
 * vs. student at a glance — same vocabulary as [PersonalLoansListScreen]'s LoanRowCard.
 */
@Composable
private fun loanKindAccent(
    kind: org.mifos.core.model.banking.LoanKind,
): androidx.compose.ui.graphics.Color? = when (kind) {
    org.mifos.core.model.banking.LoanKind.MORTGAGE -> MaterialTheme.colorScheme.secondary
    org.mifos.core.model.banking.LoanKind.BUSINESS -> MaterialTheme.colorScheme.secondary
    org.mifos.core.model.banking.LoanKind.AUTO -> MaterialTheme.colorScheme.tertiary
    org.mifos.core.model.banking.LoanKind.STUDENT -> MaterialTheme.colorScheme.tertiary
    org.mifos.core.model.banking.LoanKind.PERSONAL -> MaterialTheme.colorScheme.primary
    org.mifos.core.model.banking.LoanKind.OTHER -> null
}

@Composable
private fun BillsQuickCard(
    state: ScreenState<List<BillReminder>>,
    onSeeAll: () -> Unit,
) {
    SectionCard(
        title = "Upcoming Bills",
        icon = Icons.Default.NotificationsActive,
        onSeeAll = onSeeAll,
    ) {
        ScreenContent(
            state = state,
            onRetry = {},
            modifier = Modifier.fillMaxWidth(),
            empty = { WidgetEmpty("No bills due in the next 7 days.") },
        ) { bills, _ ->
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                val total = bills.sumOf { it.amount }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Total due (next 7d)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    MoneyText(
                        text = "$${total.formatGrouped(2)}",
                        tone = MoneyTone.Negative,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                bills.take(BILLS_PREVIEW_LIMIT).forEach { bill ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    ) {
                        UrgencyDot(urgency = urgencyForDay(bill.dueDay))
                        Text(
                            text = bill.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "Day ${bill.dueDay}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        MoneyText(
                            text = "$${bill.amount.formatGrouped(2)}",
                            tone = MoneyTone.Negative,
                            style = MaterialTheme.typography.bodyMedium,
                        )
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
    SectionCard(
        title = "Today's Rates",
        icon = Icons.AutoMirrored.Default.TrendingUp,
        onSeeAll = onSeeAll,
    ) {
        ScreenContent(
            state = state,
            onRetry = onRetry,
            modifier = Modifier.fillMaxWidth(),
        ) { rates, _ ->
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                RateRow(
                    label = "Federal Funds",
                    value = "${rates.fedFundsPercent.formatDecimal(2)}%",
                    direction = RateDirection.Flat,
                    delta = "—",
                )
                RateRow(
                    label = "30Y Mortgage",
                    value = "${rates.mortgage30YPercent.formatDecimal(2)}%",
                    direction = RateDirection.Up,
                    delta = "+0.05",
                )
                FreshnessIndicator(state = FreshnessState.Fresh, label = "Live · FRED")
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
    SectionCard(
        title = "USD Exchange",
        icon = Icons.Default.CurrencyExchange,
        onSeeAll = onSeeAll,
    ) {
        ScreenContent(
            state = state,
            onRetry = onRetry,
            modifier = Modifier.fillMaxWidth(),
        ) { rates, _ ->
            val keyRates = listOf("EUR", "GBP", "INR")
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                keyRates.forEach { code ->
                    val value = rates.rates[code] ?: return@forEach
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "USD → $code",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = value.formatGrouped(4),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    onSeeAll: () -> Unit,
    content: @Composable () -> Unit,
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
            Text(
                text = "See all",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onSeeAll),
            )
        }
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        content()
    }
}

@Composable
private fun RateRow(
    label: String,
    value: String,
    direction: RateDirection,
    delta: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Spacer(Modifier.size(MaterialTheme.spacing.sm))
        RateBadge(delta = delta, direction = direction)
    }
}

@Composable
private fun WidgetEmpty(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
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
private fun FeatureRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    AppCard(
        modifier = Modifier.clickable(onClick = onClick),
        contentPadding = PaddingValues(MaterialTheme.spacing.lg),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Heuristic mapping bill due-day to an urgency tier for the [UrgencyDot]. */
private fun urgencyForDay(dueDay: Int): Urgency = when {
    dueDay <= 1 -> Urgency.Today
    dueDay <= 3 -> Urgency.Upcoming
    dueDay <= 7 -> Urgency.Upcoming
    else -> Urgency.Distant
}

private const val BILLS_PREVIEW_LIMIT = 3
