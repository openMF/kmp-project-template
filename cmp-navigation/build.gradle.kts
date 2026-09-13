/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

plugins {
    alias(libs.plugins.kmp.library.convention)
    alias(libs.plugins.cmp.feature.convention)
    alias(libs.plugins.kmp.koin.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Core Modules
            implementation(projects.core.data)
            implementation(projects.core.database)
            implementation(projects.core.network) // E1: FeatureRegistry wires the relocated DemoNetworkModule (core/network/demo/di)
            implementation(projects.core.model)
            implementation(projects.core.common)
            implementation(projects.core.datastore)
            // Firebase analytics (firebaseModule + AnalyticsHelper + Compose helpers) via core/firebase.
            implementation(projects.core.firebase)
            // core/platform re-exports core-base/platform (platformModule, GarbageCollectionManager) —
            // the app-shell reaches those through core/ per G-CORE-BASE-ENCAP.
            implementation(projects.core.platform)
            // core-base/security is the ONE sanctioned app-shell exception: cmp-navigation is the DI
            // aggregator (KoinModules wires SecurityModule) and reads isReleaseBuild; no core/ wrapper
            // is warranted for a security module the shell itself assembles. Feature modules NEVER
            // depend on core-base — enforced by the encapsulation gate (Phase A).
            implementation(projects.coreBase.security)

            // Backbone shell features (template-owned) — always present in every fork.
            implementation(projects.feature.home)
            implementation(projects.feature.profile)
            implementation(projects.feature.settings)
            // Fork feature-module deps come from the fork-owned `feature-deps.gradle.kts` seam
            // (applied at the bottom of this file, S7/F4). A fork adds a feature there, never here.
            implementation(projects.sync)

            // put your multiplatform dependencies here
            // Deep links (home-screen widgets + external app:// URIs) — cmp-deep-link (KmpToolkit,
            // commonMain). Android capture is auto-init (ContentProvider); RootNavScreen collects
            // DeepLinkHandler.lastReceived and dispatches through the fork-owned DeepLinkRegistry seam.
            implementation(libs.cmp.deep.link)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.components.resources)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            // Phase 3 (store5-screen-state-persistence 03-vm-scoping) — enables
            // koinNavViewModel() so nav destinations acquire ViewModels scoped to
            // NavBackStackEntry (cleared on pop) instead of Activity (cleared on
            // Activity death). Resolves io.insert-koin:koin-compose-viewmodel-navigation
            // via gradle/libs.versions.toml:259; version is the shared Koin ref.
            implementation(libs.koin.compose.navigation)
            // Provides `com.russhwolf.settings.Settings` referenced by
            // `saveable/PersistentSaveableStateRegistry.kt` at the app root.
            // The `named("plain")` binding itself is contributed by
            // `core-base/datastore/DatastoreBaseModule` (transitively wired in
            // via `core/datastore/DatastoreModule` in `KoinModules.allModules`).
            implementation(libs.multiplatform.settings)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.serialization.core)
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "cmp.navigation.generated.resources"
}

// Fork-owned feature-module dependencies (S7/F4 white-label seam). Applied AFTER the `kotlin { }` block
// above so the `commonMainImplementation` configuration it contributes to already exists. A fork edits
// `feature-deps.gradle.kts`, never this template-owned build file — a template sync full-copies this file.
//
// Apply ONLY when the seam file is present. `feature-deps.gradle.kts` is `owner: fork` (never synced), so a
// fork that adopted the template BEFORE this seam existed — or is mid-adoption — may not have it yet; an
// unconditional `apply(from = …)` then fails the whole configuration ("Could not read script …feature-deps
// .gradle.kts as it does not exist"), which blocks even `syncForkConfig`. Guarding the apply keeps
// cmp-navigation configurable in that window; the fork wires its features by creating the seam file.
rootProject.file("feature-deps.gradle.kts").takeIf { it.exists() }?.let { apply(from = it) }

