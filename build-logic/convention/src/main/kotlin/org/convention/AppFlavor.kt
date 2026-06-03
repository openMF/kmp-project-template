/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.CommonExtension
import org.gradle.kotlin.dsl.invoke

/**
 * AGP-side flavor registration helper for modules that do NOT apply
 * `kotlin("multiplatform")` — specifically `cmp-android`.
 *
 * `KmpFlavorPlugin` requires `KotlinMultiplatformExtension` to be present.
 * When it is not (pure `com.android.application` modules), the plugin returns
 * early and its built-in `bridgeAgpProductFlavors` never runs. This helper is
 * called synchronously from [KMPFlavorsConventionPlugin] via
 * `pluginManager.withPlugin("com.android.application")` so AGP receives the
 * flavor registration before variant resolution.
 *
 * For KMP library modules (which DO have `kotlin("multiplatform")`) the plugin's
 * own bridge via `androidComponents.finalizeDsl` handles everything — this
 * helper is skipped for those modules via the `findByType(CommonExtension)` null
 * guard in [KMPFlavorsConventionPlugin].
 *
 * This file is intentionally kept minimal — it mirrors only the dimensions and
 * flavors declared in [KMPFlavorsConventionPlugin]'s `kmpFlavors { }` block.
 * Keep both in sync when adding new flavors.
 */

@Suppress("EnumEntryName")
enum class FlavorDimension { contentType }

@Suppress("EnumEntryName")
enum class AppFlavor(val dimension: FlavorDimension, val applicationIdSuffix: String? = null) {
    demo(FlavorDimension.contentType, applicationIdSuffix = ".demo"),
    prod(FlavorDimension.contentType),
}

/**
 * Registers base `demo`/`prod` AGP product flavors on [commonExtension].
 * Idempotent — skips any flavor or dimension already present.
 */
fun configureFlavors(commonExtension: CommonExtension) {
    commonExtension.apply {
        if (FlavorDimension.contentType.name !in flavorDimensions) {
            (flavorDimensions as MutableList<String>).add(FlavorDimension.contentType.name)
        }
        productFlavors {
            AppFlavor.values().forEach { flv ->
                if (findByName(flv.name) == null) {
                    create(flv.name) {
                        dimension = flv.dimension.name
                        if (this@apply is ApplicationExtension && this is ApplicationProductFlavor) {
                            flv.applicationIdSuffix?.let { applicationIdSuffix = it }
                        }
                    }
                }
            }
        }
    }
}
