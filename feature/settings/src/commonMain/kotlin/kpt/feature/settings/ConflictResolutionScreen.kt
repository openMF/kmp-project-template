/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.store.mutation.conflict.ConflictEntry
import kpt.core.designsystem.theme.spacing
import kpt.core.ui.scaffold.KptScaffold
import kpt.feature.settings.generated.resources.Res
import kpt.feature.settings.generated.resources.feature_settings_conflicts_accept_server
import kpt.feature.settings.generated.resources.feature_settings_conflicts_empty_message
import kpt.feature.settings.generated.resources.feature_settings_conflicts_empty_title
import kpt.feature.settings.generated.resources.feature_settings_conflicts_retry_local
import kpt.feature.settings.generated.resources.feature_settings_conflicts_row_label
import kpt.feature.settings.generated.resources.feature_settings_conflicts_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Settings Sync conflicts screen — VM-connected entry point. Lists pending write conflicts from the
 * [ConflictInboxViewModel]; each row resolves to Keep-server or Retry-mine.
 */
@Composable
internal fun ConflictResolutionScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConflictInboxViewModel = koinViewModel(),
) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    ConflictResolutionScreenContent(
        uiState = uiState,
        onAcceptServer = viewModel::acceptServer,
        onRetryLocal = viewModel::retryLocal,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

/** Stateless, previewable body — rendered by Roborazzi via the `@Preview` functions. */
@Composable
internal fun ConflictResolutionScreenContent(
    uiState: ConflictInboxUiState,
    onAcceptServer: (String) -> Unit,
    onRetryLocal: (String) -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.spacing
    KptScaffold(
        title = stringResource(Res.string.feature_settings_conflicts_title),
        onNavigationIconClick = onBackClick,
        modifier = modifier,
    ) {
        when (uiState) {
            ConflictInboxUiState.Loading -> Unit
            is ConflictInboxUiState.Success -> if (uiState.isEmpty) {
                ConflictEmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(sp.md),
                    verticalArrangement = Arrangement.spacedBy(sp.sm),
                ) {
                    items(uiState.conflicts, key = { it.id }) { conflict ->
                        ConflictCard(conflict, onAcceptServer, onRetryLocal)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConflictCard(
    conflict: ConflictEntry,
    onAcceptServer: (String) -> Unit,
    onRetryLocal: (String) -> Unit,
) {
    val sp = MaterialTheme.spacing
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(sp.md), verticalArrangement = Arrangement.spacedBy(sp.xs)) {
            Text(
                text = stringResource(Res.string.feature_settings_conflicts_row_label) + " · " + conflict.entity,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(text = conflict.key, style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = sp.xs),
                horizontalArrangement = Arrangement.spacedBy(sp.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = { onAcceptServer(conflict.id) }) {
                    Text(stringResource(Res.string.feature_settings_conflicts_accept_server))
                }
                TextButton(onClick = { onRetryLocal(conflict.id) }) {
                    Text(stringResource(Res.string.feature_settings_conflicts_retry_local))
                }
            }
        }
    }
}

@Composable
private fun ConflictEmptyState(modifier: Modifier = Modifier) {
    val sp = MaterialTheme.spacing
    Column(
        modifier = modifier.fillMaxWidth().padding(sp.lg),
        verticalArrangement = Arrangement.spacedBy(sp.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.feature_settings_conflicts_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(Res.string.feature_settings_conflicts_empty_message),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