// ── GeneratedFeatureKoinBindings ────────────────────────────────────────────
// `FeatureRegistry.featureKoinModules` used to be a hand-kept `listOf(...)`: adding a feature meant
// an import AND a list entry in a file the feature does not own, and forgetting either produced a
// clean build with a feature whose ViewModels simply never resolve at runtime.
//
// This derives the list the same way the KSP processors derive theirs — from the declarations
// themselves. It is a GRADLE task rather than a KSP processor for one measured reason: a KSP
// aggregator can only read a dependency's declarations during a PER-TARGET pass, never the
// commonMain metadata pass (probed 2026-09-13: metadata → 0 declarations, kspKotlinDesktop → 1).
// The consumer here is commonMain, so a KSP aggregate would force an expect/actual across six
// targets to reach it. Gradle already knows the feature list at configure time and can write
// straight into commonMain.
//
// The module's FQN is READ FROM SOURCE, never inferred from the Gradle path — `:feature:crypto`
// declares `CryptoFeatureModule`, not `CryptoModule`, and a convention would have silently dropped
// it. A feature with no `di/` module (e.g. `:feature:showcase`, nav-only) is skipped, not an error.
val generatedFeatureKoinDir = layout.buildDirectory.dir("generated/featureKoin/commonMain/kotlin")

val generateFeatureKoinBindings by tasks.registering {
    val featureDirs = rootProject.file("feature").listFiles()?.sortedBy { it.name }.orEmpty()
    val outDir = generatedFeatureKoinDir
    inputs.files(featureDirs.map { File(it, "src/commonMain/kotlin") }.filter { it.exists() })
    outputs.dir(outDir)
    doLast {
        val rx = Regex("""^\s*val\s+([A-Za-z0-9_]+Module)\s*(:\s*Module\s*)?=\s*module\s*\{""", RegexOption.MULTILINE)
        val found = mutableListOf<String>()
        featureDirs.forEach { fdir ->
            val src = File(fdir, "src/commonMain/kotlin")
            if (!src.isDirectory) return@forEach
            src.walkTopDown()
                .filter { it.isFile && it.extension == "kt" && it.parentFile.name == "di" }
                .sortedBy { it.path }
                .forEach { f ->
                    val text = f.readText()
                    val pkg = Regex("""^package\s+([A-Za-z0-9_.]+)""", RegexOption.MULTILINE)
                        .find(text)?.groupValues?.get(1) ?: return@forEach
                    rx.find(text)?.groupValues?.get(1)?.let { found += "$pkg.$it" }
                }
        }
        val dir = outDir.get().asFile.resolve("cmp/navigation/registry")
        dir.mkdirs()
        val body = if (found.isEmpty()) {
            "    // No feature module declares a Koin module yet.\n"
        } else {
            found.joinToString("\n") { "    includes(${it.substringAfterLast('.')})" } + "\n"
        }
        dir.resolve("GeneratedFeatureKoinBindings.kt").writeText(
            buildString {
                appendLine("// GENERATED by :cmp-navigation:generateFeatureKoinBindings — do not edit.")
                appendLine("package cmp.navigation.registry")
                appendLine()
                appendLine("import org.koin.core.module.Module")
                appendLine("import org.koin.dsl.module")
                found.sorted().forEach { appendLine("import $it") }
                appendLine()
                appendLine("/** Every `feature/<f>/di` Koin module, derived from source. */")
                appendLine("public val GeneratedFeatureKoinBindings: Module = module {")
                append(body)
                appendLine("}")
            },
        )
        logger.lifecycle("generateFeatureKoinBindings: ${found.size} feature module(s)")
    }
}

kotlin.sourceSets.named("commonMain") { kotlin.srcDir(generatedFeatureKoinDir) }
tasks.matching { it.name.startsWith("compileKotlin") || it.name.startsWith("ksp") }
    .configureEach { dependsOn(generateFeatureKoinBindings) }

