/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.currency

import org.mifos.core.database.infra.entity.SampleEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChargeTypeConvertersTest {

    private val converters = ChargeTypeConverters()

    @Test
    fun intListRoundTripPreservesData() {
        val original = arrayListOf<Int?>(1, 2, 3, null, 5)
        val json = converters.toIntList(original)
        val restored = converters.fromIntList(json)
        assertEquals(original, restored)
    }

    @Test
    fun intListEmptyListRoundTrips() {
        val original = arrayListOf<Int?>()
        val json = converters.toIntList(original)
        val restored = converters.fromIntList(json)
        assertEquals(original, restored)
    }

    @Test
    fun intListAllNullsRoundTrips() {
        val original = arrayListOf<Int?>(null, null, null)
        val json = converters.toIntList(original)
        val restored = converters.fromIntList(json)
        assertEquals(original, restored)
    }

    @Test
    fun sampleEntityRoundTripPreservesData() {
        val original = SampleEntity(id = 42, name = "Test Entity")
        val json = converters.fromSampleEntity(original)
        val restored = converters.toSampleEntity(json)
        assertEquals(original, restored)
    }

    @Test
    fun sampleEntityNullInputReturnsNull() {
        assertNull(converters.fromSampleEntity(null))
        assertNull(converters.toSampleEntity(null))
    }

    @Test
    fun sampleEntityDefaultValuesRoundTrips() {
        val original = SampleEntity()
        val json = converters.fromSampleEntity(original)
        val restored = converters.toSampleEntity(json)
        assertEquals(original, restored)
    }
}
