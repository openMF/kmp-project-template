
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.mifos.configureFlavors
import org.mifos.configureKotlinAndroid
import org.mifos.configureKotlinMultiplatform
import org.mifos.libs

/**
 * Plugin that applies the Android library and Kotlin multiplatform plugins and configures them.
 */
class KMPLibraryConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.mifos.kmp.koin")
                apply("org.mifos.detekt.plugin")
                apply("org.mifos.spotless.plugin")
            }

            configureKotlinMultiplatform()

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 34
                configureFlavors(this)
                // The resource prefix is derived from the module name,
                // so resources inside ":core:module1" must be prefixed with "core_module1_"
                resourcePrefix = path
                    .split("""\W""".toRegex())
                    .drop(1).distinct()
                    .joinToString(separator = "_")
                    .lowercase() + "_"
            }

            dependencies {
                add("commonTestImplementation", libs.findLibrary("kotlin.test").get())
                add("commonTestImplementation", libs.findLibrary("kotlinx.coroutines.test").get())
            }
        }
    }
}