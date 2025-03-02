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
data class SampleUser(
    val id: Long,
    val name: String,
    val email: String,
    val isActive: Boolean,
    val age: Int,
) {
    companion object {
        val DEFAULT = SampleUser(
            id = 0L,
            name = "Guest",
            email = "guest@example.com",
            isActive = false,
            age = 0,
        )
    }
}
