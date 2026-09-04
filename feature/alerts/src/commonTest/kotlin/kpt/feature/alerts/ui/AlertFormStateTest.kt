/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.alerts.ui

import kpt.core.model.demo.alerts.AlertDirection
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Locks [AlertFormState.canSubmit] — the create-alert submit gate.
 *
 * `feature/alerts` had NO test source set at all, despite being a write path: it is the only
 * store-backed feature that shipped with zero tests. `canSubmit` is the guard that decides whether a
 * price alert can be persisted, so its boundaries (blank coin, unparseable target, zero, negative)
 * are exactly where a bad row reaches the database.
 */
class AlertFormStateTest {

    private fun form(coin: String = "btc", target: String = "100") =
        AlertFormState(coinId = coin, direction = AlertDirection.ABOVE, targetValueText = target)

    @Test
    fun defaultFormCannotSubmit() {
        assertFalse(AlertFormState().canSubmit, "an untouched form must not be submittable")
    }

    @Test
    fun validCoinAndPositiveTargetCanSubmit() {
        assertTrue(form().canSubmit)
    }

    @Test
    fun blankCoinIdBlocksSubmit() {
        assertFalse(form(coin = "").canSubmit)
        assertFalse(form(coin = "   ").canSubmit, "whitespace is not a coin id")
    }

    @Test
    fun unparseableTargetBlocksSubmit() {
        // Mid-type states must not be submittable — the field is free text.
        assertFalse(form(target = "").canSubmit)
        assertFalse(form(target = "abc").canSubmit)
        assertFalse(form(target = "1.2.3").canSubmit)
        assertFalse(form(target = "-").canSubmit)
    }

    @Test
    fun zeroOrNegativeTargetBlocksSubmit() {
        // A price alert at <= 0 can never meaningfully fire.
        assertFalse(form(target = "0").canSubmit)
        assertFalse(form(target = "0.0").canSubmit)
        assertFalse(form(target = "-5").canSubmit)
    }

    @Test
    fun fractionalAndLargeTargetsAreAccepted() {
        assertTrue(form(target = "0.00001").canSubmit, "sub-cent alerts are valid for cheap coins")
        assertTrue(form(target = "1000000").canSubmit)
    }

    @Test
    fun directionDoesNotAffectSubmitability() {
        // Both directions are always valid — only coin + target gate the submit.
        assertTrue(form().copy(direction = AlertDirection.BELOW).canSubmit)
        assertTrue(form().copy(direction = AlertDirection.ABOVE).canSubmit)
    }
}
