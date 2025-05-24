/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.utils

import kotlinx.serialization.json.Json
import org.mifos.core.database.entity.SampleEntity
import template.core.base.database.TypeConverter

class ChargeTypeConverters {

    @TypeConverter
    fun fromIntList(value: String): ArrayList<Int?> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun toIntList(list: ArrayList<Int?>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun fromSampleEntity(value: SampleEntity?): String? {
        return value?.let { Json.encodeToString(SampleEntity.serializer(), it) }
    }

    @TypeConverter
    fun toSampleEntity(value: String?): SampleEntity? {
        return value?.let { Json.decodeFromString(SampleEntity.serializer(), it) }
    }
}
