/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loans.ui

/**
 * Append-only test-tag registry for the personal-loans feature.
 *
 * Consumed by:
 *  - Compose UI tests in `feature/loans/src/commonTest/`
 *    (`PersonalLoansListScreenUiTest`, `LoansJourneyUiTest`).
 *  - Maestro on-device flows under `maestro/screen-state/` — Maestro
 *    resolves these string constants via `assertVisible: { id: "..." }`
 *    matchers on Android's `Modifier.testTag(...)` semantics.
 *
 * **APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).** Removing or
 * renaming any string breaks the build (test-side compile reference) and the
 * on-device Maestro flows in the same commit. Add new tags at the bottom.
 * If a tag genuinely must be retired, add a sibling `// uitest-tag-retire:`
 * annotation and land the source deletion in the same commit as the
 * matching Maestro / UI-test callers.
 */
object TestTags {

    /** Tags for [PersonalLoansListScreen]. */
    object LoansList {
        /** The scrollable [LazyColumn] hosting the loan rows. */
        const val LIST: String = "loans_list"

        /**
         * Per-row tag prefix. Full tag = `"${ROW}_${loan.id}"` — allowing
         * Maestro / Compose tests to pick a specific row without needing a
         * synthetic label.
         */
        const val ROW: String = "loans_list_row"

        /** Extended FAB that opens the "New loan" screen. */
        const val FAB: String = "loans_list_fab"

        /** Confirm button in the delete-loan alert dialog. */
        const val DELETE_CONFIRM: String = "loans_list_delete_confirm"
    }
}
