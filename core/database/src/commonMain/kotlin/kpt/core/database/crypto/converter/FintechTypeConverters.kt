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

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.json.Json

class FintechTypeConverters {

    @ColumnTypeConverter
    fun mapToString(map: Map<String, Double>): String = Json.encodeToString(map)

    @ColumnTypeConverter
    fun stringToMap(json: String): Map<String, Double> = Json.decodeFromString(json)

    @ColumnTypeConverter
    fun ratePointsToString(list: List<RatePointPair>): String = Json.encodeToString(list)

    @ColumnTypeConverter
    fun stringToRatePoints(json: String): List<RatePointPair> = Json.decodeFromString(json)
}

@kotlinx.serialization.Serializable
data class RatePointPair(
    val date: String,
    val value: Double,
)
