plugins {
    alias(libs.plugins.mifospay.kmp.library)
}

android {
    namespace = "org.mifos.core.domain"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.data)
            implementation(projects.core.model)
        }
    }
}