
import org.convention.keystore.KeystoreConfig
import org.convention.keystore.SecretsConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for keystore management following your existing patterns
 */
class KeystoreManagementConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            // Create extension for configuration
            val keystoreExtension = extensions.create("keystoreManagement", KeystoreManagementExtension::class.java)

            // Set default configurations
            keystoreExtension.keystoreConfig.convention(KeystoreConfig())
            keystoreExtension.secretsConfig.convention(SecretsConfig())

            // Add task group description
            tasks.register("keystoreHelp") {
                group = "keystore"
                description = "Shows available keystore management commands"
                doLast {
                    logger.lifecycle("""
                        |Keystore Management Plugin
                        |
                        |Available tasks:
                        |  - keystoreHelp: Shows this help message
                        |
                        |Configuration example:
                        |  keystoreManagement {
                        |    keystoreConfig {
                        |      companyName = "Your Company"
                        |      organization = "Your Org"
                        |    }
                        |  }
                    """.trimMargin())
                }
            }
        }
    }
}

/**
 * Extension class for keystore management configuration
 */
abstract class KeystoreManagementExtension {
    abstract val keystoreConfig: org.gradle.api.provider.Property<KeystoreConfig>
    abstract val secretsConfig: org.gradle.api.provider.Property<SecretsConfig>
}