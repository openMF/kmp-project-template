import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Self-registering kover convention plugin.
 *
 * - Applied to root (via `alias(libs.plugins.kover.convention)` in the root
 *   plugins block): applies kover and configures the report filter + verify
 *   rules. Does NOT enumerate or filter subprojects.
 *
 * - Applied to any leaf module (chained from AndroidApplication / KMPLibrary
 *   / KMPCoreBaseLibraryConventionPlugin, or directly from cmp-desktop):
 *   applies kover AND self-registers into root's aggregation via
 *   `rootProject.dependencies.add("kover", project)`. Same pattern detekt /
 *   spotless use — each module opts itself in.
 *
 * Adding a new feature/core module to coverage requires zero changes here:
 * just ensure the new module applies one of the base convention plugins (or
 * applies `org.convention.kover.plugin` directly).
 */
class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlinx.kover")

            if (project == rootProject) {
                configureRootReports()
            } else {
                // Self-register into root's kover aggregation. Root's `kover`
                // configuration is created when KoverConventionPlugin applies
                // to root (during root build.gradle.kts evaluation, before
                // any subproject is configured), so this add() is safe.
                rootProject.dependencies.add("kover", project)
            }
        }
    }

    private fun Project.configureRootReports() {
        extensions.configure<KoverProjectExtension> {
            reports {
                filters {
                    excludes {
                        classes(
                            "*.di.*",                       // Koin / kotlin-inject DI modules
                            "*.BuildConfig",
                            "*ComposableSingletons*",       // Compose generated lambda holders
                            "*_*Factory*",                  // Generated factories
                            "*\$ComposableLambda\$*",
                            "*Preview*",                    // @Preview functions
                            "*Test*",                       // test helpers themselves
                        )
                        packages(
                            "*.generated.*",
                            "*.ksp.*",
                        )
                        annotatedBy(
                            // @Composable funcs are better tested via screenshot/UI tests,
                            // not Kover line coverage.
                            "androidx.compose.runtime.Composable",
                        )
                    }
                }
                verify {
                    // Phase 1 floor — single global threshold while coverage grows.
                    // Per-module thresholds added as test-coverage PRs raise individual modules.
                    rule { minBound(40) }
                }
            }
        }
    }
}
