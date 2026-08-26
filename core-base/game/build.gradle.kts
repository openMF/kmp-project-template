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
    alias(libs.plugins.kmp.core.base.library.convention)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Brand-neutral 2D game engine primitives — Compose-Canvas only, ZERO third-party deps.
            implementation(compose.runtime)     // withFrameNanos, remember, State
            implementation(compose.foundation)  // Canvas, pointerInput gestures
            implementation(compose.ui)          // DrawScope, geometry, Color, Modifier
        }
    }
}
