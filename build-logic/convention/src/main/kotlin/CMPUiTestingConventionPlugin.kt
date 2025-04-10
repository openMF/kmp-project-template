import org.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@OptIn(ExperimentalWasmDsl::class)
class CMPUiTestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.configure<KotlinMultiplatformExtension>("kotlin") {
                wasmJs {
                    browser {
                        testTask {
                            useKarma {
                                useChromium()
                                // Add other browsers to test on, eg:-
                                // useChrome()
                            }
                        }
                    }
                }

                dependencies {
                    val composePluginVersion = libs.findVersion("compose-plugin").get()
                    add("desktopTestImplementation", "org.jetbrains.compose.desktop:desktop-jvm-$currentTarget:$composePluginVersion")
                    add("commonTestImplementation", "org.jetbrains.compose.ui:ui-test:$composePluginVersion")
                    add("androidTestImplementation", libs.findLibrary("androidx.compose.ui.test").get())
                    add("debugImplementation", libs.findLibrary("androidx.compose.ui.test.manifest").get())
                }
            }
        }
    }

    private val currentTarget by lazy {
        "$currentOS-$currentArch"
    }

    private val currentArch by lazy {
        when (val osArch = System.getProperty("os.arch")) {
            "x86_64", "amd64" -> "x64"
            "aarch64" -> "arm64"
            else -> error("Unsupported OS arch: $osArch")
        }
    }

    private val currentOS by lazy {
        val os = OperatingSystem.current()
        when {
            os.isWindows -> "windows"
            os.isLinux -> "linux"
            os.isMacOsX -> "macos"
            else -> error("Unsupported OS: ${os.name}")
        }
    }
}
