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
}


kotlin {
//    android {
//        defaultConfig {
//            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        }
//        namespace = "org.mifos.core.designsystem"
//
//    }

    androidLibrary {
        namespace = "org.mifos.core.designsystem"
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }


    sourceSets {

        val androidHostTest by getting {
            dependencies {
                implementation(libs.androidx.compose.ui.test)
            }
        }
        val androidDeviceTest by getting {
            dependencies {
                implementation(libs.androidx.compose.ui.test)
            }
        }
//
//        androidInstrumentedTest.dependencies {
//            implementation(libs.androidx.compose.ui.test)
//        }
//        androidUnitTest.dependencies {
//            implementation(libs.androidx.compose.ui.test)
//        }
        commonMain.dependencies {
            api(projects.coreBase.designsystem)

            implementation(compose.ui)
            implementation(compose.uiUtil)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.coil.kt.compose)
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "org.mifos.core.designsystem.generated.resources"
}