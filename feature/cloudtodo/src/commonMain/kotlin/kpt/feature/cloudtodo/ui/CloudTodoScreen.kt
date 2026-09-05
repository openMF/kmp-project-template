/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.cloudtodo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.theme.spacing
import kpt.core.model.demo.cloudtodo.CloudTodo
import kpt.core.ui.scaffold.KptScaffold
import kpt.feature.cloudtodo.generated.resources.Res
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_complete_online_text
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_detail_intro
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_detail_title
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_applied_local_body
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_applied_local_title
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_applied_queued_body
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_applied_queued_title
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_applied_synced_body
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_applied_synced_title
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_blocked_offline
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_blocked_precondition
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_blocked_title
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_blocked_unauthenticated
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_conflicted_body
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_conflicted_title
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_dismiss_text
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_failed_kept
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_failed_rolled_back
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_failed_title
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_outcome_resolve_text
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_status_completed
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_status_not_completed
import kpt.feature.cloudtodo.generated.resources.screens_cloudtodo_toggle_optimistic_text
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * **Cloud Todo (write-path demo)** — the on-device surface for the Store5 write path.
 *
 * UI-SOURCE (RULE-UI-SOURCE-001): converted from `idea-layer/screens/cloudtodo/preview/content.html`
 * — `source: preview`, the highest tier present (no `ui_source_override`, no stitch render).
 * Recorded in `idea-layer/state/ui-source-ledger.jsonl` BEFORE this file was written. Structure
 * mirrors that preview 1:1: app bar → intro → summary → the two policy buttons → outcome card.
 *
 * Why it exists: `CloudTodoRepository` is the template's only `createMutableStore` +
 * `MutationGateway` consumer and had no UI, so the two `MutationPolicy` arms and the exhaustive
 * `MutationResult` rendering were never observable in a running app.
 *
 * **Turn the network off** to reach the interesting arms: the optimistic write still lands and
 * queues; the online-required write returns `Blocked(OFFLINE)` and writes nothing.
 *
 * Conflict RESOLUTION is not here — a `Conflicted` outcome hands off to the shipped
 * `SyncAndDraftsScreen` (feature/settings) via [onResolveConflict].
 */
@Composable
fun CloudTodoScreen(
    onBackClick: () -> Unit,
    onResolveConflict: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CloudTodoViewModel = koinViewModel(),
) {
    val sp = MaterialTheme.spacing
    val outcome by viewModel.lastOutcome.collectAsStateWithLifecycle()

    KptScaffold(
        onNavigationIconClick = onBackClick,
        title = stringResource(Res.string.screens_cloudtodo_detail_title),
        modifier = modifier.testTag(TestTags.CloudTodo.SCREEN),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = sp.lg, vertical = sp.md),
            verticalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            Text(
                text = stringResource(Res.string.screens_cloudtodo_detail_intro),
                style = MaterialTheme.typography.bodyMedium,
            )

            // ScreenContent's content lambda is (data, freshnessSignal) — this screen does not render
            // a staleness banner, so the signal is unused.
            //
            // weight(fill = false) is load-bearing: ScreenContent fills the available height by
            // default, which pushed the outcome card below the viewport — present in the tree but
            // never visible. A user would have tapped an action and seen nothing. Caught by
            // CloudTodoScreenUiTest (node existed, assertIsDisplayed failed).
            ScreenContent(
                stream = viewModel.todo,
                modifier = Modifier.weight(1f, fill = false),
            ) { todo, _ ->
                TodoActions(
                    todo = todo,
                    onToggleOptimistic = viewModel::onToggleOptimistic,
                    onCompleteOnline = viewModel::onCompleteOnline,
                )
            }

            outcome?.let {
                OutcomeCard(
                    outcome = it,
                    onDismiss = viewModel::onDismissOutcome,
                    onResolveConflict = onResolveConflict,
                )
            }
        }
    }
}

@Composable
internal fun TodoActions(
    todo: CloudTodo,
    onToggleOptimistic: (CloudTodo) -> Unit,
    onCompleteOnline: (CloudTodo) -> Unit,
) {
    val sp = MaterialTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(sp.md)) {
        Column(
            modifier = Modifier.testTag(TestTags.CloudTodo.SUMMARY),
            verticalArrangement = Arrangement.spacedBy(sp.xs),
        ) {
            Text(
                text = "#${todo.id} · ${todo.title}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (todo.completed) {
                    stringResource(Res.string.screens_cloudtodo_status_completed)
                } else {
                    stringResource(Res.string.screens_cloudtodo_status_not_completed)
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(sp.sm)) {
            // Optimistic — Room-first; lands locally, queues the network sync, retries on reconnect.
            Button(
                onClick = { onToggleOptimistic(todo) },
                modifier = Modifier.testTag(TestTags.CloudTodo.TOGGLE_OPTIMISTIC),
            ) {
                Text(stringResource(Res.string.screens_cloudtodo_toggle_optimistic_text))
            }
            // OnlineRequired — awaits the server; offline it is Blocked and NOTHING is written.
            OutlinedButton(
                onClick = { onCompleteOnline(todo) },
                modifier = Modifier.testTag(TestTags.CloudTodo.COMPLETE_ONLINE),
            ) {
                Text(stringResource(Res.string.screens_cloudtodo_complete_online_text))
            }
        }
    }
}

