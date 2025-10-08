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
 * This file configures Dokka for multi-module documentation generation in your KMP project.
 *
 * Prerequisites:
 * 1. Add Dokka plugin to your libs.versions.toml:
 *    [versions]
 *    dokka = "2.0.0"
 *
 *    [plugins]
 *    dokka = { id = "org.jetbrains.dokka", version.ref = "dokka" }
 *
 * 2. Enable Dokka V2 in gradle.properties:
 *    org.jetbrains.dokka.experimental.gradle.pluginMode=V2EnabledWithHelpers
 *
 * 3. Apply in root build.gradle.kts:
 *    plugins {
 *        alias(libs.plugins.dokka)
 *    }
 *    
 *    subprojects {
 *        apply(plugin = "org.jetbrains.dokka")
 *    }
 *    
 *    apply(from = "docs-generator/dokka-config/dokka.gradle.kts")
 *
 * 4. Run: ./gradlew dokkaHtmlMultiModule --no-configuration-cache
 *    Note: Dokka currently has issues with Gradle configuration cache
 */

import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial

// Configure the root multi-module task
tasks.withType<DokkaMultiModuleTask>().configureEach {
    moduleName.set("KMP Project Documentation")
    
    outputDirectory.set(layout.buildDirectory.dir("docs/html"))
    
    val readmeFile = rootProject.file("README.md")
    if (readmeFile.exists()) {
        includes.from(readmeFile)
    }
}

// Configure subproject documentation
subprojects {
    tasks.withType<DokkaTaskPartial>().configureEach {
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
                    java.net.URL(
                        "https://github.com/YOUR_USERNAME/YOUR_REPO/tree/main/${project.path.replace(":", "/")}/src"
                    )
                )
                remoteLineSuffix.set("#L")
            }
            
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
            // Exclude Compose Resources generated and API packages from Dokka
            perPackageOption {
                // org.jetbrains.compose.resources API
                matchingRegex.set("org\\.jetbrains\\.compose\\.resources(\\..*)?")
                suppress.set(true)
            }
            perPackageOption {
                // common pattern used by compose.resources generated classes
                matchingRegex.set(".*\\.generated\\.resources(\\..*)?")
                suppress.set(true)
            }
        }
    }
}
