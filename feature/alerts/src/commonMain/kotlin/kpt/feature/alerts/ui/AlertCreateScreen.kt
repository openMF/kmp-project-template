/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.alerts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.ui.submit.MutationScreenContent
import kpt.core.model.demo.alerts.AlertDirection
import kpt.feature.alerts.generated.resources.Res
import kpt.feature.alerts.generated.resources.screens_alert_create_back_cd
import kpt.feature.alerts.generated.resources.screens_alert_create_coin_label
import kpt.feature.alerts.generated.resources.screens_alert_create_dir_above
import kpt.feature.alerts.generated.resources.screens_alert_create_dir_below
import kpt.feature.alerts.generated.resources.screens_alert_create_submit
import kpt.feature.alerts.generated.resources.screens_alert_create_target_label
import kpt.feature.alerts.generated.resources.screens_alert_create_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Create-Price-Alert form — the toolkit's `submit_offline_write` demo UI. The submit routes
 * through [AlertCreateViewModel]'s `DraftSubmitHandler` (offline outbox + reconnect retry);
 * [MutationScreenContent] renders the persistent Saving / Saved / Failed-with-retry strip and
 * calls [onSubmitted] on success so the screen pops back to the list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertCreateScreen(
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlertCreateViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.formState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.testTag(TestTags.AlertCreate.SCREEN),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screens_alert_create_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_alert_create_back_cd),
                        )
                    }
                },
            )
        },
    ) { padding ->
        MutationScreenContent(
            screenState = uiState.screen,
            submitState = uiState.submit,
            onRetry = viewModel::onRetry,
            onSubmitted = { onSubmitted() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) { _, _ ->
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = form.coinId,
                    onValueChange = viewModel::onCoinIdChange,
                    label = { Text(stringResource(Res.string.screens_alert_create_coin_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag(TestTags.AlertCreate.COIN_FIELD),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = form.direction == AlertDirection.ABOVE,
                        onClick = { viewModel.onDirectionChange(AlertDirection.ABOVE) },
                        label = { Text(stringResource(Res.string.screens_alert_create_dir_above)) },
                    )
                    FilterChip(
                        selected = form.direction == AlertDirection.BELOW,
                        onClick = { viewModel.onDirectionChange(AlertDirection.BELOW) },
                        label = { Text(stringResource(Res.string.screens_alert_create_dir_below)) },
                    )
                }
                OutlinedTextField(
                    value = form.targetValueText,
                    onValueChange = viewModel::onTargetValueChange,
                    label = { Text(stringResource(Res.string.screens_alert_create_target_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag(TestTags.AlertCreate.TARGET_FIELD),
                )
                Button(
                    onClick = viewModel::submitForm,
                    enabled = form.canSubmit,
                    modifier = Modifier.fillMaxWidth().testTag(TestTags.AlertCreate.SUBMIT),
                ) {
                    Text(stringResource(Res.string.screens_alert_create_submit))
                }
            }
        }
    }
}
