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
    alias(libs.plugins.dokka)
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("$rootDir/docs-website/static/api"))
    }
}

// Currently cmp-web throws some unresolved symbol errors
// So it is not included in the dokka task

dependencies {
    dokka(project(":cmp-shared"))
    dokka(project(":cmp-desktop"))
    dokka(project(":cmp-android"))
//    dokka(project(":cmp-web:"))
    dokka(project(":cmp-navigation"))
    dokka(project(":core:data"))
    dokka(project(":core:domain"))
    dokka(project(":core:datastore"))
    dokka(project(":core:designsystem"))
    dokka(project(":core:ui"))
    dokka(project(":core:common"))
    dokka(project(":core:network"))
    dokka(project(":core:model"))
    dokka(project(":core:analytics"))
    dokka(project(":core:database:"))

    dokka(project(":feature:home"))
    dokka(project(":feature:profile"))
    dokka(project(":feature:settings"))

    dokka(project(":core-base:common"))
    dokka(project(":core-base:database"))
    dokka(project(":core-base:network"))
    dokka(project(":core-base:designsystem"))
    dokka(project(":core-base:platform"))
    dokka(project(":core-base:ui"))
    dokka(project(":core-base:analytics"))
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

// Configuration for CMP module dependency graph
moduleGraphAssert {
    configurations += setOf("commonMainImplementation", "commonMainApi")
    configurations += setOf("androidMainImplementation", "androidMainApi")
    configurations += setOf("desktopMainImplementation", "desktopMainApi")
    configurations += setOf("jsMainImplementation", "jsMainApi")
    configurations += setOf("nativeMainImplementation", "nativeMainApi")
    configurations += setOf("wasmJsMainImplementation", "wasmJsMainApi")
}

