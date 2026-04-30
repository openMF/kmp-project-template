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

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Persistent entity for the `samples` table.
 *
 * Uses Room 3 annotations from `androidx.room3` — works on all KMP targets.
 * The class is also [@Serializable][Serializable] so it can be used with
 * [ChargeTypeConverters][org.mifos.core.database.utils.ChargeTypeConverters]
 * for JSON serialization in type-converter columns.
 *
 * @property id Auto-generated primary key.
 * @property name Human-readable sample name.
 */
@Entity(tableName = "samples")
@Serializable
data class SampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
)
