import org.convention.configureDokkaConvention
import org.gradle.api.Plugin
import org.gradle.api.Project

class DokkaConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.configureDokkaConvention()
    }

    private fun Project.applyPlugins() {
        pluginManager.apply {
            apply("org.jetbrains.dokka")
        }
    }
}