import com.mobilebytelabs.kmpflavors.KmpFlavorExtension
import com.mobilebytelabs.kmpflavors.KmpFlavorPlugin
import org.convention.KmpFlavors
import org.convention.configureKmpFlavors
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention plugin that applies and configures KMP Product Flavors plugin.
 *
 * This plugin provides cross-platform flavor support for Kotlin Multiplatform modules,
 * allowing consistent flavor configuration across Android, iOS, Desktop, and Web targets.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("org.convention.kmp.flavors")
 * }
 * ```
 *
 * This will configure:
 * - Demo/Prod flavors aligned with Android application flavors
 * - BuildConfig generation with flavor-specific constants
 * - Proper source set wiring for all platforms
 */
class KMPFlavorsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Apply the KMP Product Flavors plugin by class
            pluginManager.apply(KmpFlavorPlugin::class.java)

            // Configure flavors using centralized configuration
            extensions.configure<KmpFlavorExtension> {
                configureKmpFlavors(
                    extension = this,
                    dimensions = KmpFlavors.defaultDimensions,
                    flavors = KmpFlavors.defaultFlavors,
                    generateBuildConfig = true,
                    buildConfigPackage = inferBuildConfigPackage(target),
                )
            }
        }
    }

    /**
     * Infers the BuildConfig package from the project's group or path.
     */
    private fun inferBuildConfigPackage(project: Project): String {
        // Try to use the project's group if available
        val group = project.group.toString()
        if (group.isNotEmpty() && group != "unspecified") {
            return "$group.${project.name.replace("-", ".")}"
        }

        // Fall back to path-based package name
        val pathParts = project.path
            .removePrefix(":")
            .split(":")
            .filter { it.isNotEmpty() }

        return if (pathParts.isNotEmpty()) {
            "org.mifos.${pathParts.joinToString(".") { it.replace("-", ".") }}"
        } else {
            "org.mifos.${project.name.replace("-", ".")}"
        }
    }
}
