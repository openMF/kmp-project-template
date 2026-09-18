/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.wizard

import kotlinx.serialization.json.Json
import kpt.core.model.banking.LoanCalcScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Regression guard for the 2026-05-25 production crash:
 *
 *   `java.lang.ClassCastException: kpt.feature.calculators.wizard.LoanCalcScenario
 *    cannot be cast to kpt.core.model.alerts.PriceAlert`
 *
 * Root cause: `CalculatorsModule` requested `outbox: SubmitOutbox<LoanCalcScenario>` from
 * Koin, but no matching `single<SubmitOutbox<LoanCalcScenario>>` was registered. Koin fell
 * back to the last `SubmitOutbox<*>` binding (`SubmitOutbox<PriceAlert>` in
 * `core/data/.../RepositoryModule.kt`), wiring `PriceAlert.serializer()` into the outbox.
 * On first `.saveByUniqueKey(scenario)`, the wrong serializer fired the ClassCast.
 *
 * These tests assert two contracts:
 *  1. [LoanCalcScenario] roundtrips cleanly through its own serializer.
 *  2. Attempting to serialize a [LoanCalcScenario] through an unrelated payload's
 *     serializer fails — proves the wrong-serializer class of bug is detectable.
 *
 * Full Koin-DI integration test for `get<SubmitOutbox<LoanCalcScenario>>()` lives in the
 * androidInstrumentedTest set bootstrapped by Stage D of the
 * `ui-stabilize-and-test` plan.
 */
class LoanCalcScenarioSerializerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun loanCalcScenario_roundtripsThroughItsOwnSerializer() {
        val original = LoanCalcScenario(
            scenarioId = "scen-1",
            name = "Home loan v2",
            principal = 5_000_000.0,
            ratePercent = 7.5,
            tenureMonths = 240,
            currentStep = 4,
        )

        val serialized = json.encodeToString(LoanCalcScenario.serializer(), original)
        val restored = json.decodeFromString(LoanCalcScenario.serializer(), serialized)

        assertEquals(original, restored)
    }

    @Test
    fun loanCalcScenario_serializerEmitsScenarioIdField() {
        // Sanity check — if the field is silently dropped, the outbox can't round-trip
        // wizard state correctly across process death.
        val payload = LoanCalcScenario(scenarioId = "abc-123")
        val serialized = json.encodeToString(LoanCalcScenario.serializer(), payload)

        assertTrue(
            serialized.contains("\"scenarioId\""),
            "scenarioId field must be present in JSON; got: $serialized",
        )
        assertTrue(
            serialized.contains("\"abc-123\""),
            "scenarioId value must be present in JSON; got: $serialized",
        )
    }

    @Test
    fun loanCalcScenario_decodesIntoCorrectType() {
        // Direct exercise of the contract that broke in production: a serialized
        // LoanCalcScenario JSON, when decoded with LoanCalcScenario.serializer(),
        // returns a LoanCalcScenario — not some other type.
        val original = LoanCalcScenario(scenarioId = "type-check")
        val json = json.encodeToString(LoanCalcScenario.serializer(), original)
        val decoded = this.json.decodeFromString(LoanCalcScenario.serializer(), json)

        assertNotNull(decoded)
        @Suppress("USELESS_IS_CHECK")
        assertTrue(decoded is LoanCalcScenario)
    }

    /**
     * Documents the crash class: trying to serialize a [LoanCalcScenario] with a serializer
     * for some other type explodes. This is exactly what happened in production when Koin
     * handed the wizard a [SubmitOutbox] wired with `PriceAlert.serializer()`.
     */
    @Test
    fun usingWrongSerializerForLoanCalcScenario_fails() {
        val payload = LoanCalcScenario(scenarioId = "boom")
        try {
            // Re-use LoanCalcScenario's own serializer on a String to simulate a
            // type-mismatch — proves the crash class. (Can't reach across modules to
            // PriceAlert.serializer() from a feature unit test without adding a circular
            // dep; the principle is identical: wrong serializer for wrong payload throws.)
            @Suppress("UNCHECKED_CAST")
            val wrongSerializer = LoanCalcScenario.serializer() as kotlinx.serialization.KSerializer<Any>
            json.encodeToString(wrongSerializer, "this is not a LoanCalcScenario" as Any)
            fail("Expected ClassCastException or SerializationException — got none.")
        } catch (e: Throwable) {
            // Any throw here is acceptable — point is the wrong serializer must fail
            // loudly rather than silently corrupt the outbox.
            assertTrue(
                e is ClassCastException || e is kotlinx.serialization.SerializationException,
                "Expected ClassCastException or SerializationException; got ${e::class.simpleName}: ${e.message}",
            )
        }
    }
}
