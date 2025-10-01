/*
 * Copyright 2024 KMP Project Template
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Dokka Documentation Configuration Template
 *
 * This file should be applied to your root build.gradle.kts to enable
 * Dokka documentation generation for all modules in your KMP project.
 *
 * Usage:
 * 1. Add Dokka plugin to your libs.versions.toml:
 *    ```
 *    [versions]
 *    dokka = "2.0.0"
 *
 *    [plugins]
 *    dokka = { id = "org.jetbrains.dokka", version.ref = "dokka" }
 *    ```
 *
 * 2. Apply this configuration in your root build.gradle.kts:
 *    ```
 *    plugins {
 *        alias(libs.plugins.dokka)
 *    }
 *
 *    apply(from = "docs/dokka-config/dokka.gradle.kts")
 *    ```
 *
 * 3. Run: ./gradlew dokkaHtmlMultiModule
 */

plugins {
    id("org.jetbrains.dokka")
}

// Configure Dokka for all subprojects
subprojects {
    apply(plugin = "org.jetbrains.dokka")
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask>().configureEach {
    moduleName.set("KMP Project Documentation")
    
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
    
    includes.from(
        "README.md",
        "docs/ARCHITECTURE.md"
    )
}

// Configure individual module documentation
subprojects {
    tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
        moduleName.set(project.name)
        
        moduleVersion.set(project.version.toString())
        
        dokkaSourceSets {
            configureEach {
                // Include all source sets
                suppress.set(false)
                
                // Display name for the source set
                displayName.set(name)
                
                // Include package-level documentation
                includes.from("README.md")
                
                // Source link configuration - link to GitHub
                sourceLink {
                    localDirectory.set(projectDir.resolve("src"))
                    remoteUrl.set(
                        java.net.URL(
                            "https://github.com/YOUR_USERNAME/YOUR_REPO/tree/main/${project.path.replace(":", "/")}/src"
                        )
                    )
                    remoteLineSuffix.set("#L")
                }
                
                // External documentation links
                externalDocumentationLink {
                    url.set(java.net.URL("https://kotlinlang.org/api/kotlinx.coroutines/"))
                    packageListUrl.set(
                        java.net.URL("https://kotlinlang.org/api/kotlinx.coroutines/package-list")
                    )
                }
                
                externalDocumentationLink {
                    url.set(java.net.URL("https://kotlinlang.org/api/kotlinx-serialization/"))
                }
                
                externalDocumentationLink {
                    url.set(java.net.URL("https://api.ktor.io/"))
                }
                
                // Platform-specific configurations
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
                
                // Suppress inherited members from platform classes
                suppressInheritedMembers.set(false)
                
                // Suppress obvious functions
                suppressObviousFunctions.set(true)
                
                // Skip empty packages
                skipEmptyPackages.set(true)
                
                // Report undocumented code
                reportUndocumented.set(false)
                
                // No deprecation warnings in docs
                skipDeprecated.set(false)
                
                // JDK version for links
                jdkVersion.set(17)
                
                // Kotlin language version
                languageVersion.set("2.0")
                
                // Kotlin API version
                apiVersion.set("2.0")
                
                // Perma-links
                perPackageOption {
                    matchingRegex.set(".*\\.internal.*")
                    suppress.set(true)
                }
            }
        }
    }
}

// Task to generate documentation for all modules
tasks.register("generateDocs") {
    group = "documentation"
    description = "Generates complete documentation for all modules"
    
    dependsOn(tasks.named("dokkaHtmlMultiModule"))
    
    doLast {
        val docsDir = layout.buildDirectory.dir("dokka").get().asFile
        println("Documentation generated at: ${docsDir.absolutePath}")
        println("Open ${docsDir.resolve("index.html").absolutePath} in your browser")
    }
}

// Task to clean documentation
tasks.register<Delete>("cleanDocs") {
    group = "documentation"
    description = "Cleans generated documentation"
    
    delete(layout.buildDirectory.dir("dokka"))
}
