/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/** Identifies this target as Android, with its version where the platform exposes one. */
actual fun getPlatform(): Platform {
    return Platform.Android
}

/** Whether a system-derived palette is available. True on API 31+, where Material You exposes wallpaper colours. */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
actual fun supportsDynamicTheming(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}
