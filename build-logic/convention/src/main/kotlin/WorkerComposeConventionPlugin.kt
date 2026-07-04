import org.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin: `id("org.convention.worker.compose")`
 *
 * Wires the [worker-kmp](https://github.com/MobileByteLabs/worker-kmp) library into a
 * Compose Multiplatform module:
 *
 *  1. Applies `io.github.mobilebytelabs.worker-app` programmatically. The Gradle plugin
 *     codegens every per-platform launcher (Android Application/Activity/manifest, JVM
 *     `fun main()`, iOS `MainViewController` + xcodegen project, wasmJs `fun main()` +
 *     `index.html`) at build time — consumer source tree carries zero per-platform Kotlin.
 *  2. Adds the `worker-compose-all` umbrella to `commonMain` — single dep brings in core
 *     `WorkManager` API + the Koin DI module + Compose Multiplatform UI + Store5 + all
 *     four platform factories.
 *  3. Adds `koin-compose` to `commonMain` so consumer Composables can `koinInject()` the
 *     `WorkScheduler` façade with no extra wiring.
 *  4. Enables the `ExperimentalWorkerApi` opt-in at the `commonMain` source-set level
 *     so consumers don't have to repeat the `@OptIn` annotation file-by-file.
 *
 * Apply this on the module that hosts the consumer-facing workers + the `WorkScheduler`
 * (in this template: the new `sync/` module from Phase 4 of the
 * mifos-template-sync-integration epic).
 *
 * Library pin: see `worker-version` in `gradle/libs.versions.toml`.
 */
class WorkerComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // 1. Apply the worker-kmp app Gradle plugin. Requires the dep to be on the
            // build-logic runtime classpath via `implementation(libs.worker.app.plugin)`
            // (see build-logic/convention/build.gradle.kts).
            pluginManager.apply("io.github.mobilebytelabs.worker-app")

            // 2 + 3. Add umbrella + koin-compose to commonMain.
            dependencies {
                add("commonMainImplementation", libs.findLibrary("worker-compose-all").get())
                add("commonMainImplementation", libs.findLibrary("koin-compose").get())
            }

            // 4. Enable the ExperimentalWorkerApi opt-in on EVERY source set.
            // KGP does NOT auto-propagate a commonMain-only opt-in down the hierarchy —
            // under the AGP9 KMP library plugin (openMF/dev migration) it fails the
            // "dependent source set must use all opt-in annotations that its dependency
            // uses" consistency check (androidMain depends on commonMain). Applying it to
            // all source sets via configureEach keeps consumers free of file-level @OptIn.
            extensions.configure(KotlinMultiplatformExtension::class.java) {
                sourceSets.configureEach {
                    languageSettings.optIn("io.github.mobilebytelabs.worker.ExperimentalWorkerApi")
                }
            }
        }
    }
}
