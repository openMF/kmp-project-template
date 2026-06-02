/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.crypto.converter

import androidx.room3.TypeConverter
import kotlinx.serialization.json.Json

class FintechTypeConverters {

    @TypeConverter
    fun mapToString(map: Map<String, Double>): String = Json.encodeToString(map)

    @TypeConverter
    fun stringToMap(json: String): Map<String, Double> = Json.decodeFromString(json)

    @TypeConverter
    fun ratePointsToString(list: List<RatePointPair>): String = Json.encodeToString(list)

    @TypeConverter
    fun stringToRatePoints(json: String): List<RatePointPair> = Json.decodeFromString(json)
}

@kotlinx.serialization.Serializable
data class RatePointPair(
    val date: String,
    val value: Double,
)
