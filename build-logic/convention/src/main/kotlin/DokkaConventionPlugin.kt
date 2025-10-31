
import org.convention.dokkaGradle
import org.gradle.api.Plugin
import org.convention.configureDokka
import org.gradle.api.Project

class DokkaConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            applyPlugins()

            dokkaGradle {
                configureDokka(this)
            }
        }
    }

    private fun Project.applyPlugins() {
        pluginManager.apply {
            apply("org.jetbrains.dokka")
        }
    }
}