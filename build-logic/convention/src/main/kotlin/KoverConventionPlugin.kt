import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlinx.kover")
            if (project == rootProject) configureRootAggregation()
        }
    }

    private fun Project.configureRootAggregation() {
        dependencies {
            subprojects
                .filter { sub ->
                    val p = sub.path
                    p.startsWith(":feature:") ||
                        p.startsWith(":core:") ||
                        p.startsWith(":core-base:")
                }
                .forEach { sub -> add("kover", sub) }
        }
        extensions.configure<KoverProjectExtension> {
            reports {
                filters {
                    excludes {
                        classes(
                            "*.di.*",
                            "*.BuildConfig",
                            "*ComposableSingletons*",
                            "*_*Factory*",
                            "*\$ComposableLambda\$*",
                            "*Preview*",
                            "*Test*",
                        )
                        packages("*.generated.*", "*.ksp.*")
                        annotatedBy("androidx.compose.runtime.Composable")
                    }
                }
                verify { rule { minBound(40) } }
            }
        }
    }
}
