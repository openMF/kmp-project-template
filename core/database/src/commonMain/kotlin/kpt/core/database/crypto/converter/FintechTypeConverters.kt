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
import kpt.core.base.database.annotation.DbConverters

/**
 * Room type converters for the composite values these tables store (rate-point lists, maps).
 *
 * Carries `@DbConverters` so `tools/database-ksp` registers it in the generated `@Database` — a
 * converter set without that annotation compiles and then fails at first query.
 */
@DbConverters
class FintechTypeConverters {

    /** Stores a code→rate map as JSON. */
    @ColumnTypeConverter
    fun mapToString(map: Map<String, Double>): String = Json.encodeToString(map)

    /** Reads a code→rate map back. */
    @ColumnTypeConverter
    fun stringToMap(json: String): Map<String, Double> = Json.decodeFromString(json)

    /** Stores a rate series as JSON. */
    @ColumnTypeConverter
    fun ratePointsToString(list: List<RatePointPair>): String = Json.encodeToString(list)

    /** Reads a rate series back. */
    @ColumnTypeConverter
    fun stringToRatePoints(json: String): List<RatePointPair> = Json.decodeFromString(json)
}

/**
 * Serialisable (date, value) pair — the wire form a rate-point list is stored as.
 */
@kotlinx.serialization.Serializable
data class RatePointPair(
    /** The sample's day, `YYYY-MM-DD`. */
    val date: String,
    /** The rate on that day. */
    val value: Double,
)
