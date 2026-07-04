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
            implementation(projects.feature.crypto)
            implementation(projects.feature.showcase)
            implementation(projects.sync)

            // put your multiplatform dependencies here
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.components.resources)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            // Phase 3 (store5-screen-state-persistence 03-vm-scoping) — enables
            // koinNavViewModel() so nav destinations acquire ViewModels scoped to
            // NavBackStackEntry (cleared on pop) instead of Activity (cleared on
            // Activity death). Resolves io.insert-koin:koin-compose-viewmodel-navigation
            // via gradle/libs.versions.toml:259; version is the shared Koin ref.
            implementation(libs.koin.compose.navigation)
            // Provides `com.russhwolf.settings.Settings` referenced by
            // `saveable/PersistentSaveableStateRegistry.kt` at the app root.
            // The `named("plain")` binding itself is contributed by
            // `core-base/datastore/DatastoreBaseModule` (transitively wired in
            // via `core/datastore/DatastoreModule` in `KoinModules.allModules`).
            implementation(libs.multiplatform.settings)
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
