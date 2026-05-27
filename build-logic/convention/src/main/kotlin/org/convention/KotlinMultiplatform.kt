package org.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Configure the Kotlin Multiplatform plugin with the default hierarchy template and additional targets.
 * This includes JVM, Android, iOS, JS and WASM targets.
 * @see KotlinMultiplatformExtension
 * @see configure
 */
@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
internal fun Project.configureKotlinMultiplatform() {
    extensions.configure<KotlinMultiplatformExtension> {
        applyProjectHierarchyTemplate()

        // Gradle JVM toolchain — selects the JDK used to compile + run tests.
        // Bumped to 21 in Phase 12 because cmp-network-monitor v3.3.1+ emits
        // Java 21 bytecode (class file version 65) which a Java 17 JVM can't load.
        // Source/target compatibility stays at Java 17 (see KotlinAndroid.kt) so
        // emitted bytecode remains 17-compatible for downstream consumers; only
        // the BUILD JVM is bumped.
        jvmToolchain(21)

        jvm("desktop")
        androidTarget()
        iosSimulatorArm64()
        iosArm64()
        js(IR) {
            this.nodejs()
            binaries.executable()
        }
        wasmJs() {
            browser()
            nodejs()
        }

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        }
    }
}