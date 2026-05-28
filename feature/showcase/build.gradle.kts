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

android {
    namespace = "org.mifos.feature.showcase"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // State-gallery deps (Phase 02): need core/store for the ScreenState model
            // (the convention plugin auto-includes core:ui, core-base:ui, core:designsystem,
            // core-base:designsystem; it does NOT include core:store).
            implementation(projects.core.store)

            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended)
        }
    }
}
