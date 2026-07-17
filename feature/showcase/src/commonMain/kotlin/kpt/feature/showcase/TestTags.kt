/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.showcase

/**
 * Append-only test-tag registry for the showcase feature.
 * Consumed by Compose UI tests in `feature/showcase/src/commonTest/`.
 * APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).
 */
object TestTags {

    /** Tags for [kpt.feature.showcase.stategallery.StateGalleryScreen]. */
    object StateGallery {
        /** Root scaffold — always rendered. */
        const val SCREEN: String = "state_gallery_screen"
    }

    /** Tags for [kpt.feature.showcase.transitions.TransitionGalleryScreen]. */
    object TransitionGallery {
        /** Root scaffold — always rendered. */
        const val SCREEN: String = "transition_gallery_screen"
    }
}
