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
// cmp-desktop is a JVM-only module and only reads the flavor DSL (windowTitle / macBundleId
// below) — it never references the generated KmpFlavorsRuntime class. Opt out of runtime
// codegen: the plugin's `expect KmpFlavorsRuntime` has no `actual` for a plain jvm() target,
// so self-generating it here breaks compileKotlinJvm. codegenHost=false = "consume the host's
// output transitively / do not self-generate" (see KmpFlavorExtension.codegenHost KDoc).
kmpFlavorExt.codegenHost.set(false)
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
            targetFormats(TargetFormat.Dmg, TargetFormat.Pkg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
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
                appCategory = "public.app-category.finance"
                iconFile.set(project.file("icons/ic_launcher.icns"))
                // Mac App Store signing.
                // MAC_SIGNING_IDENTITY: identity string passed by Fastlane lane via -P property.
                // MAC_KEYCHAIN_PATH: explicit keychain file path from the lane's temp keychain.
                // Identity format: "Apple Distribution: Org Name (TEAMID)"
                // Signing is skipped when MAC_SIGNING_IDENTITY is absent/empty (e.g. local DMG).
                val macSigningId = (project.findProperty("MAC_SIGNING_IDENTITY") as? String)
                    ?.takeIf { it.isNotEmpty() }
                    ?: providers.environmentVariable("MAC_SIGNING_IDENTITY").orNull
                        ?.takeIf { it.isNotEmpty() }
                // Explicit keychain path passed by Fastlane lane (MAC_KEYCHAIN_PATH).
                // Bypasses the execOperations.exec() keychain-search-list issue in
                // Gradle 9.x: security find-certificate with an explicit path works
                // even when the search-list lookup fails from a subprocess context.
                val macKeychainPath = (project.findProperty("MAC_KEYCHAIN_PATH") as? String)
                    ?.takeIf { it.isNotEmpty() }
                    ?: providers.environmentVariable("MAC_KEYCHAIN_PATH").orNull
                        ?.takeIf { it.isNotEmpty() }
                signing {
                    sign.set(macSigningId != null)
                    identity.set(macSigningId ?: "")
                    if (macKeychainPath != null) {
                        keychain.set(macKeychainPath)
                    }
                }
                // Sandbox entitlements required for Mac App Store submission.
                entitlementsFile.set(project.file("mac-app-store.entitlements"))
                // Provisioning profile embeds team + distribution cert info.
                // Set MAC_PROVISIONING_PROFILE_PATH to the .provisionprofile file path.
                // Capture projectDirectory eagerly (config-cache serializable Directory value)
                // so the .map{} lambda never captures an implicit Project reference.
                val projectDirectory = layout.projectDirectory
                provisioningProfile.set(
                    providers.environmentVariable("MAC_PROVISIONING_PROFILE_PATH")
                        .map { projectDirectory.file(it) }
                )
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



