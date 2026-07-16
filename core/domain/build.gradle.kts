/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.kmp.library.convention)
    // NOT applying kmp.library.iosx64.convention: depends on core:data, which is itself excluded
    // (transitively depends on Compose Multiplatform modules — see
    // KMPLibraryIosX64ConventionPlugin kdoc).
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            api(projects.core.data)
            api(projects.core.model)
        }
    }
}