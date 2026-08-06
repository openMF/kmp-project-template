/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.alerts.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.model.demo.alerts.AlertDirection
import kpt.core.model.demo.alerts.PriceAlert
import kpt.feature.alerts.generated.resources.Res
import kpt.feature.alerts.generated.resources.screens_alerts_back_cd
import kpt.feature.alerts.generated.resources.screens_alerts_create_cd
import kpt.feature.alerts.generated.resources.screens_alerts_delete_cd
import kpt.feature.alerts.generated.resources.screens_alerts_row_above
import kpt.feature.alerts.generated.resources.screens_alerts_row_below
import kpt.feature.alerts.generated.resources.screens_alerts_row_pct_change
import kpt.feature.alerts.generated.resources.screens_alerts_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Price-alerts list — the read side of the toolkit's `submit_offline_write` demo. Renders
 * saved alerts (offline Room-backed reactive list) with per-row delete; a FAB routes to the
 * offline-first create form ([AlertCreateScreen]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsListScreen(
    onBackClick: () -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlertsListViewModel = koinViewModel(),
) {
    Scaffold(
        modifier = modifier.testTag(TestTags.AlertsList.SCREEN),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screens_alerts_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_alerts_back_cd),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                modifier = Modifier.testTag(TestTags.AlertsList.CREATE_FAB),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(Res.string.screens_alerts_create_cd),
                )
            }
        },
    ) { padding ->
        ScreenContent(
            stream = viewModel.alerts,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) { alerts, _ ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = alerts, key = PriceAlert::id) { alert ->
                    AlertRow(alert = alert, onDelete = { viewModel.onDelete(alert.id) })
                }
            }
        }
    }
}

@Composable
private fun AlertRow(
    alert: PriceAlert,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val directionLabel = when (alert.direction) {
        AlertDirection.ABOVE -> stringResource(Res.string.screens_alerts_row_above)
        AlertDirection.BELOW -> stringResource(Res.string.screens_alerts_row_below)
        AlertDirection.PCT_CHANGE -> stringResource(Res.string.screens_alerts_row_pct_change)
    }
    ListItem(
        modifier = modifier.fillMaxWidth(),
        headlineContent = { Text(alert.coinId) },
        supportingContent = { Text("$directionLabel ${alert.targetValue}") },
        trailingContent = {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag(TestTags.AlertsList.DELETE_PREFIX + alert.id),
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(Res.string.screens_alerts_delete_cd),
                )
            }
        },
    )
}
