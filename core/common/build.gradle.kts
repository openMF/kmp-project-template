plugins {
    alias(libs.plugins.mifospay.kmp.library)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "org.mifos.core.common"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(libs.coil.kt)
            api(libs.coil.core)
            api(libs.coil.svg)
            api(libs.coil.network.ktor)
            api(libs.kermit.logging)
            api(libs.squareup.okio)
            api(libs.jb.kotlin.stdlib)
            api(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
        iosMain.dependencies {
            api(libs.kermit.simple)
        }
        desktopMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.kotlin.reflect)
        }
        jsMain.dependencies {
            api(libs.jb.kotlin.stdlib.js)
            api(libs.jb.kotlin.dom)
        }
    }
}