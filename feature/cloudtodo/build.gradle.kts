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
            // core/store carries AppScreenStateDefaults + AppErrorMapper; core-base/store carries the
            // MutationResult / BlockReason types this screen renders exhaustively.
            implementation(projects.core.store)

            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended)
            // compose-resources — stringResource()-based UI copy (i18n) per
            // RULE-IMPL-NO-HARDCODED-STRING-001.
            implementation(compose.components.resources)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

// Compose-resources class generator config — exposes `Res.string.*` under
// `kpt.feature.cloudtodo.generated.resources` (RULE-IMPL-NO-HARDCODED-STRING-001).
compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "kpt.feature.cloudtodo.generated.resources"
}
