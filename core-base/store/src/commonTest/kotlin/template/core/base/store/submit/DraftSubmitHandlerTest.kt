/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.store.submit

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import template.core.base.store.error.ErrorCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DraftSubmitHandlerTest {

    private val formKey = "test_form"

    // ─── T1: success path ────────────────────────────────────────────────────

    @Test
    fun `submit success transitions to Submitted and does not save draft`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        handler.submit("payload") { "ok" }
        testScheduler.advanceUntilIdle()

        assertIs<SubmitState.Submitted<String>>(handler.state.value)
        assertTrue(outbox.entries.isEmpty(), "No draft should be saved on success")
    }

    // ─── T2: network failure saves draft ─────────────────────────────────────

    @Test
    fun `submit network failure saves draft to outbox`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        class IOException(msg: String) : RuntimeException(msg)
        handler.submit("payload") { throw IOException("connect timed out") }
        testScheduler.advanceUntilIdle()

        val state = assertIs<SubmitState.Failed>(handler.state.value)
        assertEquals(ErrorCategory.Network, state.category)
        val draft = outbox.getPending(formKey)
        assertEquals("payload", draft?.payload)
    }

    // ─── T3: non-network failure does NOT save draft ──────────────────────────

    @Test
    fun `submit server failure does not save draft`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        handler.submit("payload") { throw RuntimeException("HTTP 500") }
        testScheduler.advanceUntilIdle()

        val state = assertIs<SubmitState.Failed>(handler.state.value)
        assertEquals(ErrorCategory.Server, state.category)
        assertNull(outbox.getPending(formKey), "Server errors must not create a draft")
    }

    // ─── T4: retry after failure re-executes block ───────────────────────────

    @Test
    fun `retry after network failure re-executes the submission block`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)
        var callCount = 0

        class IOException(msg: String) : RuntimeException(msg)
        handler.submit("payload") { callCount++; throw IOException("timeout") }
        testScheduler.advanceUntilIdle()

        handler.retry()
        testScheduler.advanceUntilIdle()

        assertEquals(2, callCount)
    }

    // ─── T5: submit while Submitting is no-op ────────────────────────────────

    @Test
    fun `submit while already Submitting is ignored`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)
        var callCount = 0

        handler.submit("p1") { callCount++; "ok" }
        handler.submit("p2") { callCount++; "ok" }  // should be ignored
        testScheduler.advanceUntilIdle()

        assertEquals(1, callCount, "Second submit while in-flight must be no-op")
    }

    // ─── T6: reset returns to Idle, draft preserved ──────────────────────────

    @Test
    fun `reset returns to Idle without removing outbox draft`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        class IOException(msg: String) : RuntimeException(msg)
        handler.submit("payload") { throw IOException("offline") }
        testScheduler.advanceUntilIdle()

        handler.reset()

        assertEquals(SubmitState.Idle, handler.state.value)
        assertEquals("payload", outbox.getPending(formKey)?.payload, "Draft must remain after reset")
    }

    // ─── T7: second submit idempotent — updates existing draft ───────────────

    @Test
    fun `second network failure updates existing draft instead of inserting new row`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        class IOException(msg: String) : RuntimeException(msg)
        handler.submit("payload_v1") { throw IOException("offline") }
        testScheduler.advanceUntilIdle()

        handler.submit("payload_v2") { throw IOException("still offline") }
        testScheduler.advanceUntilIdle()

        val pending = outbox.getAllPending()
        assertEquals(1, pending.size, "Idempotent upsert must not create duplicate rows")
        assertEquals("payload_v2", pending.first().payload)
    }

    // ─── T8: success after prior draft marks draft submitted ─────────────────

    @Test
    fun `success after saved draft marks the draft as SUBMITTED`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        class IOException(msg: String) : RuntimeException(msg)
        handler.submit("payload") { throw IOException("offline") }
        testScheduler.advanceUntilIdle()

        handler.retry()
        testScheduler.advanceUntilIdle()

        handler.submit("payload") { "ok" }
        testScheduler.advanceUntilIdle()

        assertIs<SubmitState.Submitted<String>>(handler.state.value)
        val entry = outbox.entries.firstOrNull { it.formKey == formKey }
        assertEquals(SubmitOutboxStatus.SUBMITTED, entry?.status)
    }
}
