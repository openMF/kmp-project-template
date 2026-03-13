/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import org.gradle.kotlin.dsl.invoke

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.kmp.library.convention)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.mifos.kmp.room)
    alias(libs.plugins.mifos.kmp.sqldelight)
}

android {
    namespace = "org.mifos.core.database"
}

val isWindows = System.getProperty("os.name").lowercase().contains("win")

fun isInstalled(binary: String) = try {
    val command = if (isWindows) arrayOf("where", binary) else arrayOf("which", binary)
    Runtime.getRuntime().exec(command).waitFor() == 0
} catch (e: Exception) {
    false
}

kotlin {
    js(IR) {
        browser {
            testTask {
                useKarma {
                    // Update this block to use your preferred browser for running tests.
                    when {
                        isInstalled("google-chrome") || isInstalled("chrome") -> useChrome()
                        isInstalled("chromium") || isInstalled("chromium-browser") -> useChrome()
                        isInstalled("brave") || isInstalled("brave-browser") -> useChrome()
                        isInstalled("microsoft-edge") || isInstalled("microsoft-edge-stable") || isInstalled("msedge") -> useChrome()
                        isInstalled("firefox") -> useFirefox()
                        isInstalled("opera") -> useOpera()
                        isInstalled("safari") -> useSafari()
                        else -> useChrome()
                    }
                }
                if (!isInstalled("google-chrome") && !isInstalled("chrome")) {
                    when {
                        isInstalled("chromium") -> environment("CHROME_BIN", "chromium")
                        isInstalled("chromium-browser") -> environment("CHROME_BIN", "chromium-browser")
                        isInstalled("brave") -> environment("CHROME_BIN", "brave")
                        isInstalled("brave-browser") -> environment("CHROME_BIN", "brave-browser")
                        isInstalled("microsoft-edge") -> environment("CHROME_BIN", "microsoft-edge")
                        isInstalled("microsoft-edge-stable") -> environment("CHROME_BIN", "microsoft-edge-stable")
                        isInstalled("msedge") -> environment("CHROME_BIN", "msedge")
                    }
                }
            }
        }
    }
    wasmJs {
        browser {
            testTask {
                useKarma {
                    // Update this block to use your preferred browser for running tests.
                    when {
                        isInstalled("google-chrome") || isInstalled("chrome") -> useChrome()
                        isInstalled("chromium") || isInstalled("chromium-browser") -> useChrome()
                        isInstalled("microsoft-edge") || isInstalled("microsoft-edge-stable") || isInstalled("msedge") -> useChrome()
                        isInstalled("firefox") -> useFirefox()
                        isInstalled("brave") || isInstalled("brave-browser") -> useChrome()
                        isInstalled("opera") -> useOpera()
                        isInstalled("safari") -> useSafari()
                        else -> useChrome()
                    }
                }
                if (!isInstalled("google-chrome") && !isInstalled("chrome")) {
                    when {
                        isInstalled("chromium") -> environment("CHROME_BIN", "chromium")
                        isInstalled("chromium-browser") -> environment("CHROME_BIN", "chromium-browser")
                        isInstalled("brave") -> environment("CHROME_BIN", "brave")
                        isInstalled("brave-browser") -> environment("CHROME_BIN", "brave-browser")
                        isInstalled("microsoft-edge") -> environment("CHROME_BIN", "microsoft-edge")
                        isInstalled("microsoft-edge-stable") -> environment("CHROME_BIN", "microsoft-edge-stable")
                        isInstalled("msedge") -> environment("CHROME_BIN", "msedge")
                    }
                }
            }
        }
    }
    sourceSets {
        val desktopMain by getting
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqldelight.runtime)
        }
        androidUnitTest.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.robolectric)
            implementation(libs.androidx.test.ext.junit)
        }

        nativeMain.dependencies {
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.sqldelight.runtime)
        }

        desktopMain.dependencies {
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.sqldelight.runtime)

        }
        jsMain.dependencies {
            implementation(libs.sqldelight.runtime)
            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.2.1"))
            implementation(npm("sql.js", "1.10.3"))
            implementation(devNpm("copy-webpack-plugin", "12.0.2"))
        }
        wasmJsMain.dependencies {
            implementation(libs.sqldelight.runtime)
            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.2.1"))
            implementation(npm("sql.js", "1.10.3"))
            implementation(devNpm("copy-webpack-plugin", "12.0.2"))
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            api(projects.core.common)
            api(projects.coreBase.database)
        }
        commonTest.dependencies {
            implementation(libs.turbine)
        }
    }
}
tasks.register("checkSourceSets") {
    doLast {
        kotlin.sourceSets.forEach { ss ->
            println("SourceSet: ${ss.name}")
            ss.dependsOn.forEach { dep ->
                println("  dependsOn: ${dep.name}")
            }
        }
    }
}
kotlin.sourceSets.all { println("SourceSet: $name") }
println("jsMain depends on: " + kotlin.sourceSets.getByName("jsMain").dependsOn.map { it.name })
