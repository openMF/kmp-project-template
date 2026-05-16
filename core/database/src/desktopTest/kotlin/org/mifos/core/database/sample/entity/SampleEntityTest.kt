/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.sample.entity

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SampleEntityTest {

    @Test
    fun defaultValuesIdIsZeroAndNameIsEmpty() {
        val entity = SampleEntity()
        assertEquals(0, entity.id)
        assertEquals("", entity.name)
    }

    @Test
    fun serializationRoundTripPreservesData() {
        val original = SampleEntity(id = 42, name = "Test")
        val json = Json.encodeToString(SampleEntity.serializer(), original)
        val deserialized = Json.decodeFromString(SampleEntity.serializer(), json)
        assertEquals(original, deserialized)
    }

    @Test
    fun serializationJsonFormatIsCorrect() {
        val entity = SampleEntity(id = 1, name = "Sample")
        val json = Json.encodeToString(SampleEntity.serializer(), entity)
        assertEquals("""{"id":1,"name":"Sample"}""", json)
    }

    @Test
    fun copyUpdatesSpecifiedFields() {
        val original = SampleEntity(id = 1, name = "Original")
        val copied = original.copy(name = "Updated")
        assertEquals(1, copied.id)
        assertEquals("Updated", copied.name)
    }

    @Test
    fun equalitySameValuesAreEqual() {
        val a = SampleEntity(id = 1, name = "Test")
        val b = SampleEntity(id = 1, name = "Test")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }
}
