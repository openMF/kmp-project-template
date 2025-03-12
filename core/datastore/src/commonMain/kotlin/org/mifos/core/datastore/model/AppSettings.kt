/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.datastore.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val theme: String,
    val language: String,
) {
    companion object {
        val DEFAULT = AppSettings(
            theme = AppTheme.SYSTEM_DEFAULT.themeName,
            language = AppLanguage.ENGLISH.code,
        )
    }
}
