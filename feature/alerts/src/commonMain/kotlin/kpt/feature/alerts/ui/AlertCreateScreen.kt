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
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.ui.draft.DraftResolutionPrompt
import kpt.core.base.ui.submit.MutationScreenContent
import kpt.core.model.demo.alerts.AlertDirection
import kpt.core.model.demo.alerts.PriceAlert
import kpt.feature.alerts.generated.resources.Res
import kpt.feature.alerts.generated.resources.screens_alert_create_back_cd
import kpt.feature.alerts.generated.resources.screens_alert_create_coin_label
import kpt.feature.alerts.generated.resources.screens_alert_create_dir_above
import kpt.feature.alerts.generated.resources.screens_alert_create_dir_below
import kpt.feature.alerts.generated.resources.screens_alert_create_draft_resume_discard
import kpt.feature.alerts.generated.resources.screens_alert_create_draft_resume_message
import kpt.feature.alerts.generated.resources.screens_alert_create_draft_resume_resume
import kpt.feature.alerts.generated.resources.screens_alert_create_draft_resume_start_fresh
import kpt.feature.alerts.generated.resources.screens_alert_create_draft_resume_title
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
@Composable
fun AlertCreateScreen(
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlertCreateViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.formState.collectAsStateWithLifecycle()

    AlertCreateScreenContent(
        form = form,
        screenState = uiState.screen,
        submitState = uiState.submit,
        hasResumableDraft = uiState.hasResumableDraft,
        onBackClick = onBackClick,
        onSubmitted = onSubmitted,
        onCoinIdChange = viewModel::onCoinIdChange,
        onDirectionChange = viewModel::onDirectionChange,
        onTargetValueChange = viewModel::onTargetValueChange,
        onSubmit = viewModel::submitForm,
        onRetry = viewModel::onRetry,
        onResume = viewModel::onResume,
        onDiscardSavedDraft = viewModel::onDiscardSavedDraft,
        onStartFresh = viewModel::onStartFresh,
        modifier = modifier,
    )
}

/**
 * Stateless body — every visual decision lives here, so `@Preview` can render it directly without a
 * Koin graph (the device-free CMP render tier). Follows the template's house pattern; see
 * `SettingsScreen`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlertCreateScreenContent(
    form: AlertFormState,
    screenState: ScreenState<PriceAlert>,
    submitState: SubmitState<PriceAlert>,
    hasResumableDraft: Boolean,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit,
    onCoinIdChange: (String) -> Unit,
    onDirectionChange: (AlertDirection) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onResume: () -> Unit,
    onDiscardSavedDraft: () -> Unit,
    onStartFresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Three-case resume: on entry, if a saved draft exists for this alert, let the user choose to
    // resume it, discard it, or start fresh (the draft stays recoverable in Settings → Sync & Drafts).
    if (hasResumableDraft) {
        DraftResolutionPrompt(
            onResume = onResume,
            onDiscard = onDiscardSavedDraft,
            onStartFresh = onStartFresh,
            onDismiss = onStartFresh,
            title = stringResource(Res.string.screens_alert_create_draft_resume_title),
            message = stringResource(Res.string.screens_alert_create_draft_resume_message),
            resumeLabel = stringResource(Res.string.screens_alert_create_draft_resume_resume),
            discardLabel = stringResource(Res.string.screens_alert_create_draft_resume_discard),
            startFreshLabel = stringResource(Res.string.screens_alert_create_draft_resume_start_fresh),
        )
    }

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
            screenState = screenState,
            submitState = submitState,
            onRetry = onRetry,
            onSubmitted = { onSubmitted() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) { _, _ ->
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = form.coinId,
                    onValueChange = onCoinIdChange,
                    label = { Text(stringResource(Res.string.screens_alert_create_coin_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag(TestTags.AlertCreate.COIN_FIELD),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = form.direction == AlertDirection.ABOVE,
                        onClick = { onDirectionChange(AlertDirection.ABOVE) },
                        label = { Text(stringResource(Res.string.screens_alert_create_dir_above)) },
                    )
                    FilterChip(
                        selected = form.direction == AlertDirection.BELOW,
                        onClick = { onDirectionChange(AlertDirection.BELOW) },
                        label = { Text(stringResource(Res.string.screens_alert_create_dir_below)) },
                    )
                }
                OutlinedTextField(
                    value = form.targetValueText,
                    onValueChange = onTargetValueChange,
                    label = { Text(stringResource(Res.string.screens_alert_create_target_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag(TestTags.AlertCreate.TARGET_FIELD),
                )
                Button(
                    onClick = onSubmit,
                    enabled = form.canSubmit,
                    modifier = Modifier.fillMaxWidth().testTag(TestTags.AlertCreate.SUBMIT),
                ) {
                    Text(stringResource(Res.string.screens_alert_create_submit))
                }
            }
        }
    }
}
