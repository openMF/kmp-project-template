plugins {
    alias(libs.plugins.cmp.feature.convention)
}

android {
    namespace = "org.mifos.feature.onboarding"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.data)
            implementation(projects.core.datastore)
            implementation(projects.core.model)
            implementation(projects.coreBase.ui)

            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
    }
}

compose {
    resources {
        packageOfResClass = "org.mifos.feature.onboarding.generated.resources"
    }
}