@Composable
internal fun OutcomeCard(
    outcome: MutationOutcome,
    onDismiss: () -> Unit,
    onResolveConflict: () -> Unit,
) {
    val sp = MaterialTheme.spacing
    Card(
        modifier = Modifier.fillMaxWidth().testTag(TestTags.CloudTodo.OUTCOME),
        colors = CardDefaults.cardColors(containerColor = outcome.containerColor()),
    ) {
        Column(
            modifier = Modifier.padding(sp.md),
            verticalArrangement = Arrangement.spacedBy(sp.sm),
        ) {
            Text(text = outcome.title(), style = MaterialTheme.typography.titleSmall)
            Text(text = outcome.body(), style = MaterialTheme.typography.bodySmall)

            Row(horizontalArrangement = Arrangement.spacedBy(sp.sm)) {
                if (outcome is MutationOutcome.Conflicted) {
                    Button(
                        onClick = onResolveConflict,
                        modifier = Modifier.testTag(TestTags.CloudTodo.OUTCOME_RESOLVE),
                    ) {
                        Text(stringResource(Res.string.screens_cloudtodo_outcome_resolve_text))
                    }
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag(TestTags.CloudTodo.OUTCOME_DISMISS),
                ) {
                    Text(stringResource(Res.string.screens_cloudtodo_outcome_dismiss_text))
                }
            }
        }
    }
}

/** Severity tint per arm — mirrors the preview's `.outcome.{applied,blocked,conflicted,failed}`. */
@Composable
private fun MutationOutcome.containerColor() = when (this) {
    MutationOutcome.OptimisticApplied,
    MutationOutcome.AppliedSynced,
    MutationOutcome.AppliedQueued,
    -> MaterialTheme.colorScheme.secondaryContainer
    is MutationOutcome.Blocked -> MaterialTheme.colorScheme.tertiaryContainer
    is MutationOutcome.Conflicted -> MaterialTheme.colorScheme.tertiaryContainer
    is MutationOutcome.Failed -> MaterialTheme.colorScheme.errorContainer
}

@Composable
private fun MutationOutcome.title(): String = when (this) {
    MutationOutcome.OptimisticApplied -> stringResource(Res.string.screens_cloudtodo_outcome_applied_local_title)
    MutationOutcome.AppliedSynced -> stringResource(Res.string.screens_cloudtodo_outcome_applied_synced_title)
    MutationOutcome.AppliedQueued -> stringResource(Res.string.screens_cloudtodo_outcome_applied_queued_title)
    is MutationOutcome.Blocked -> stringResource(Res.string.screens_cloudtodo_outcome_blocked_title)
    is MutationOutcome.Conflicted -> stringResource(Res.string.screens_cloudtodo_outcome_conflicted_title)
    is MutationOutcome.Failed -> stringResource(Res.string.screens_cloudtodo_outcome_failed_title)
}

@Composable
private fun MutationOutcome.body(): String = when (this) {
    MutationOutcome.OptimisticApplied -> stringResource(Res.string.screens_cloudtodo_outcome_applied_local_body)
    MutationOutcome.AppliedSynced -> stringResource(Res.string.screens_cloudtodo_outcome_applied_synced_body)
    MutationOutcome.AppliedQueued -> stringResource(Res.string.screens_cloudtodo_outcome_applied_queued_body)
    is MutationOutcome.Blocked -> when (reason) {
        BlockReason.OFFLINE -> stringResource(Res.string.screens_cloudtodo_outcome_blocked_offline)
        BlockReason.UNAUTHENTICATED -> stringResource(Res.string.screens_cloudtodo_outcome_blocked_unauthenticated)
        BlockReason.PRECONDITION_FAILED -> stringResource(Res.string.screens_cloudtodo_outcome_blocked_precondition)
    }
    is MutationOutcome.Conflicted -> stringResource(Res.string.screens_cloudtodo_outcome_conflicted_body)
    is MutationOutcome.Failed -> if (rolledBack) {
        stringResource(Res.string.screens_cloudtodo_outcome_failed_rolled_back, message)
    } else {
        stringResource(Res.string.screens_cloudtodo_outcome_failed_kept, message)
    }
}
