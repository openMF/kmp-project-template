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

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.alerts.AlertDirection
import kpt.core.model.demo.alerts.PriceAlert
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * The `*Screen` entry composables are not previewed: they resolve their ViewModel through Koin.
 * Literals below are PREVIEW FIXTURE DATA — never reachable from the running app — hence
 * `// i18n:skip` rather than a translated string resource.
 */

internal fun previewAlert(
    id: String = "alert-1", // i18n:skip
    direction: AlertDirection = AlertDirection.ABOVE,
    targetValue: Double = 50_000.0,
) = PriceAlert(
    id = id,
    coinId = "bitcoin", // i18n:skip
    direction = direction,
    targetValue = targetValue,
    enabled = true,
    createdAtMs = 1_700_000_000_000L,
)

@Preview
@Composable
internal fun AlertRowPreview() {
    KptTheme {
        AlertRow(alert = previewAlert(), onDelete = {})
    }
}

@Preview
@Composable
internal fun AlertRowEveryDirectionPreview() {
    // `direction` selects the row's label through a three-way `when`. Rendering only ABOVE would
    // leave two thirds of that branch — including the PCT_CHANGE wording — unseen.
    KptTheme {
        Column {
            AlertRow(previewAlert(id = "a1", direction = AlertDirection.ABOVE), onDelete = {}) // i18n:skip
            AlertRow(previewAlert(id = "a2", direction = AlertDirection.BELOW), onDelete = {}) // i18n:skip
            AlertRow(
                previewAlert(id = "a3", direction = AlertDirection.PCT_CHANGE, targetValue = 5.0), // i18n:skip
                onDelete = {},
            )
        }
    }
}
