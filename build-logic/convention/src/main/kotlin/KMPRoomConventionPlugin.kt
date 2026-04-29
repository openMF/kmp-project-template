import androidx.room3.gradle.RoomExtension
import org.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class KMPRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("androidx.room3")
            pluginManager.apply("com.google.devtools.ksp")

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                "implementation"(libs.findLibrary("androidx.room.runtime").get())

                listOf(
                    "kspAndroid",
                    "kspDesktop",
                    "kspIosArm64",
                    "kspIosX64",
                    "kspIosSimulatorArm64",
                    "kspJs",
                    "kspWasmJs",
                ).forEach { platform ->
                    add(platform, libs.findLibrary("androidx.room.compiler").get())
                }
            }
        }
    }
}
