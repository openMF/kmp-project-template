/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.store

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import template.core.base.ui.ScreenStateDefaults
import template.core.base.ui.ScreenStateEmpty
import template.core.base.ui.ScreenStateError
import template.core.base.ui.ScreenStateLoading
import template.core.base.ui.ScreenStateNoNetwork

/**
 * App-wide [ScreenStateDefaults] for `template.core.base.ui.ScreenContent` and
 * `template.core.base.ui.PagingScreenContent`.
 *
 * Wire this once at the app's theme root:
 *
 * ```kotlin
 * @Composable
 * fun MifosApp(content: @Composable () -> Unit) {
 *     MifosTheme {
 *         CompositionLocalProvider(
 *             LocalScreenStateDefaults provides appScreenStateDefaults(),
 *         ) {
 *             content()
 *         }
 *     }
 * }
 * ```
 *
 * Forks customize by replacing visuals with branded Lottie animations
 * (`ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.empty)` once the bundled
 * .json files ship — see `core-base/ui/src/commonMain/composeResources/files/screenstate/README.md`),
 * adding telemetry hooks via [ScreenStateError.onShown], and pointing
 * [ScreenStateError.messageFor] at [mapErrorToUserMessage] for domain-aware copy.
 */
@Composable
fun appScreenStateDefaults(): ScreenStateDefaults = remember {
    ScreenStateDefaults(
        loading = ScreenStateLoading.Skeleton(rowCount = 5),
        empty = ScreenStateEmpty(
            // TODO(fork): swap to ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.empty)
            //   once the bundled .json animation files land in core-base/ui composeResources.
            title = "Nothing here yet",
            message = "When you have data, it'll show up here.",
        ),
        error = ScreenStateError(
            messageFor = ::mapErrorToUserMessage,
            // TODO(fork): wire telemetry, e.g.
            //   onShown = { error -> AppTelemetry.recordError("screen_state_error", error) },
        ),
        noNetwork = ScreenStateNoNetwork(
            message = "You're offline. We'll retry when you reconnect.",
        ),
    )
}
