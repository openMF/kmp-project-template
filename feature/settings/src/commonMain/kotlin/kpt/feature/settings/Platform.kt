/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

/**
 * Which platform the app is running on, for the About screen and for a platform-conditional branch that cannot be
 * expressed as an `expect`/`actual` of its own.
 */
expect fun getPlatform(): Platform

/** The targets this template builds for. */
enum class Platform {
    /** Android. The only target where `supportsDynamicTheming` can return true. */
    Android,

    /** Desktop JVM — Windows, macOS and Linux all report this; the OS is not distinguished here. */
    Desktop,

    /** iOS. Also what macOS native reports, since both build from the same source set. */
    IOS,

    /** Kotlin/JS browser. */
    JS,

    /** Kotlin/Wasm browser. */
    Wasm,
}

/**
 * Whether the platform exposes a system-derived palette, so the settings screen can hide the dynamic-colour toggle
 * where it would do nothing.
 */
expect fun supportsDynamicTheming(): Boolean
