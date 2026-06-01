/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

plugins {
    alias(libs.plugins.kmp.library.convention)
    alias(libs.plugins.cmp.feature.convention)
    alias(libs.plugins.kmp.koin.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Core Modules
            implementation(projects.core.data)
            implementation(projects.core.database)
            implementation(projects.core.model)
            implementation(projects.core.common)
            implementation(projects.core.datastore)

            implementation(projects.core.datastore)
            implementation(projects.coreBase.common)
            implementation(projects.coreBase.platform)
            implementation(projects.coreBase.security)

            implementation(projects.feature.home)
            implementation(projects.feature.currencyRates)
            implementation(projects.feature.emiCalculator)
            implementation(projects.feature.profile)
            implementation(projects.feature.settings)
            implementation(projects.feature.bills)
            implementation(projects.feature.loans)
            implementation(projects.feature.amortization)
            implementation(projects.feature.rates)
            implementation(projects.feature.calculators)
            implementation(projects.feature.macro)
            implementation(projects.feature.showcase)

            //put your multiplatform dependencies here
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.components.resources)
            implementation(libs.window.size)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.serialization.core)
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "cmp.navigation.generated.resources"
}