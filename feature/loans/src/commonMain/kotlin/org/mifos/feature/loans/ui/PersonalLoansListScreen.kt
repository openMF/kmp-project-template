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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.designsystem.component.AmountDisplay
import org.mifos.core.designsystem.component.HeroCard
import org.mifos.core.designsystem.theme.spacing
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

    val sp = MaterialTheme.spacing
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Personal Loans",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddLoanClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New loan") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
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
                    .padding(horizontal = sp.lg),
                contentPadding = PaddingValues(top = sp.sm, bottom = sp.xxl),
                verticalArrangement = Arrangement.spacedBy(sp.md),
            ) {
                item(key = "summary") {
                    SummaryHero(ui)
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
private fun SummaryHero(ui: LoansListUiState) {
    HeroCard {
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
            AmountDisplay(
                amountText = formatMoney(ui.totalPrincipalRemaining),
                label = "Total outstanding",
                supporting = {
                    Text(
                        text = "${ui.loans.size} active loan${if (ui.loans.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = "Monthly EMI",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = formatMoney(ui.totalMonthlyEmi),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    }
}
