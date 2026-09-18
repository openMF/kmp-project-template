/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.currency

import kpt.core.database.currency.converter.ChargeTypeConverters
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
