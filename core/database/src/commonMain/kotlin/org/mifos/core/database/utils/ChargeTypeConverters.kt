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
import androidx.room3.TypeConverter

/**
 * Room 3 [TypeConverter] collection for JSON-backed column types.
 *
 * Registered on [AppDatabase][org.mifos.core.database.AppDatabase] via
 * `@TypeConverters(ChargeTypeConverters::class)`. Each converter pair
 * serializes/deserializes a complex type to/from a JSON string column using
 * `kotlinx.serialization`.
 */
class ChargeTypeConverters {

    /** Deserializes a JSON string to a nullable-int list. */
    @TypeConverter
    fun fromIntList(value: String): ArrayList<Int?> {
        return Json.decodeFromString(value)
    }

    /** Serializes a nullable-int list to a JSON string. */
    @TypeConverter
    fun toIntList(list: ArrayList<Int?>): String {
        return Json.encodeToString(list)
    }

    /** Serializes a [SampleEntity] to a JSON string, or `null` if the entity is `null`. */
    @TypeConverter
    fun fromSampleEntity(value: SampleEntity?): String? {
        return value?.let { Json.encodeToString(SampleEntity.serializer(), it) }
    }

    /** Deserializes a JSON string to a [SampleEntity], or `null` if the string is `null`. */
    @TypeConverter
    fun toSampleEntity(value: String?): SampleEntity? {
        return value?.let { Json.decodeFromString(SampleEntity.serializer(), it) }
    }
}
