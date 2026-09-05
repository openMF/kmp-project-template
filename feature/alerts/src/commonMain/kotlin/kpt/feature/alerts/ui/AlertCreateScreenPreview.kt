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

import androidx.compose.runtime.Composable
import kpt.core.base.store.error.ErrorCategory
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.SubmitState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.alerts.AlertDirection
import kpt.core.model.demo.alerts.PriceAlert
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier — see AlertsListScreenPreview.kt for the
 * full rationale, including why fixture literals carry `// i18n:skip`.
 *
 * `AlertCreateScreen` is the stateful wrapper (it resolves its ViewModel through Koin);
 * `AlertCreateScreenContent` is the stateless body rendered here.
 *
 * This screen is the `submit_offline_write` demo, so the states that matter are the WRITE states:
 * a submit in flight, a network failure that kept the payload in the outbox, and the three-case
 * resume prompt. Each gets its own preview — the happy empty form is the least interesting one.
 */

@Composable
private fun previewContent(
    form: AlertFormState = AlertFormState(coinId = "bitcoin", targetValueText = "50000"), // i18n:skip
    submitState: SubmitState<PriceAlert> = SubmitState.Idle,
    hasResumableDraft: Boolean = false,
) {
    KptTheme {
        AlertCreateScreenContent(
            form = form,
            screenState = ScreenState.Content(previewAlert()),
            submitState = submitState,
            hasResumableDraft = hasResumableDraft,
            onBackClick = {},
            onSubmitted = {},
            onCoinIdChange = {},
            onDirectionChange = {},
            onTargetValueChange = {},
            onSubmit = {},
            onRetry = {},
            onResume = {},
            onDiscardSavedDraft = {},
            onStartFresh = {},
        )
    }
}

@Preview
@Composable
internal fun AlertCreateScreenContentPreview() {
    previewContent()
}

@Preview
@Composable
internal fun AlertCreateScreenContentEmptyFormPreview() {
    // A blank form is the first-open state, and `canSubmit` is false — so this is the only render
    // that shows the submit button in its disabled treatment.
    previewContent(form = AlertFormState())
}

@Preview
@Composable
internal fun AlertCreateScreenContentBelowDirectionPreview() {
    // The direction chips are a two-way selection; rendering BELOW proves the highlight tracks the
    // form rather than always sitting on ABOVE.
    previewContent(
        form = AlertFormState(
            coinId = "ethereum", // i18n:skip
            direction = AlertDirection.BELOW,
            targetValueText = "1500", // i18n:skip
        ),
    )
}

@Preview
@Composable
internal fun AlertCreateScreenContentSubmittingPreview() {
    previewContent(submitState = SubmitState.Submitting())
}

@Preview
@Composable
internal fun AlertCreateScreenContentOfflineFailurePreview() {
    // The offline-write payoff: the failure strip must read as "saved, will retry" rather than
    // "lost", because the draft IS still in the outbox awaiting reconnect.
    previewContent(
        submitState = SubmitState.Failed(
            error = IllegalStateException("no connection"), // i18n:skip
            category = ErrorCategory.Network,
            draftSaved = true,
        ),
    )
}

@Preview
@Composable
internal fun AlertCreateScreenContentResumePromptPreview() {
    // Case 3 of the three-case resume — the prompt that appears over the form when a saved draft
    // is found on entry.
    previewContent(hasResumableDraft = true)
}
