/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.macro.ui

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Instant

/*
 * @Preview siblings for the device-free CMP render tier — see CountryPickerScreenPreview.kt for the
 * full rationale.
 */

@Preview
@Composable
internal fun OfflineDataBannerWithTimestampPreview() {
    KptTheme {
        OfflineDataBanner(fetchedAt = Instant.fromEpochMilliseconds(1_700_000_000_000L))
    }
}

@Preview
@Composable
internal fun OfflineDataBannerNeverFetchedPreview() {
    // `fetchedAt = null` is the never-synced case. It takes a different copy path from the
    // timestamped one, so rendering only the happy variant leaves the cold-start banner uncovered.
    KptTheme {
        OfflineDataBanner(fetchedAt = null)
    }
}
