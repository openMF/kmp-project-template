import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin that wires worker-kmp into a Compose Multiplatform module.
 *
 * - Applies `io.github.mobilebytelabs.worker-app` (KSP-driven codegen of per-platform
 *   launchers from @WorkerKmpApp annotations).
 * - Adds `worker-compose-all` all-in-one bundle + `koin-compose` to commonMain.
 * - Enables the `io.github.mobilebytelabs.worker.ExperimentalWorkerApi` opt-in.
 *
 * Pre-condition (consumer responsibility): kotlin.multiplatform must be applied first.
 *
 * Note on pluginManager.apply vs plugins {} — a convention plugin cannot open a new
 * plugins {} scope on the target project, so worker-app is applied programmatically.
 * This requires the worker-app META-INF/gradle-plugins descriptor on the convention
 * plugin's RUNTIME classpath — hence implementation(libs.worker.app.plugin) in
 * build-logic/convention/build.gradle.kts, NOT compileOnly.
 *
 * Registered as: org.convention.worker.compose
 * Source-of-truth shape: docs/getting-started/convention-plugin.md
 */
class WorkerComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            pluginManager.apply("io.github.mobilebytelabs.worker-app")

            dependencies {
                add("commonMainImplementation", libs.findLibrary("worker-compose-all").get())
                add("commonMainImplementation", libs.findLibrary("koin-compose").get())
            }

            extensions.configure<KotlinMultiplatformExtension> {
                compilerOptions {
                    optIn.add("io.github.mobilebytelabs.worker.ExperimentalWorkerApi")
                }
            }
        }
    }
}
