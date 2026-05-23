/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

/* =================================================================================
 *  DO NOT EDIT to add consumer-specific flavors. This file is SYNCED from
 *  kmp-project-template into every downstream consumer app via sync-dirs.sh.
 *  Edits here will be overwritten on the next sync.
 *
 *  To add consumer-specific flavors / dimensions / overrides, create:
 *      build-logic/convention/src/main/kotlin/local/LocalFlavors.kt
 *
 *  That local/ directory is excluded from sync-dirs.sh and survives every sync.
 *  See docs/FLAVORS_EXTENSION.md.
 * ================================================================================= */

import com.android.build.api.dsl.CommonExtension
import com.mobilebytelabs.kmpflavors.KmpFlavorExtension
import com.mobilebytelabs.kmpflavors.KmpFlavorPlugin
import org.convention.configureFlavors
import org.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention plugin that wires `kmp-product-flavors` with the BASE flavor contract
 * every downstream consumer app inherits:
 *
 * - `tier`: `demo` (default — demo credentials for testers / public demo APK) and
 *   `prod` (real customers).
 * - `buildType`: `debug` (default — debuggable), `staging`, `release`.
 *
 * ## AGP bridge
 *
 * `bridgeAgpProductFlavors` and `bridgeAgpBuildTypes` default to `true` in the plugin
 * and use `androidComponents.finalizeDsl` — the correct AGP lifecycle hook (runs after
 * `kmpFlavors {}` DSL evaluation, before AGP variant resolution). No manual
 * `android { productFlavors {} }` block is needed in build-logic.
 *
 * Consumer apps extend this contract by creating
 * `build-logic/convention/src/main/kotlin/local/LocalFlavors.kt` — see
 * [LocalFlavorsLoader] for the hook and `docs/FLAVORS_EXTENSION.md` for examples.
 */
class KMPFlavorsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // 1. Apply the upstream plugin (provides KmpFlavorExtension + codegen + source-set wiring
            //    + AGP bridge via androidComponents.finalizeDsl).
            pluginManager.apply(KmpFlavorPlugin::class.java)

            // 2. Configure the KMP-side flavor contract.
            //    buildConfigPackage comes from gradle/libs.versions.toml ([versions].appPackage)
            //    so forks change the brand by editing ONE line.
            extensions.configure<KmpFlavorExtension> {
                buildConfigPackage.set(libs.findVersion("appPackage").get().requiredVersion)
                enableBuildTypes.set(true)

                flavorDimensions {
                    register("contentType") { priority.set(0) }
                }

                flavors {
                    register("demo") {
                        dimension.set("contentType")
                        isDefault.set(true)
                        applicationIdSuffix.set(".demo")
                        bundleIdSuffix.set(".demo")
                        buildConfigField("Boolean", "IS_DEMO_BUILD", "true")
                        buildConfigField("String", "BASE_URL", "\"https://demo.openmf.org\"")
                        buildConfigField("String", "DEMO_USERNAME", "\"demo\"")
                        buildConfigField("String", "DEMO_PASSWORD", "\"demo\"")
                    }
                    register("prod") {
                        dimension.set("contentType")
                        buildConfigField("Boolean", "IS_DEMO_BUILD", "false")
                        buildConfigField("String", "BASE_URL", "\"https://api.openmf.org\"")
                        buildConfigField("String", "DEMO_USERNAME", "\"\"")
                        buildConfigField("String", "DEMO_PASSWORD", "\"\"")
                    }
                }

                buildTypes {
                    register("debug") {
                        isDefault.set(true)
                        isDebuggable.set(true)
                        applicationIdSuffix.set(".debug")
                        buildConfigField("Boolean", "ENABLE_LOGGING", "true")
                        buildConfigField("Boolean", "SHOW_DEBUG_OVERLAY", "true")
                        buildConfigField("String", "LOG_TAG", "\"KMPTemplate-DEBUG\"")
                    }
                    register("staging") {
                        isDebuggable.set(false)
                        applicationIdSuffix.set(".staging")
                        buildConfigField("Boolean", "ENABLE_LOGGING", "true")
                        buildConfigField("Boolean", "SHOW_DEBUG_OVERLAY", "false")
                        buildConfigField("String", "LOG_TAG", "\"KMPTemplate-STAGING\"")
                    }
                    register("release") {
                        isDebuggable.set(false)
                        isMinifyEnabled.set(true)
                        buildConfigField("Boolean", "ENABLE_LOGGING", "false")
                        buildConfigField("Boolean", "SHOW_DEBUG_OVERLAY", "false")
                        buildConfigField("String", "LOG_TAG", "\"KMPTemplate\"")
                    }
                }

                // Consumer extension hook — must be the LAST statement so the
                // local file sees the fully-populated extension.
                LocalFlavorsLoader.applyIfPresent(this, target)
            }

            // AGP-side registration for pure Android modules (e.g. cmp-android).
            //
            // KmpFlavorPlugin requires KotlinMultiplatformExtension. When that is not
            // present (com.android.application modules without kotlin("multiplatform")),
            // the plugin returns early and its built-in bridgeAgpProductFlavors never
            // fires. We register the same demo/prod dimensions + flavors synchronously
            // here via pluginManager.withPlugin so AGP receives them before variant
            // resolution.
            //
            // For KMP library modules the plugin's own androidComponents.finalizeDsl
            // bridge handles registration — configureFlavors() is idempotent and skips
            // any flavor already present, so calling it here is safe for those modules
            // too.
            listOf("com.android.application", "com.android.library").forEach { agpId ->
                pluginManager.withPlugin(agpId) {
                    extensions.findByType(CommonExtension::class.java)
                        ?.let { configureFlavors(it) }
                }
            }
        }
    }
}
