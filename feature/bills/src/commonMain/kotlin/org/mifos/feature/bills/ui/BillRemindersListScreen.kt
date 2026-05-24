/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.bills.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.model.banking.BillReminder
import template.core.base.ui.screen.ScreenContent
import kotlin.time.Clock

/**
 * Top-level Bill Reminders dashboard.
 *
 * - Summary card displays "$X.XX upcoming this month" (30-day window from the VM).
 * - LazyColumn of rows, each with a "Mark paid" trailing button and a tap-to-edit click.
 * - FAB navigates to the add screen.
 *
 * Empty state ("No bills yet — tap + to add") is delegated to the framework's
 * [ScreenContent] which reads the project's `appScreenStateDefaults`.
 */
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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Bill Reminders") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBillClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add bill")
            }
        },
    ) { padding ->
        ScreenContent(
            state = screenState,
            onRetry = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { data, _ ->
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).day
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item("summary") {
                    UpcomingSummaryCard(
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
private fun UpcomingSummaryCard(totalAmount: Double, upcomingCount: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Upcoming this month",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatCurrency(totalAmount),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "$upcomingCount bill(s) due in next 7 days",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BillReminderRow(
    bill: BillReminder,
    today: Int,
    onMarkPaid: () -> Unit,
    onClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = bill.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${formatCurrency(bill.amount)} · ${dueRelativeLabel(bill.dueDay, today)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AssistChip(
                    onClick = onClick,
                    label = { Text(bill.category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    colors = AssistChipDefaults.assistChipColors(),
                )
                if (!bill.enabled) {
                    Text(
                        text = "Reminders disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            TextButton(onClick = onMarkPaid) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Mark paid",
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text("Mark paid")
            }
        }
    }
}

/**
 * Render a relative label for a bill's `dueDay` against the user's current day-of-month.
 * Intentionally simple — the row only shows a single line; full date math sits in the
 * domain layer (recurrence helpers). "Overdue" surfaces only the same-month case; cross-
 * month overdue is rare and folded into "Due in N days".
 */
private fun dueRelativeLabel(dueDay: Int, todayDay: Int): String {
    val diff = dueDay - todayDay
    return when {
        diff == 0 -> "Due today"
        diff == 1 -> "Due tomorrow"
        diff in 2..30 -> "Due in $diff days"
        diff < 0 -> "Overdue (day $dueDay)"
        else -> "Day $dueDay"
    }
}

/**
 * Single-decimal-place currency rendering used in the list. The full localised currency
 * formatter lives in core/ui — we deliberately use a primitive here to avoid dragging
 * extra dependencies into the feature for a non-critical surface.
 */
private fun formatCurrency(value: Double): String {
    val cents = (value * 100).toLong()
    val whole = cents / 100
    val frac = (cents % 100).toString().padStart(2, '0')
    return "$$whole.$frac"
}
