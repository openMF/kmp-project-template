/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.currency.converter

import androidx.room3.TypeConverter
import co.touchlab.kermit.Logger
import kotlinx.serialization.json.Json
import org.mifos.core.database.sample.entity.SampleEntity
import template.core.base.security.FieldEncryptor

private const val ENC_PREFIX = "ENC:"

/**
 * Room 3 [TypeConverter] collection for JSON-backed column types.
 *
 * Supports optional field-level encryption via [FieldEncryptor]. When an encryptor
 * is installed via [install], JSON strings are encrypted before storage and prefixed
 * with "ENC:" for format detection. Unencrypted (legacy) values are read transparently,
 * enabling crash-safe encrypt-on-write migration.
 *
 * Room 3 KMP instantiates converters via no-arg constructor. The [FieldEncryptor]
 * is injected post-construction through the [Companion.install] method, which must
 * be called before the first database operation.
 */
class ChargeTypeConverters {

    companion object {
        @kotlin.concurrent.Volatile
        private var encryptor: FieldEncryptor? = null

        /**
         * Install a [FieldEncryptor] for all converter instances.
         * Call once during app initialization, before any database access.
         */
        fun install(fieldEncryptor: FieldEncryptor) {
            encryptor = fieldEncryptor
        }
    }

    private fun encryptString(value: String): String {
        val enc = encryptor ?: return value
        return try {
            ENC_PREFIX + enc.encrypt(value)
        } catch (e: Exception) {
            Logger.d("ChargeTypeConverters") { "Encryption failed, storing plaintext: ${e.message}" }
            value
        }
    }

    private fun decryptString(value: String): String {
        if (!value.startsWith(ENC_PREFIX)) return value
        val raw = value.removePrefix(ENC_PREFIX)
        return encryptor?.runCatching { decrypt(raw) }
            ?.onFailure { Logger.d("ChargeTypeConverters") { "Decryption failed: ${it.message}" } }
            ?.getOrDefault(raw)
            ?: raw
    }

    /** Deserializes a JSON string to a nullable-int list. */
    @TypeConverter
    fun fromIntList(value: String): ArrayList<Int?> {
        return try {
            Json.decodeFromString(decryptString(value))
        } catch (e: Exception) {
            Logger.d("ChargeTypeConverters") { "Failed to decode int list: ${e.message}" }
            arrayListOf()
        }
    }

    /** Serializes a nullable-int list to a JSON string. */
    @TypeConverter
    fun toIntList(list: ArrayList<Int?>): String {
        return encryptString(Json.encodeToString(list))
    }

    /** Serializes a [SampleEntity] to a JSON string, or `null` if the entity is `null`. */
    @TypeConverter
    fun fromSampleEntity(value: SampleEntity?): String? {
        return value?.let { encryptString(Json.encodeToString(SampleEntity.serializer(), it)) }
    }

    /** Deserializes a JSON string to a [SampleEntity], or `null` if the string is `null`. */
    @TypeConverter
    fun toSampleEntity(value: String?): SampleEntity? {
        return value?.let {
            try {
                Json.decodeFromString(SampleEntity.serializer(), decryptString(it))
            } catch (e: Exception) {
                Logger.d("ChargeTypeConverters") { "Failed to decode SampleEntity: ${e.message}" }
                null
            }
        }
    }
}
