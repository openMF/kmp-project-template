package org.convention

import com.mobilebytelabs.kmpflavors.FlavorConfig
import com.mobilebytelabs.kmpflavors.KmpFlavorExtension
import org.gradle.api.NamedDomainObjectContainer

/**
 * KMP Product Flavors configuration for kmp-project-template.
 *
 * This provides cross-platform flavor support that aligns with the existing
 * Android application flavors (demo/prod).
 *
 * ## Flavor Dimensions
 * - **contentType**: Demo vs Production content source
 *
 * ## Build Variants
 * - demo: Uses local/mock data for development and testing
 * - prod: Uses production backend services
 *
 * ## Usage in Modules
 * ```kotlin
 * plugins {
 *     id("org.convention.kmp.flavors")
 * }
 * ```
 */
object KmpFlavors {

    /**
     * Flavor dimension definitions.
     * Currently matches Android's contentType dimension.
     */
    enum class Dimension(val priority: Int) {
        CONTENT_TYPE(0)
    }

    /**
     * Available flavors aligned with Android application flavors.
     */
    enum class Flavor(
        val dimension: Dimension,
        val isDefault: Boolean = false,
        val applicationIdSuffix: String? = null,
        val bundleIdSuffix: String? = null,
    ) {
        DEMO(
            dimension = Dimension.CONTENT_TYPE,
            isDefault = true, // Demo is default for development
            applicationIdSuffix = ".demo",
            bundleIdSuffix = ".demo",
        ),
        PROD(
            dimension = Dimension.CONTENT_TYPE,
            isDefault = false,
        );

        val flavorName: String = name.lowercase()
    }

    /**
     * Default dimension configurations for kmp-product-flavors.
     */
    val defaultDimensions: List<DimensionConfig>
        get() = Dimension.values().map { dimension ->
            DimensionConfig(
                name = dimension.name.lowercase().replace("_", ""),
                priority = dimension.priority,
            )
        }

    /**
     * Default flavor configurations for kmp-product-flavors.
     */
    val defaultFlavors: List<FlavorConfigData>
        get() = Flavor.values().map { flavor ->
            FlavorConfigData(
                name = flavor.flavorName,
                dimension = flavor.dimension.name.lowercase().replace("_", ""),
                isDefault = flavor.isDefault,
                applicationIdSuffix = flavor.applicationIdSuffix,
                bundleIdSuffix = flavor.bundleIdSuffix,
            )
        }

    /**
     * Checks if a specific flavor is currently active.
     */
    fun isFlavorActive(flavor: Flavor, activeVariant: String): Boolean =
        activeVariant.contains(flavor.flavorName, ignoreCase = true)

    /**
     * Gets the base URL for the current flavor.
     */
    fun getBaseUrl(flavor: Flavor): String = when (flavor) {
        Flavor.DEMO -> "https://demo-api.mifos.org"
        Flavor.PROD -> "https://api.mifos.org"
    }

    /**
     * Gets the analytics enabled flag for the current flavor.
     */
    fun isAnalyticsEnabled(flavor: Flavor): Boolean = when (flavor) {
        Flavor.DEMO -> false
        Flavor.PROD -> true
    }
}

/**
 * Data class for dimension configuration.
 */
data class DimensionConfig(
    val name: String,
    val priority: Int,
)

/**
 * Data class for flavor configuration.
 */
data class FlavorConfigData(
    val name: String,
    val dimension: String,
    val isDefault: Boolean = false,
    val applicationIdSuffix: String? = null,
    val bundleIdSuffix: String? = null,
    val buildConfigFields: Map<String, BuildConfigFieldData> = emptyMap(),
)

/**
 * Data class for BuildConfig field.
 */
data class BuildConfigFieldData(
    val type: String,
    val value: String,
)

/**
 * Extension function to configure KMP flavors using centralized configuration.
 */
fun configureKmpFlavors(
    extension: KmpFlavorExtension,
    dimensions: List<DimensionConfig>,
    flavors: List<FlavorConfigData>,
    generateBuildConfig: Boolean = true,
    buildConfigPackage: String? = null,
) {
    extension.apply {
        this.generateBuildConfig.set(generateBuildConfig)
        buildConfigPackage?.let { this.buildConfigPackage.set(it) }

        // Configure dimensions
        flavorDimensions {
            dimensions.forEach { dim ->
                register(dim.name) {
                    priority.set(dim.priority)
                }
            }
        }

        // Configure flavors
        this.flavors {
            flavors.forEach { flavorData ->
                register(flavorData.name) {
                    dimension.set(flavorData.dimension)
                    isDefault.set(flavorData.isDefault)
                    flavorData.applicationIdSuffix?.let { applicationIdSuffix.set(it) }
                    flavorData.bundleIdSuffix?.let { bundleIdSuffix.set(it) }

                    // Add standard BuildConfig fields
                    addStandardBuildConfigFields(this, flavorData)
                }
            }
        }
    }
}

/**
 * Adds standard BuildConfig fields to a flavor.
 */
private fun addStandardBuildConfigFields(
    flavorConfig: FlavorConfig,
    flavorData: FlavorConfigData,
) {
    val flavor = KmpFlavors.Flavor.values().find { it.flavorName == flavorData.name }
    if (flavor != null) {
        flavorConfig.apply {
            buildConfigField("String", "BASE_URL", "\"${KmpFlavors.getBaseUrl(flavor)}\"")
            buildConfigField("Boolean", "ANALYTICS_ENABLED", KmpFlavors.isAnalyticsEnabled(flavor).toString())
        }
    }

    // Add any custom fields
    flavorData.buildConfigFields.forEach { (name, field) ->
        flavorConfig.buildConfigField(field.type, name, field.value)
    }
}
