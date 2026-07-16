import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Adds the `iosX64` (Intel simulator) target on top of the shared KMP-library base
 * (`org.convention.kmp.library` / `org.convention.kmp.core.base.library`, which already register
 * iosArm64/iosSimulatorArm64 via `configureKotlinMultiplatform()`).
 *
 * A separate opt-in plugin, not a flag on the base convention, because Compose Multiplatform
 * 1.12.0-beta02 publishes no `ios_x64` klib for `compose.runtime`/`compose.ui`/`compose.foundation`
 * (Xcode dropped the Intel simulator; JetBrains has been retiring the target release over release)
 * — any module resolving those artifacts fails `compileKotlinIosX64` outright. Apply only to
 * Compose-free modules; Compose Multiplatform modules (anything aliasing `jetbrainsCompose`) must
 * NOT apply this plugin.
 */
class KMPLibraryIosX64ConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.configure<KotlinMultiplatformExtension> {
            iosX64()
        }
    }
}
