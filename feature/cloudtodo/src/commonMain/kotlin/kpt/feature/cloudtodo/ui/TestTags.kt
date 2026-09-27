/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.cloudtodo.ui

/**
 * Append-only test-tag registry for the cloud-todo feature.
 * Consumed by Compose UI tests in `feature/cloudtodo/src/commonTest/`.
 * APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).
 *
 * Tags mirror `idea-layer/screens/cloudtodo/ui.yaml#test_tags`.
 */
object TestTags {

    /** Tags for [kpt.feature.cloudtodo.ui.CloudTodoScreen]. */
    object CloudTodo {
        /** The screen root. A Maestro flow waits on this before asserting anything else on the screen. */
        const val SCREEN: String = "cloudtodo_detail_screen"

        // No APP_BAR tag: KptScaffold owns the navigation icon and exposes no parameter to tag
        // it, so such a tag could never resolve in a test. Declaring it would be dead weight.
        /** The todo summary block. */
        const val SUMMARY: String = "cloudtodo_summary"

        /**
         * The optimistic-toggle control — the one that writes locally first and syncs after, so it succeeds offline.
         */
        const val TOGGLE_OPTIMISTIC: String = "cloudtodo_toggle_optimistic"

        /** The network-required complete control. */
        const val COMPLETE_ONLINE: String = "cloudtodo_complete_online"

        /**
         * The outcome banner, present only after a mutation resolves. Its absence is what a test asserts to prove a
         * write is still in flight.
         */
        const val OUTCOME: String = "cloudtodo_outcome"

        /** The banner's dismiss control. */
        const val OUTCOME_DISMISS: String = "cloudtodo_outcome_dismiss"

        /** Its resolve-conflict control. */
        const val OUTCOME_RESOLVE: String = "cloudtodo_outcome_resolve"
    }

    // NO ConflictInbox tags here — conflict resolution is the shipped feature/settings
    // SyncAndDraftsScreen, which owns its own tags. This feature only navigates to it.
}
