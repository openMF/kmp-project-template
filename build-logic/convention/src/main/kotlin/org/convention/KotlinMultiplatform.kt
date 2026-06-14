package org.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Configure the Kotlin Multiplatform plugin with the default hierarchy template and additional targets.
 * This includes JVM, iOS, JS and WASM targets.
 *
 * NOTE: androidTarget() is intentionally absent. com.android.kotlin.multiplatform.library (AGP 9+)
 * registers the Android target automatically via its withPlugin("kotlin.multiplatform") callback.
 * Calling androidTarget() here would create a duplicate 'android' target conflict.
 *
 * @see KotlinMultiplatformExtension
 * @see configure
 */
@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
internal fun Project.configureKotlinMultiplatform() {
    val jvmToolchainVersion = libs.findVersion("jvmToolchain").get().requiredVersion.toInt()

    extensions.configure<KotlinMultiplatformExtension> {
        applyProjectHierarchyTemplate()

        // Gradle JVM toolchain — selects the JDK used to compile + run tests.
        // Source/target compatibility stays at javaVersion (KotlinAndroid.kt) so
        // emitted bytecode remains compatible for downstream consumers; only the
        // BUILD JVM is set here.
        jvmToolchain(jvmToolchainVersion)


        jvm("desktop")
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

        targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
            experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
        }

    }
}