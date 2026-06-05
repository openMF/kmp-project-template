/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mobile-wallet/blob/master/LICENSE.md
 */
import com.mobilebytelabs.kmpflavors.KmpFlavorExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlin.serialization)
    // Kover applied explicitly (cmp-desktop doesn't use a base convention plugin
    // that would chain it — unlike feature/*, core/*, cmp-shared, cmp-navigation,
    // cmp-android which get kover via Android/KMP/CMP convention plugins).
    alias(libs.plugins.kover.convention)
    alias(libs.plugins.kmp.flavors.convention)
}

kotlin {
    jvm()

    jvmToolchain(libs.versions.jvmToolchain.get().toInt())

    sourceSets {
        jvmMain.dependencies {
            implementation(projects.cmpShared)

            implementation(libs.kotlinx.coroutines.swing)
            implementation(compose.desktop.currentOs)
            implementation(libs.jb.kotlin.stdlib)
            implementation(libs.kotlin.reflect)

            implementation(libs.koin.core)
        }
    }
}

val appName: String = libs.versions.desktopAppName.get()
val packageNameSpace: String = libs.versions.appId.get()
val appVersion: String = libs.versions.desktopPackageVersion.get()

// Resolve the active flavor (Gradle property -PkmpFlavor=demo|prod; falls back to DSL default).
val kmpFlavorExt = extensions.getByType<KmpFlavorExtension>()
val activeFlavor: String = (findProperty("kmpFlavor") as? String)
    ?: kmpFlavorExt.flavors.find { it.isDefault.getOrElse(false) }?.name
    ?: "prod"
val activeFlavorConfig = kmpFlavorExt.flavors.findByName(activeFlavor)

val windowTitle = appName + (activeFlavorConfig?.desktopWindowTitleSuffix?.orNull ?: "")
val macBundleId = packageNameSpace + (activeFlavorConfig?.applicationIdSuffix?.orNull ?: "")

compose.desktop {
    application {
        mainClass = "MainKt"
        // Use the same JDK that compiled cmp-shared (toolchain version 21).
        // The plugin defaults to the Gradle daemon's java.home (JDK 17 on this machine),
        // which can't load Java 21 bytecode from cmp-shared at runtime.
        javaHome = javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmToolchain.get().toInt()))
        }.get().metadata.installationPath.asFile.absolutePath
        jvmArgs("-Dapp.name=$windowTitle")
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = windowTitle
            packageVersion = appVersion
            description = "Desktop Application"
            copyright = "© 2024 Mifos Initiative. All rights reserved."
            vendor = "Mifos Initiative"
            licenseFile.set(project.file("../LICENSE"))
            includeAllModules = true

            macOS {
                bundleID = macBundleId
                dockName = windowTitle
                iconFile.set(project.file("icons/ic_launcher.icns"))
                notarization {
                    val providers = project.providers
                    appleID.set(providers.environmentVariable("NOTARIZATION_APPLE_ID"))
                    password.set(providers.environmentVariable("NOTARIZATION_PASSWORD"))
                    teamID.set(providers.environmentVariable("NOTARIZATION_TEAM_ID"))
                }
            }

            windows {
                menuGroup = windowTitle
                shortcut = true
                dirChooser = true
                perUserInstall = true
                iconFile.set(project.file("icons/ic_launcher.ico"))
            }

            linux {
                modules("jdk.security.auth")
                iconFile.set(project.file("icons/ic_launcher.png"))
            }
        }
        buildTypes.release.proguard {
            configurationFiles.from(file("compose-desktop.pro"))
            obfuscate.set(true)
            optimize.set(true)
        }
    }
}

