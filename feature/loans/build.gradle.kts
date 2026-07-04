/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.cmp.feature.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.domain)
            implementation(projects.core.ui)
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.coreBase.store)
            implementation(projects.feature.amortization)

            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended)
            // compose-resources — for stringResource()-based UI copy (i18n) per
            // RULE-IMPL-NO-HARDCODED-STRING-001 (W2 of store5-superbrain-v2).
            // Mirrors `core/store/build.gradle.kts` wiring.
            implementation(compose.components.resources)

            // Phase 3 (store5-screen-state-persistence 03-vm-scoping) — koinNavViewModel()
            // for NavBackStackEntry-scoped VM acquisition on the PersonalLoansListScreen
            // acquisition site. Screens import it aliased as `retainedKoinViewModel` per
            // the sub-plan's naming contract; the enumeration Kdoc lives in the
            // `cmp-navigation/.../RetainedKoinViewModel.kt` sibling.
            //
            // NOTE (module-graph deviation from 03-vm-scoping.md): the sub-plan expected
            // the alias to be re-exported from cmp-navigation. That would require a
            // features → cmp-navigation edge (cyclic — cmp-navigation already depends on
            // this module), so features consume the koin symbol directly instead.
            implementation(libs.koin.compose.navigation)
        }

        commonTest.dependencies {
            // kotlin.test + kotlinx.coroutines.test come from the convention plugin.
            implementation(libs.turbine)
        }
    }
}

// Compose-resources class generator config — mirrors `core/store/build.gradle.kts`
// so `Res.string.*` is exposed under `kpt.feature.loans.generated.resources`,
// matching the source `import kpt.feature.loans.generated.resources.Res`
// inserted by RULE-IMPL-NO-HARDCODED-STRING-001 backfill (T7 of
// plan-layer/active/store5-superbrain-v2/02-impl-i18n-hard-rule.md).
compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "kpt.feature.loans.generated.resources"
}
