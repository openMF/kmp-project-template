plugins {
    alias(libs.plugins.mifospay.kmp.library)
    alias(libs.plugins.kotlin.parcelize)
    id("kotlinx-serialization")
}

android {
    namespace = "org.mifos.core.model"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}