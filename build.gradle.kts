// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.google.oss.licenses.plugin) {
            exclude(group = "com.google.protobuf")
        }
        // Pin R8 to a version that understands Kotlin 2.3 metadata. The R8 bundled
        // with AGP 8.12.3 reads up to Kotlin metadata 2.1 only, so every release-mode
        // build with Kotlin 2.3.20 emits "R8: An error occurred when parsing kotlin
        // metadata" warnings for almost every class. Override it with R8 9.1.x stable.
        // Compatibility matrix: https://developer.android.com/studio/build/kotlin-d8-r8-versions
        classpath("com.android.tools:r8:9.1.31")
    }
}

plugins {
    alias(libs.plugins.kotlinCocoapods) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.dependencyGuard) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.moduleGraph) apply true
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.roborazzi) apply false
    // Multiplatform plugins
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.wire) apply false
    alias(libs.plugins.ktrofit) apply false

    alias(libs.plugins.room) apply false

    // Kover — root-level aggregation. Per-module kover application happens via
    // `org.convention.kover.plugin` chained from base convention plugins
    // (AndroidApplication / KMPLibrary / KMPCoreBaseLibrary). cmp-desktop applies
    // it directly. Aggregation + filter/verify config lives inline below this
    // plugins{} block because Gradle classpath isolation prevents extracting the
    // root-aggregator config to a helper (kover needs typed DSL at root, which
    // requires kover-gradle-plugin runtime classpath in build-logic, which causes
    // transitive AGP conflicts — proven by trial).
    // Tasks: ./gradlew koverHtmlReport | koverXmlReport | koverVerify
    alias(libs.plugins.kover) apply true
}

// ============================================================================
// Kover root aggregation
// ============================================================================

dependencies {
    listOf(
        // feature/* modules
        ":feature:crypto",
        ":feature:currency-rates",
        ":feature:emi-calculator",
        ":feature:home",
        ":feature:profile",
        ":feature:settings",
        // core/* modules
        ":core:analytics",
        ":core:common",
        ":core:data",
        ":core:database",
        ":core:datastore",
        ":core:designsystem",
        ":core:domain",
        ":core:model",
        ":core:network",
        ":core:store",
        ":core:ui",
        // core-base/* modules (framework-pure)
        ":core-base:analytics",
        ":core-base:common",
        ":core-base:database",
        ":core-base:datastore",
        ":core-base:designsystem",
        ":core-base:network",
        ":core-base:platform",
        ":core-base:security",
        ":core-base:store",
        ":core-base:ui",
    ).forEach { path ->
        kover(project(path))
    }
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.di.*",                       // Koin / kotlin-inject DI modules
                    "*.BuildConfig",
                    "*ComposableSingletons*",       // Compose generated lambda holders
                    "*_*Factory*",                  // Generated factories
                    "*\$ComposableLambda\$*",
                    "*Preview*",                    // @Preview functions
                    "*Test*",                       // test helpers themselves
                )
                packages(
                    "*.generated.*",
                    "*.ksp.*",
                )
                annotatedBy(
                    // @Composable funcs are better tested via screenshot/UI tests,
                    // not Kover line coverage.
                    "androidx.compose.runtime.Composable",
                )
            }
        }
        verify {
            // Phase 1 floor — single global threshold while coverage grows.
            // Per-module thresholds added as test-coverage PRs raise individual modules.
            rule { minBound(40) }
        }
    }
}

object DynamicVersion {
    fun setDynamicVersion(file: File, version: String) {
        val cleanedVersion = version.split('+')[0]
        file.writeText(cleanedVersion)
    }
}

tasks.register("versionFile") {
    val file = File(projectDir, "version.txt")

    DynamicVersion.setDynamicVersion(file, project.version.toString())
}

// Task to print all the module paths in the project e.g. :core:data
// Used by module graph generator script
tasks.register("printModulePaths") {
    subprojects {
        if (subprojects.isEmpty()) {
            println(this.path)
        }
    }
}

// Force consistent versions across all subprojects to fix KLIB resolver duplicate warnings
// The conflict is between org.jetbrains.androidx.* (CMP) and androidx.* (Google) transitive deps
subprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            // Replace Google androidx.lifecycle with JetBrains fork for non-Android targets
            if (requested.group == "org.jetbrains.androidx.lifecycle") {
                useVersion("2.9.6")
            }
            if (requested.group == "org.jetbrains.androidx.savedstate") {
                useVersion("1.3.6")
            }
        }
    }
}

// Configuration for CMP module dependency graph
moduleGraphAssert {
    configurations += setOf("commonMainImplementation", "commonMainApi")
    configurations += setOf("androidMainImplementation", "androidMainApi")
    configurations += setOf("desktopMainImplementation", "desktopMainApi")
    configurations += setOf("jsMainImplementation", "jsMainApi")
    configurations += setOf("nativeMainImplementation", "nativeMainApi")
    configurations += setOf("wasmJsMainImplementation", "wasmJsMainApi")
}

