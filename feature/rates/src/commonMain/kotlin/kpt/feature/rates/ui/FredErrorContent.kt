/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.rates.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kpt.core.base.ui.screen.DefaultEmptyContent
import kpt.core.base.ui.screen.DefaultErrorContent
import kpt.core.base.ui.screen.LocalScreenStateDefaults
import kpt.core.base.ui.screen.ScreenStateVisual
import kpt.core.model.economic.FredApiKeyMissingException
import kpt.feature.rates.generated.resources.Res
import kpt.feature.rates.generated.resources.screens_rates_fred_key_missing
import org.jetbrains.compose.resources.stringResource

/**
 * `ScreenContent`'s error slot for FRED-backed screens. A build without a FRED key gets a setup
 * notice with no retry — retrying cannot succeed — and every other failure keeps the framework error.
 */
@Composable
internal fun FredErrorContent(error: Throwable, onRetry: () -> Unit) {
    if (error is FredApiKeyMissingException) {
        DefaultEmptyContent(
            modifier = Modifier.testTag(TestTags.FRED_KEY_MISSING),
            config = LocalScreenStateDefaults.current.empty.copy(
                visual = ScreenStateVisual.Vector(Icons.Default.Key),
                message = stringResource(Res.string.screens_rates_fred_key_missing),
            ),
        )
    } else {
        DefaultErrorContent(error, onRetry)
    }
}
