/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.platform.context

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal

actual typealias AppContext = android.content.Context

actual val LocalContext: ProvidableCompositionLocal<AppContext>
    get() = androidx.compose.ui.platform.LocalContext

actual val AppContext.activity: Any
    @Composable
    get() = requireNotNull(LocalActivity.current)
