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

import androidx.compose.runtime.Composable
import kpt.core.base.store.mutation.conflict.ConflictEntry
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Roborazzi-scanned previews for the Sync conflicts screen. `CommonComposablePreviewScanner`
 * auto-discovers each `internal @Preview` in this package and records/verifies a golden render.
 */
@Preview
@Composable
internal fun ConflictResolutionPopulatedPreview() {
    KptTheme {
        ConflictResolutionScreenContent(
            uiState = ConflictInboxUiState.Success(
                conflicts = listOf(
                    ConflictEntry(
                        id = "1",
                        entity = "cloud-todo",
                        key = "Buy groceries",
                        localPayloadJson = "\"local\"",
                        serverPayloadJson = "\"server\"",
                        formRoute = "todo/edit/1",
                        recordedAtMs = 1_000L,
                    ),
                    ConflictEntry(
                        id = "2",
                        entity = "bill-reminder",
                        key = "Electricity",
                        localPayloadJson = "\"local\"",
                        serverPayloadJson = "\"server\"",
                        formRoute = null,
                        recordedAtMs = 900L,
                    ),
                ),
            ),
            onAcceptServer = {},
            onRetryLocal = {},
        )
    }
}

@Preview
@Composable
internal fun ConflictResolutionEmptyPreview() {
    KptTheme {
        ConflictResolutionScreenContent(
            uiState = ConflictInboxUiState.Success(conflicts = emptyList()),
            onAcceptServer = {},
            onRetryLocal = {},
        )
    }
}
