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

import com.mobilebytelabs.kmpflavors.KmpFlavorExtension
import com.mobilebytelabs.kmpflavors.KmpFlavorPlugin
import org.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

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
 * and use `AgpProductFlavorRegistrar` (hooked via `pluginManager.withPlugin`) — the
 * correct AGP lifecycle hook (fires synchronously before AGP's afterEvaluate). No manual
 * `android { productFlavors {} }` block is needed in build-logic, including for pure
 * `com.android.application` modules that do not apply `kotlin("multiplatform")`.
 *
 * Consumer apps extend this contract by creating
 * `build-logic/convention/src/main/kotlin/local/LocalFlavors.kt` — see
 * [LocalFlavorsLoader] for the hook and `docs/FLAVORS_EXTENSION.md` for examples.
 */
class KMPFlavorsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // 1. Apply the upstream plugin (provides KmpFlavorExtension + codegen + source-set wiring
            //    + AGP bridge via AgpProductFlavorRegistrar.whenObjectAdded).
            pluginManager.apply(KmpFlavorPlugin::class.java)

            // 2. Configure the KMP-side flavor contract.
            //    buildConfigPackage comes from gradle/libs.versions.toml ([versions].appId)
            //    so forks change the brand by editing ONE line.
            extensions.configure<KmpFlavorExtension> {
                buildConfigPackage.set(libs.findVersion("appId").get().requiredVersion)
                // App identity for KmpFlavorsRuntime — single-sourced from libs.versions.toml
                // so forks rebrand by editing one line. appId gets the active flavor's id
                // suffix appended; appDisplayName is the human-facing name.
                appId.set(libs.findVersion("appId").get().requiredVersion)
                appDisplayName.set(libs.findVersion("appDisplayName").get().requiredVersion)
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
                        desktopWindowTitleSuffix.set(" (Demo)")
                        webTitleSuffix.set(" (Demo)")
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

            // 3. Register xcconfig generator. Reads the live DSL at execution time so
            //    adding / renaming / removing a flavor automatically updates xcconfigs
            //    on the next iOS build — no manual sync step required.
            registerIosFlavorXcconfigsTask()

            // 4. Register `exportKmpFlavorsManifest` — the consumer-side gradle task
            //    that materialises the DSL into `build/kmp-flavors/variants.json`, the
            //    single machine-readable derivation point consumed by fastlane, the
            //    `resolve-variants` GHA job, and the `openMF/mifos-x-actionhub`
            //    reusable workflow (epic `deploy-gha-product-flavors`, decisions D8/D9).
            //    Reads the live DSL at execution time so adding / renaming / removing a
            //    flavor auto-propagates to every CI stage on the next `resolve-variants`
            //    invocation — no committed derived list, no hand-maintained catalog,
            //    zero drift surface. Zero change to `build-logic/flavor-plugin/**`.
            registerExportKmpFlavorsManifestTask()
        }
    }

    private fun Project.registerIosFlavorXcconfigsTask() {
        val ext = extensions.getByType<KmpFlavorExtension>()

        val generateTask = tasks.register<GenerateIosFlavorXcconfigsTask>("generateIosFlavorXcconfigs") {
            group       = "kmp-flavors"
            description = "Auto-generate cmp-ios/Configs/{variant}.xcconfig from kmpFlavors DSL."

            // Lazy providers — Gradle reads these at execution time after DSL is fully
            // configured (including LocalFlavors.kt additions).
            flavorBundleSuffixes.set(provider {
                ext.flavors.associate { f -> f.name to (f.bundleIdSuffix.orNull ?: "") }
            })
            buildTypeBundleSuffixes.set(provider {
                ext.buildTypes.associate { bt -> bt.name to (bt.bundleIdSuffix.orNull ?: "") }
            })
            // Relative to :cmp-shared — cmp-ios is a sibling module.
            outputDir.set(layout.projectDirectory.dir("../cmp-ios/Configs"))
        }

        // Wire to iOS framework compilation so xcconfigs are always fresh before
        // Xcode links the KMP framework (covers both Xcode-triggered and CLI builds).
        tasks.configureEach {
            if (name == "embedAndSignAppleFrameworkForXcode" ||
                (name.startsWith("link") && name.contains("Framework") && name.contains("Ios"))) {
                dependsOn(generateTask)
            }
        }
    }

    /**
     * Registers the `exportKmpFlavorsManifest` task — sibling of
     * [registerIosFlavorXcconfigsTask]. Emits `build/kmp-flavors/variants.json`
     * from the currently declared `kmpFlavors {}` DSL.
     *
     * The three `MapProperty` inputs are wired to **lazy providers** that pull
     * from the plugin's public [KmpFlavorExtension] surface at execution time
     * — so the manifest picks up every DSL change (including
     * `local/LocalFlavors.kt` extensions) on the next run without any manual
     * refresh step.
     *
     * The task is **not** hooked to any downstream build task: it's driven
     * explicitly by the `resolve-variants` GHA job (`.github/workflows/…`) and
     * by fastlane's `variant_resolver.rb` at CI runtime — the manifest is a CI
     * derivation artifact, not a build-time dependency of the app modules
     * themselves.
     */
    private fun Project.registerExportKmpFlavorsManifestTask() {
        val ext = extensions.getByType<KmpFlavorExtension>()

        tasks.register<ExportKmpFlavorsManifestTask>("exportKmpFlavorsManifest") {
            group       = "kmp-flavors"
            description = "Emit variants.json from the kmpFlavors {} DSL (single derivation point for CI/CD)."

            // Lazy providers — Gradle reads these at execution time after DSL is fully
            // configured (including LocalFlavors.kt additions). `orNull ?: ""` ensures
            // flavors / build-types that declare no suffix serialise as the empty
            // string rather than dropping out of the map, so downstream consumers see
            // a total function from name → suffix.
            flavorAppIdSuffixes.set(provider {
                ext.flavors.associate { f -> f.name to (f.applicationIdSuffix.orNull ?: "") }
            })
            flavorBundleIdSuffixes.set(provider {
                ext.flavors.associate { f -> f.name to (f.bundleIdSuffix.orNull ?: "") }
            })
            buildTypeAppIdSuffixes.set(provider {
                ext.buildTypes.associate { bt -> bt.name to (bt.applicationIdSuffix.orNull ?: "") }
            })

            outputFile.set(layout.buildDirectory.file("kmp-flavors/variants.json"))
        }
    }
}
