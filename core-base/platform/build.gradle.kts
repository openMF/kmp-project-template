/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import com.android.build.api.dsl.androidLibrary
import com.codingfeline.buildkonfig.compiler.FieldSpec
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
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.codingfeline.buildKonfig)
}


kotlin {

    targets.withType<KotlinMetadataTarget> {
        compilations.all {
            // force BuildKonfig to attach
        }
    }

    androidLibrary {
        namespace = "template.core.base.platform"

//        buildFeatures {
//            buildConfig = true
//        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(libs.calf.permissions)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.ktx)
            implementation(libs.androidx.activity.compose)

            implementation(libs.androidx.metrics)
            implementation(libs.androidx.browser)
            implementation(libs.androidx.compose.runtime)

            implementation(compose.material3)

            implementation(libs.review)
            implementation(libs.review.ktx)

            implementation(libs.app.update.ktx)
            implementation(libs.app.update)
        }
    }
}


buildkonfig {
    packageName = "template.core.base.platform"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "true", const = true)
    }
//    defaultConfigs("debug") {
//        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "true", const = true)
//    }

}