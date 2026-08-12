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
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.core.store)
            implementation(projects.core.ui)

            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
        }

        commonTest.dependencies {
            implementation(libs.turbine)
        }
    }
}

// Compose-resources class generator config — mirrors `feature/watchlist/build.gradle.kts`
// so `Res.string.*` is exposed under `kpt.feature.addtowatchlist.generated.resources`.
compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "kpt.feature.addtowatchlist.generated.resources"
}
