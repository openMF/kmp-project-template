/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import com.android.build.api.dsl.androidLibrary
import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.gradle.declarative.dsl.schema.FqName.Empty.packageName
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget

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
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.codingfeline.buildKonfig)
}


kotlin {

    targets.withType<KotlinMetadataTarget> {
        compilations.all {
            // force BuildKonfig to attach
        }
    }
    androidLibrary {
        namespace = "template.core.base.analytics"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(libs.kermit.logging)

            // For timing and performance tracking
            implementation(libs.kotlinx.datetime)
        }

        // TODO: The dependency is throwing errors.
        // I am commenting it, but please verify if it is actually even required in android Main block.
        // As the cmp-android source set applies firebase itself also.
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
//            implementation(libs.gitlive.firebase.analytics)
        }

        nonJsCommonMain.dependencies {
            implementation(libs.gitlive.firebase.analytics)
        }

        nativeMain.dependencies {
            implementation(libs.gitlive.firebase.analytics)
        }

        desktopMain.dependencies {
            implementation(libs.gitlive.firebase.analytics)
        }

        mobileMain.dependencies {
            implementation(libs.gitlive.firebase.crashlytics)
        }
        
        // Test dependencies for all platforms
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

buildkonfig {
    packageName = "template.core.base.analytics"

// 1. Default (e.g. Debug/Dev)
    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "FLAVOR", "dev")
        buildConfigField(FieldSpec.Type.BOOLEAN, "IS_DEMO_MODE", "true")
    }

    // 2. Production Flavor (Overwrites default)
    targetConfigs {
        create("prod") {
            buildConfigField(FieldSpec.Type.STRING, "FLAVOR", "prod")
            buildConfigField(FieldSpec.Type.BOOLEAN, "IS_DEMO_MODE", "false")
        }
    }
}
