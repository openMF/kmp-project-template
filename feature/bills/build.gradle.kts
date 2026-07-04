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
            implementation(projects.core.common)
            implementation(projects.core.domain)
            implementation(projects.core.ui)
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.core.platform)
            implementation(projects.coreBase.store)
            implementation(projects.coreBase.platform)
            implementation(projects.coreBase.ui)

            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended)
            // compose-resources — for stringResource()-based UI copy (i18n) per
            // RULE-IMPL-NO-HARDCODED-STRING-001 (W2 of store5-superbrain-v2).
            implementation(compose.components.resources)

            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

// Compose-resources class generator config — exposes `Res.string.*` under
// `kpt.feature.bills.generated.resources` for the i18n backfill
// (RULE-IMPL-NO-HARDCODED-STRING-001 / W2 store5-superbrain-v2).
compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "kpt.feature.bills.generated.resources"
}
