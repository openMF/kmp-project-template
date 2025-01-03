plugins {
    alias(libs.plugins.mifospay.cmp.feature)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "org.mifos.feature.home"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
