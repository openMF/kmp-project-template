/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.cmp.feature.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.coreBase.store)
            implementation(projects.coreBase.ui)
            // ScreenUiStateStore lives in core-base/datastore — the ViewModel
            // reads/writes the paging cursor here on top of Phase 2's
            // rememberRetainedScreenState (which owns scroll persistence).
            implementation(projects.coreBase.datastore)

            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended)
            // compose-resources — for stringResource()-based UI copy (i18n) per
            // RULE-IMPL-NO-HARDCODED-STRING-001 (W2 of store5-superbrain-v2).
            implementation(compose.components.resources)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            // kotlin.test + kotlinx.coroutines.test come from the convention plugin.
            implementation(libs.turbine)
            // NOTE: the full runComposeUiTest render test for RULE-KMP-COMPOSE-UITEST-001
            // is deferred until compose.uiTest is wired into commonTest across all 6
            // targets (no module in the fork does this yet). CoinMarketsScreenUiTest
            // enforces the append-only TestTags contract at compile time in the interim.
        }
    }
}

// Compose-resources class generator config — mirrors `feature/loans/build.gradle.kts`
// so `Res.string.*` is exposed under `kpt.feature.crypto.generated.resources`.
compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "kpt.feature.crypto.generated.resources"
}
