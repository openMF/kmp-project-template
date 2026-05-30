/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.amortization.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.mifos.core.model.banking.AmortizationRow
import template.core.base.ui.screen.ScreenContent
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmortizationScheduleScreen(
    loanId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AmortizationScheduleViewModel = koinViewModel(key = loanId) { parametersOf(loanId) },
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Payment Schedule") },
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
            onRetry = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { rows, _ ->
            AmortizationTable(rows = rows)
        }
    }
}

@Composable
private fun AmortizationTable(
    rows: List<AmortizationRow>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { ScheduleTableHeader() }
        item { HorizontalDivider(thickness = 1.5.dp) }
        itemsIndexed(rows) { index, row ->
            ScheduleRow(
                row = row,
                isEven = index % 2 == 0,
            )
        }
        if (rows.isNotEmpty()) {
            item { HorizontalDivider(thickness = 1.5.dp) }
            item { ScheduleTotalRow(rows = rows) }
        }
    }
}

@Composable
private fun ScheduleTableHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        HeaderCell(text = "#", weight = 0.6f)
        HeaderCell(text = "Principal", weight = 1.3f)
        HeaderCell(text = "Interest", weight = 1.3f)
        HeaderCell(text = "Balance", weight = 1.3f)
    }
}

@Composable
private fun ScheduleRow(
    row: AmortizationRow,
    isEven: Boolean,
    modifier: Modifier = Modifier,
) {
    val bg = if (isEven) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DataCell(text = row.month.toString(), weight = 0.6f, align = TextAlign.Center)
        DataCell(text = formatMoney(row.principal), weight = 1.3f)
        DataCell(
            text = formatMoney(row.interest),
            weight = 1.3f,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DataCell(text = formatMoney(row.balance), weight = 1.3f, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ScheduleTotalRow(
    rows: List<AmortizationRow>,
    modifier: Modifier = Modifier,
) {
    val totalPrincipal = rows.sumOf { it.principal }
    val totalInterest = rows.sumOf { it.interest }
    val totalPayment = rows.sumOf { it.payment }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Summary",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SummaryLine(label = "Total payments", value = formatMoney(totalPayment))
        SummaryLine(label = "Total principal", value = formatMoney(totalPrincipal))
        SummaryLine(
            label = "Total interest",
            value = formatMoney(totalInterest),
            valueColor = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor,
        )
    }
}

@Composable
private fun RowScope.HeaderCell(
    text: String,
    weight: Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.weight(weight)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RowScope.DataCell(
    text: String,
    weight: Float,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.End,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    Box(modifier = modifier.weight(weight)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            textAlign = align,
            color = color,
            fontWeight = fontWeight,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun formatMoney(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "—"
    val rounded = (value * 100.0).roundToLong()
    val whole = rounded / 100L
    val cents = (rounded % 100L).let { if (it < 0) -it else it }
    val sign = if (rounded < 0L && whole == 0L) "-" else ""
    return if (cents == 0L) "$$sign$whole" else "$$sign$whole.${cents.toString().padStart(2, '0')}"
}
