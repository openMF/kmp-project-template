plugins {
    alias(libs.plugins.kmp.library.convention)
}

android {
    namespace = "org.mifos.corebase.network"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        desktopMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}