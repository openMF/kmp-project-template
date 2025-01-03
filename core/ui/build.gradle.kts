plugins {
    alias(libs.plugins.mifospay.kmp.library)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "org.mifos.core.ui"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(libs.androidx.metrics)
            implementation(libs.androidx.browser)
            implementation(libs.androidx.compose.runtime)
        }

        commonMain.dependencies {
            api(projects.core.analytics)
            api(projects.core.designsystem)
            api(projects.core.model)
            api(projects.core.common)
            implementation(libs.jb.composeViewmodel)
            implementation(libs.jb.lifecycleViewmodel)
            implementation(libs.jb.lifecycleViewmodelSavedState)
            implementation(libs.coil.kt)
            implementation(libs.coil.kt.compose)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.jb.composeNavigation)
            implementation(libs.filekit.compose)
            implementation(libs.filekit.core)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.bundles.androidx.compose.ui.test)
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
}