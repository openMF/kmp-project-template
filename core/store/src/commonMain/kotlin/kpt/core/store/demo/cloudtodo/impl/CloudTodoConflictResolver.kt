/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.cloudtodo.impl

import kpt.core.base.store.infra.ConflictStrategy
import kpt.core.model.demo.cloudtodo.CloudTodo

/**
 * The named conflict surface for the cloud-todo MUTABLE archetype (S5-CONFLICT).
 *
 * Store5's `Updater` does not consume a [ConflictStrategy] itself, so the policy has to be
 * applied by hand at the one point where BOTH the server echo and the client value exist —
 * inside `Updater.post`. Previously that meant a bare `ConflictStrategy.ClientWins()` literal
 * inline in [provideCloudTodoStore]: the policy worked, but it had no name, no test seam, and
 * no way for a caller to tell an ordinary write apart from one that actually diverged. This
 * resolver is that surface.
 *
 * [resolve] returns both the winning value AND whether the two sides had genuinely diverged,
 * so the caller can distinguish "server echoed what we sent" from "server had something else
 * and we overrode it" — the input a `MutationResult.Conflicted` outcome needs. Collapsing the
 * two is how a silent overwrite reaches the user as a plain success.
 */
class CloudTodoConflictResolver(
    private val strategy: ConflictStrategy<CloudTodo> = ConflictStrategy.ClientWins(),
) {

    /** Outcome of reconciling one server echo against the client value. */
    data class Resolution(
        /** The value to persist — whichever side [strategy] selected. */
        val value: CloudTodo,
        /** True when server and client genuinely disagreed on a user-visible field. */
        val diverged: Boolean,
    )

    fun resolve(server: CloudTodo, client: CloudTodo): Resolution = Resolution(
        value = strategy.resolve(server = server, client = client),
        diverged = server.completed != client.completed || server.title != client.title,
    )
}
