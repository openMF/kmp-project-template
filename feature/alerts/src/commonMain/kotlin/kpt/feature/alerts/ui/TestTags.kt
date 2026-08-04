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

/**
 * Append-only test-tag registry for the alerts feature.
 * Consumed by Compose UI tests in `feature/alerts/src/commonTest/`.
 * APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).
 */
object TestTags {

    /** Tags for [AlertsListScreen]. */
    object AlertsList {
        /** Root scaffold — always rendered regardless of load state. */
        const val SCREEN: String = "alerts_list_screen"

        /** FAB that routes to the create-alert form. */
        const val CREATE_FAB: String = "alerts_create_fab"

        /** Per-row delete button. Suffix with the alert id. */
        const val DELETE_PREFIX: String = "alerts_delete_"
    }

    /** Tags for [AlertCreateScreen]. */
    object AlertCreate {
        /** Root scaffold — always rendered regardless of load/submit state. */
        const val SCREEN: String = "alert_create_screen"

        /** Coin id text field. */
        const val COIN_FIELD: String = "alert_create_coin_field"

        /** Target price text field. */
        const val TARGET_FIELD: String = "alert_create_target_field"

        /** Submit button. */
        const val SUBMIT: String = "alert_create_submit"
    }
}
