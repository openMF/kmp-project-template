// File: samples/kmp-project-template/sync/build.gradle.kts
plugins {
    alias(libs.plugins.cmp.feature.convention)
    alias(libs.plugins.worker.compose.convention)
}

android { namespace = "org.mifos.sync" }

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:data"))
        implementation(project(":core:datastore"))
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.serialization.json)
        // worker-compose-all and koin-compose come from the convention plugin
    }
    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.core.ktx) // NotificationManagerCompat
    }
    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.kotlinx.coroutines.test)
    }
}
