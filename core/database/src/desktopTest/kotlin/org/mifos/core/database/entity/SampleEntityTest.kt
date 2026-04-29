/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.entity

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SampleEntityTest {

    @Test
    fun defaultValues_idIsZeroAndNameIsEmpty() {
        val entity = SampleEntity()
        assertEquals(0, entity.id)
        assertEquals("", entity.name)
    }

    @Test
    fun serialization_roundTrip_preservesData() {
        val original = SampleEntity(id = 42, name = "Test")
        val json = Json.encodeToString(SampleEntity.serializer(), original)
        val deserialized = Json.decodeFromString(SampleEntity.serializer(), json)
        assertEquals(original, deserialized)
    }

    @Test
    fun serialization_jsonFormat_isCorrect() {
        val entity = SampleEntity(id = 1, name = "Sample")
        val json = Json.encodeToString(SampleEntity.serializer(), entity)
        assertEquals("""{"id":1,"name":"Sample"}""", json)
    }

    @Test
    fun copy_updatesSpecifiedFields() {
        val original = SampleEntity(id = 1, name = "Original")
        val copied = original.copy(name = "Updated")
        assertEquals(1, copied.id)
        assertEquals("Updated", copied.name)
    }

    @Test
    fun equality_sameValues_areEqual() {
        val a = SampleEntity(id = 1, name = "Test")
        val b = SampleEntity(id = 1, name = "Test")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }
}
