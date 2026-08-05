/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.bills.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.designsystem.component.HeroCard
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.component.AmountDisplay
import kpt.core.designsystem.component.MoneyText
import kpt.core.designsystem.component.MoneyTone
import kpt.core.designsystem.component.StatusChip
import kpt.core.designsystem.component.StatusChipIntent
import kpt.core.designsystem.component.Urgency
import kpt.core.designsystem.component.UrgencyDot
import kpt.core.designsystem.theme.finance
import kpt.core.designsystem.theme.spacing
import kpt.core.model.demo.banking.BillReminder
import kpt.feature.bills.generated.resources.Res
import kpt.feature.bills.generated.resources.screens_bills_list_back_cd
import kpt.feature.bills.generated.resources.screens_bills_list_mark_paid_cd
import kpt.feature.bills.generated.resources.screens_bills_list_new_fab_text
import kpt.feature.bills.generated.resources.screens_bills_list_paid_button
import kpt.feature.bills.generated.resources.screens_bills_list_reminders_disabled
import kpt.feature.bills.generated.resources.screens_bills_list_summary_due_plural
import kpt.feature.bills.generated.resources.screens_bills_list_summary_due_single
import kpt.feature.bills.generated.resources.screens_bills_list_summary_label
import kpt.feature.bills.generated.resources.screens_bills_list_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillRemindersListScreen(
    onBackClick: () -> Unit,
    onAddBillClick: () -> Unit,
    onEditBillClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BillRemindersListViewModel = koinViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val sp = MaterialTheme.spacing

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.screens_bills_list_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_bills_list_back_cd),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddBillClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(Res.string.screens_bills_list_new_fab_text)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag(TestTags.BillsList.FAB),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        ScreenContent(
            state = screenState,
            onRetry = viewModel::onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { data, _ ->
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).day
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = sp.lg,
                    end = sp.lg,
                    top = sp.sm,
                    bottom = sp.xxl,
                ),
                verticalArrangement = Arrangement.spacedBy(sp.md),
            ) {
                item("summary") {
                    UpcomingSummaryHero(
                        totalAmount = data.totalUpcomingAmount,
                        upcomingCount = data.upcoming.size,
                    )
                }
                items(items = data.all, key = { it.id }) { bill ->
                    BillReminderRow(
                        bill = bill,
                        today = today,
                        onMarkPaid = { viewModel.onMarkPaid(bill.id) },
                        onClick = { onEditBillClick(bill.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingSummaryHero(totalAmount: Double, upcomingCount: Int) {
    HeroCard {
        AmountDisplay(
            amountText = formatCurrency(totalAmount),
            label = stringResource(Res.string.screens_bills_list_summary_label),
            supporting = {
                Text(
                    text = if (upcomingCount == 1) {
                        stringResource(Res.string.screens_bills_list_summary_due_single, upcomingCount)
                    } else {
                        stringResource(Res.string.screens_bills_list_summary_due_plural, upcomingCount)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
        )
    }
}

@Composable
private fun BillReminderRow(bill: BillReminder, today: Int, onMarkPaid: () -> Unit, onClick: () -> Unit) {
    val sp = MaterialTheme.spacing
    val diff = bill.dueDay - today
    val urgency = when {
        diff < 0 -> Urgency.Overdue
        diff == 0 -> Urgency.Today
        diff <= 7 -> Urgency.Upcoming
        else -> Urgency.Distant
    }
    val statusIntent = when {
        diff < 0 -> StatusChipIntent.Danger
        diff <= 1 -> StatusChipIntent.Warning
        diff <= 7 -> StatusChipIntent.Info
        else -> StatusChipIntent.Neutral
    }
    val f = MaterialTheme.finance
    val accent = when {
        diff < 0 -> f.urgencyOverdue
        diff <= 1 -> f.urgencyToday
        diff <= 7 -> f.urgencyUpcoming
        else -> null
    }
    AppCard(
        modifier = Modifier.clickable(onClick = onClick),
        accentColor = accent,
    ) {
        // Row 1 — leading dot + bill name + amount on the right
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            UrgencyDot(urgency = urgency)
            Text(
                text = bill.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            MoneyText(
                text = formatCurrency(bill.amount),
                tone = MoneyTone.Negative,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(Modifier.height(sp.sm))

        // Row 2 — chips on left, Paid button on right. Chips get the whole row width
        // when there's no Paid button, otherwise share with the action.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(sp.sm),
        ) {
            StatusChip(text = shortDueLabel(diff), intent = statusIntent)
            StatusChip(
                text = bill.category.name.lowercase().replaceFirstChar { it.uppercase() },
                intent = StatusChipIntent.Neutral,
            )
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = onMarkPaid,
                contentPadding = PaddingValues(horizontal = sp.sm, vertical = 0.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = stringResource(Res.string.screens_bills_list_mark_paid_cd),
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(
                    text = stringResource(Res.string.screens_bills_list_paid_button),
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        if (!bill.enabled) {
            Spacer(Modifier.height(sp.xs))
            Text(
                text = stringResource(Res.string.screens_bills_list_reminders_disabled),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

/** Short due-relative label — fits the [StatusChip] without truncation on narrow phones. */
private fun shortDueLabel(diff: Int): String = when {
    diff == 0 -> "Today"
    diff == 1 -> "Tomorrow"
    diff in 2..30 -> "Due in ${diff}d"
    diff < 0 -> "Overdue"
    else -> "Day $diff"
}

private fun formatCurrency(value: Double): String {
    val cents = (value * 100).toLong()
    val whole = cents / 100
    val frac = (cents % 100).toString().padStart(2, '0')
    return "$$whole.$frac"
}
