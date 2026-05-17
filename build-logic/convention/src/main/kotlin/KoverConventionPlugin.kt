import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin that applies the Kover plugin to a module.
 *
 * Mirrors the DetektConventionPlugin / SpotlessConventionPlugin pattern — chained
 * from the base convention plugins (AndroidApplicationConventionPlugin,
 * KMPLibraryConventionPlugin, KMPCoreBaseLibraryConventionPlugin) so every module
 * that uses one of those gets kover applied automatically. cmp-desktop applies
 * this directly since it doesn't chain through a base convention plugin.
 *
 * Per-module application produces coverage artifacts that the root project's
 * aggregation block (in root build.gradle.kts) collects into a unified report.
 *
 * Root-level aggregation + filter/verify config lives inline at the bottom of
 * root build.gradle.kts. Helper-extraction to `org.convention.Kover.kt` (matching
 * the detekt/spotless pattern) was attempted but doesn't work cleanly for kover
 * because root-aggregation needs typed DSL at root, which requires kover-gradle-plugin
 * runtime classpath in build-logic, which causes transitive AGP conflicts.
 */
class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            applyPlugins()
        }
    }

    private fun Project.applyPlugins() {
        pluginManager.apply {
            apply("org.jetbrains.kotlinx.kover")
        }
    }
}
