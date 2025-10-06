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

// Apply Dokka to subprojects
subprojects {
    apply(plugin = "org.jetbrains.dokka")
}

// Dokka Configuration
tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask>().configureEach {
    moduleName.set("KMP Project Documentation")
    
    outputDirectory.set(layout.buildDirectory.dir("docs/html"))
    
    val readmeFile = rootProject.file("README.md")
    if (readmeFile.exists()) {
        includes.from(readmeFile)
    }
}

subprojects {
    tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
        moduleName.set(project.name)
        
        moduleVersion.set(project.version.toString())
        
        dokkaSourceSets.configureEach {
            suppress.set(false)
            
            displayName.set(name)
            
            val readmeFile = project.file("README.md")
            if (readmeFile.exists()) {
                includes.from(readmeFile)
            }
            
            sourceLink {
                localDirectory.set(projectDir.resolve("src"))
                remoteUrl.set(
                    java.net.URI(
                        "https://github.com/YOUR_USERNAME/YOUR_REPO/tree/main/${project.path.replace(":", "/")}/src"
                    ).toURL()
                )
                remoteLineSuffix.set("#L")
            }
            
            externalDocumentationLink {
                url.set(java.net.URI("https://kotlinlang.org/api/kotlinx.coroutines/").toURL())
                packageListUrl.set(
                    java.net.URI("https://kotlinlang.org/api/kotlinx.coroutines/package-list").toURL()
                )
            }
            
            externalDocumentationLink {
                url.set(java.net.URI("https://kotlinlang.org/api/kotlinx-serialization/").toURL())
            }
            
            externalDocumentationLink {
                url.set(java.net.URI("https://api.ktor.io/").toURL())
            }
            
            platform.set(
                when {
                    name.contains("android", ignoreCase = true) -> 
                        org.jetbrains.dokka.Platform.jvm
                    name.contains("jvm", ignoreCase = true) -> 
                        org.jetbrains.dokka.Platform.jvm
                    name.contains("js", ignoreCase = true) -> 
                        org.jetbrains.dokka.Platform.js
                    name.contains("native", ignoreCase = true) || 
                    name.contains("ios", ignoreCase = true) -> 
                        org.jetbrains.dokka.Platform.native
                    name.contains("common", ignoreCase = true) -> 
                        org.jetbrains.dokka.Platform.common
                    else -> org.jetbrains.dokka.Platform.common
                }
            )
            
            suppressInheritedMembers.set(false)
            suppressObviousFunctions.set(true)
            skipEmptyPackages.set(true)
            reportUndocumented.set(false)
            skipDeprecated.set(false)
            jdkVersion.set(17)
            
            perPackageOption {
                matchingRegex.set(".*\\.internal.*")
                suppress.set(true)
            }
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

// Configuration for CMP module dependency graph
moduleGraphAssert {
    configurations += setOf("commonMainImplementation", "commonMainApi")
    configurations += setOf("androidMainImplementation", "androidMainApi")
    configurations += setOf("desktopMainImplementation", "desktopMainApi")
    configurations += setOf("jsMainImplementation", "jsMainApi")
    configurations += setOf("nativeMainImplementation", "nativeMainApi")
    configurations += setOf("wasmJsMainImplementation", "wasmJsMainApi")
}

