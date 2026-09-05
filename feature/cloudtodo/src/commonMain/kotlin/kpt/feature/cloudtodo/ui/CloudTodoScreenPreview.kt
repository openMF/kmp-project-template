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

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.cloudtodo.CloudTodo
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * `CloudTodoScreen` is not previewed: it resolves its ViewModel through Koin. Literals below are
 * PREVIEW FIXTURE DATA — never reachable from the running app.
 *
 * `MutationOutcome` is the whole reason this demo exists: `MutationResult` is a sealed interface so
 * every write outcome — queued offline, conflicted, rolled back — reaches the user as its own
 * treatment rather than a generic toast. Each arm therefore gets its own preview; a single
 * happy-path render would leave the offline and conflict cards, the two the user most needs to
 * understand, completely uncovered.
 */

private fun previewTodo(completed: Boolean = false) = CloudTodo(
    id = 1,
    title = "Ship the offline-write demo", // i18n:skip
    completed = completed,
)

@Preview
@Composable
internal fun TodoActionsPreview() {
    KptTheme {
        TodoActions(
            todo = previewTodo(),
            onToggleOptimistic = {},
            onCompleteOnline = {},
        )
    }
}

@Preview
@Composable
internal fun TodoActionsCompletedPreview() {
    KptTheme {
        TodoActions(
            todo = previewTodo(completed = true),
            onToggleOptimistic = {},
            onCompleteOnline = {},
        )
    }
}

@Preview
@Composable
internal fun OutcomeCardQueuedOfflinePreview() {
    KptTheme {
        OutcomeCard(
            outcome = MutationOutcome.AppliedQueued,
            onDismiss = {},
            onResolveConflict = {},
        )
    }
}

@Preview
@Composable
internal fun OutcomeCardConflictedPreview() {
    // The only outcome with a second action — "resolve" routes to the conflict inbox. If this arm
    // renders like the others the user has no way to reach their conflicted write.
    KptTheme {
        OutcomeCard(
            outcome = MutationOutcome.Conflicted(conflictId = "c-1"), // i18n:skip
            onDismiss = {},
            onResolveConflict = {},
        )
    }
}

@Preview
@Composable
internal fun OutcomeCardFailedRolledBackPreview() {
    KptTheme {
        OutcomeCard(
            outcome = MutationOutcome.Failed(message = "Server rejected the write", rolledBack = true), // i18n:skip
            onDismiss = {},
            onResolveConflict = {},
        )
    }
}
