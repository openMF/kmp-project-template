/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.entity

import kotlinx.serialization.Serializable
import template.core.base.database.Entity
import template.core.base.database.PrimaryKey

@Entity(
    tableName = "samples",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
)
@Serializable
data class SampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
)
