package org.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

/**
 * Configure the Kotlin Multiplatform plugin with the default hierarchy template and additional targets.
 * This includes JVM, Android, iOS, JS and WASM targets.
 * @see KotlinMultiplatformExtension
 * @see configure
 */
@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
internal fun Project.configureKotlinMultiplatform() {
    extensions.configure<KotlinMultiplatformExtension> {
        applyDefaultHierarchyTemplate()

        jvm("desktop")
        androidTarget()
        iosSimulatorArm64()
        iosX64()
        iosArm64()
        js(IR) {
            this.nodejs()
            binaries.executable()
        }
        wasmJs() {
            browser()
            nodejs()
        }

        androidTarget {
            instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
        }

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
}