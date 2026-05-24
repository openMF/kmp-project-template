/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.loans.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.model.banking.Loan
import template.core.base.ui.screen.ScreenContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalLoansListScreen(
    onBackClick: () -> Unit,
    onAddLoanClick: () -> Unit,
    onLoanClick: (loanId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PersonalLoansListViewModel = koinViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<Loan?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Personal Loans") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLoanClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add loan")
            }
        },
    ) { padding ->
        ScreenContent(
            state = screenState,
            onRetry = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { ui, _ ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "summary") {
                    SummaryCard(ui)
                }
                items(items = ui.loans, key = { it.id }) { loan ->
                    LoanRowCard(
                        loan = loan,
                        onClick = { onLoanClick(loan.id) },
                        onLongPress = { pendingDelete = loan },
                    )
                }
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete loan?") },
            text = { Text("'${target.name}' will be removed. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDeleteLoan(target.id)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SummaryCard(ui: LoansListUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Portfolio Summary",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Loans: ${ui.loans.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Total monthly EMI: ${formatMoney(ui.totalMonthlyEmi)}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Total outstanding: ${formatMoney(ui.totalPrincipalRemaining)}",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
