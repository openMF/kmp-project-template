import app.cash.sqldelight.gradle.SqlDelightExtension
import org.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.Actions.with
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies


private const val DATABASE_NAME = "MifosSQLDelightDatabase"

class KMPSQLDelightConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("app.cash.sqldelight")

            extensions.configure<SqlDelightExtension> {
                databases.create(DATABASE_NAME) {
                    packageName.set("org.mifos.core.database")
                    generateAsync.set(true)
                    schemaOutputDirectory.set(
                        file("$projectDir/schemas")
                    )
                    verifyMigrations.set(false)
                }
            }


            dependencies {
                add("androidMainImplementation",libs.findLibrary("sqldelight.android.driver").get())
                add("nativeMainImplementation",libs.findLibrary("sqldelight.native.driver").get())
                add("desktopMainImplementation",libs.findLibrary("sqldelight.sqlite.driver").get())
                add("jsMainImplementation",libs.findLibrary("sqldelight.web.worker.driver").get())
                add("wasmJsMainImplementation",libs.findLibrary("sqldelight.web.worker.driver").get())

                add("commonMainImplementation", libs.findLibrary("sqldelight.coroutines").get())
                add("commonMainImplementation", libs.findLibrary("sqldelight.primitive.adapters").get())
            }

        }

    }
}