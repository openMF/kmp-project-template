/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loans.ui

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.component.HeroCard
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.component.AmountDisplay
import kpt.core.designsystem.theme.spacing
import kpt.core.model.demo.banking.Loan
import kpt.feature.loans.generated.resources.Res
import kpt.feature.loans.generated.resources.screens_loans_list_active_count_plural
import kpt.feature.loans.generated.resources.screens_loans_list_active_count_single
import kpt.feature.loans.generated.resources.screens_loans_list_back_cd
import kpt.feature.loans.generated.resources.screens_loans_list_delete_cancel
import kpt.feature.loans.generated.resources.screens_loans_list_delete_confirm
import kpt.feature.loans.generated.resources.screens_loans_list_delete_dialog_message
import kpt.feature.loans.generated.resources.screens_loans_list_delete_dialog_title
import kpt.feature.loans.generated.resources.screens_loans_list_monthly_emi_label
import kpt.feature.loans.generated.resources.screens_loans_list_new_fab_text
import kpt.feature.loans.generated.resources.screens_loans_list_title
import kpt.feature.loans.generated.resources.screens_loans_list_total_outstanding_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinNavViewModel as retainedKoinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalLoansListScreen(
    onBackClick: () -> Unit,
    onAddLoanClick: () -> Unit,
    onLoanClick: (loanId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PersonalLoansListViewModel = retainedKoinViewModel(),
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
                        stringResource(Res.string.screens_loans_list_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_loans_list_back_cd),
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
                onClick = onAddLoanClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(Res.string.screens_loans_list_new_fab_text)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag(TestTags.LoansList.FAB),
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
            // Scroll position survives tab-switch and config-change via the default
            // `rememberLazyListState()` (which uses `rememberSaveable` internally with
            // `LazyListState.Saver`) — Navigation's per-tab `saveState`/`restoreState`
            // and Android's saved-instance-state Bundle carry it. No per-screen code.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = sp.lg)
                    .testTag(TestTags.LoansList.LIST),
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
                        modifier = Modifier.testTag("${TestTags.LoansList.ROW}_${loan.id}"),
                    )
                }
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(Res.string.screens_loans_list_delete_dialog_title)) },
            text = {
                Text(
                    stringResource(
                        Res.string.screens_loans_list_delete_dialog_message,
                        target.name,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteLoan(target.id)
                        pendingDelete = null
                    },
                    modifier = Modifier.testTag(TestTags.LoansList.DELETE_CONFIRM),
                ) { Text(stringResource(Res.string.screens_loans_list_delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(Res.string.screens_loans_list_delete_cancel))
                }
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
                label = stringResource(Res.string.screens_loans_list_total_outstanding_label),
                supporting = {
                    Text(
                        text = stringResource(
                            if (ui.loans.size == 1) {
                                Res.string.screens_loans_list_active_count_single
                            } else {
                                Res.string.screens_loans_list_active_count_plural
                            },
                            ui.loans.size,
                        ),
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
                        text = stringResource(Res.string.screens_loans_list_monthly_emi_label),
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
