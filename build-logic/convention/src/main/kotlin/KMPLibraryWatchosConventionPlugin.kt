import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Adds `watchosArm64` + `watchosSimulatorArm64` targets on top of the shared KMP-library base
 * (`org.convention.kmp.library` / `org.convention.kmp.core.base.library`, which already register
 * iosArm64/iosSimulatorArm64/iosX64 + the default hierarchy template via
 * `configureKotlinMultiplatform()`).
 *
 * A separate opt-in plugin, not a flag on the base convention, so watchOS stays an explicit
 * per-module choice — mirroring how the Mileway/kmp-toolkit family gates watchOS behind its own
 * `mileway.kmp.library.watchos` plugin rather than the shared base. Compose Multiplatform modules
 * (no watchOS UI target exists) and modules pulling in platform SDKs without watchOS artifacts
 * (e.g. GitLive Firebase in `core-base:analytics`) must NOT apply this plugin. Apply only to
 * Compose-free "shared" logic modules, e.g. `core-base:common`, `core-base:observability`.
 */
class KMPLibraryWatchosConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.configure<KotlinMultiplatformExtension> {
            watchosArm64()
            watchosSimulatorArm64()
        }
    }
}