// ── GeneratedFeatureDestinations ────────────────────────────────────────────
// The nav twin of GeneratedFeatureKoinBindings. Scans for `@FeatureDestination` and emits the
// aggregate `FeatureRegistry.featureDestinations` used to spell out by hand.
//
// Keyed on the ANNOTATION, never on a naming convention: "top-level" is not readable from the
// signature. `amortizationScheduleDestination` has the exact shape of a top-level entry but is
// nested inside `loansGraph`, and `cloudTodoGraph` is top-level-shaped yet belongs to
// ShowcaseRegistry. A `*Graph` vs `*Destination` rule mis-registers both. Registration is a
// decision, so it is declared at the destination.
val generatedFeatureNavDir = layout.buildDirectory.dir("generated/featureNav/commonMain/kotlin")

val generateFeatureDestinations by tasks.registering {
    val featureDirs = rootProject.file("feature").listFiles()?.sortedBy { it.name }.orEmpty()
    val outDir = generatedFeatureNavDir
    inputs.files(featureDirs.map { File(it, "src/commonMain/kotlin") }.filter { it.exists() })
    outputs.dir(outDir)
    doLast {
        // `@FeatureDestination` immediately above `fun NavGraphBuilder.<name>(`.
        val rx = Regex(
            """@FeatureDestination\s*\n\s*(?:public\s+)?fun\s+NavGraphBuilder\.([A-Za-z0-9_]+)\s*\(([^)]*)\)""",
            RegexOption.MULTILINE,
        )
        val found = mutableListOf<Pair<String, String>>()   // fqn to simpleName
        val bad = mutableListOf<String>()
        featureDirs.forEach { fdir ->
            val src = File(fdir, "src/commonMain/kotlin")
            if (!src.isDirectory) return@forEach
            src.walkTopDown()
                .filter { it.isFile && it.extension == "kt" && it.parentFile.name == "navigation" }
                .sortedBy { it.path }
                .forEach { f ->
                    val text = f.readText()
                    val pkg = Regex("""^package\s+([A-Za-z0-9_.]+)""", RegexOption.MULTILINE)
                        .find(text)?.groupValues?.get(1) ?: return@forEach
                    rx.findAll(text).forEach { m ->
                        val name = m.groupValues[1]
                        val params = m.groupValues[2].trim()
                        // The aggregate invokes ONE shape. A destination with a bespoke signature
                        // cannot be called generically, and silently dropping it would remove a
                        // screen from the app with a clean build — so it fails the build instead.
                        if (!Regex("""^[A-Za-z0-9_]+\s*:\s*NavController$""").matches(params)) {
                            bad += "$pkg.$name($params)"
                        } else {
                            found += "$pkg.$name" to name
                        }
                    }
                }
        }
        if (bad.isNotEmpty()) {
            throw GradleException(
                "generateFeatureDestinations: @FeatureDestination requires the signature " +
                    "(navController: NavController) so the aggregate can invoke it uniformly. " +
                    "Non-conforming: ${bad.joinToString("; ")}",
            )
        }
        val dir = outDir.get().asFile.resolve("cmp/navigation/registry")
        dir.mkdirs()
        dir.resolve("GeneratedFeatureDestinations.kt").writeText(
            buildString {
                appendLine("// GENERATED by :cmp-navigation:generateFeatureDestinations — do not edit.")
                appendLine("package cmp.navigation.registry")
                appendLine()
                appendLine("import androidx.navigation.NavController")
                appendLine("import androidx.navigation.NavGraphBuilder")
                found.map { it.first }.sorted().forEach { appendLine("import $it") }
                appendLine()
                appendLine("/** Every `@FeatureDestination` top-level entry, derived from source. */")
                appendLine("public val GeneratedFeatureDestinations: NavGraphBuilder.(NavController) -> Unit = {")
                appendLine("        navController ->")
                if (found.isEmpty()) {
                    appendLine("    // No feature declares a @FeatureDestination yet.")
                } else {
                    found.map { it.second }.sorted().forEach { appendLine("    $it(navController)") }
                }
                appendLine("}")
            },
        )
        logger.lifecycle("generateFeatureDestinations: ${found.size} destination(s)")
    }
}

kotlin.sourceSets.named("commonMain") { kotlin.srcDir(generatedFeatureNavDir) }
tasks.matching { it.name.startsWith("compileKotlin") || it.name.startsWith("ksp") }
    .configureEach { dependsOn(generateFeatureDestinations) }
