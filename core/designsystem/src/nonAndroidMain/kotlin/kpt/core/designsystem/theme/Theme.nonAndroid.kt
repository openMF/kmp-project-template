/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * The brand colour scheme. [dynamicColor] is ignored — no non-Android target exposes a system palette.
 *
 * @param useDarkTheme whether to build the dark scheme.
 * @param dynamicColor accepted for signature parity, never honoured.
 */
@Composable
actual fun platformColorScheme(useDarkTheme: Boolean, dynamicColor: Boolean): ColorScheme {
    return when (useDarkTheme) {
        true -> darkColorScheme()
        false -> lightColorScheme()
    }
}
