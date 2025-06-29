/*
 * Copyright 2023 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.analytics

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Global key used to obtain access to the AnalyticsHelper through a CompositionLocal.
 */
val LocalAnalyticsHelper = staticCompositionLocalOf<AnalyticsHelper> {
    // Provide a default AnalyticsHelper which does nothing. This is so that tests and previews
    // do not have to provide one. For real app builds provide a different implementation.
    NoOpAnalyticsHelper()
}
