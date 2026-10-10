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

/** Identifies this target as iOS, with its version where the platform exposes one. */
actual fun getPlatform(): Platform = Platform.IOS

/** Whether a system-derived palette is available. Always false — iOS has no system palette to read. */
actual fun supportsDynamicTheming(): Boolean = false
