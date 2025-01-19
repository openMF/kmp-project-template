import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "org.convention.buildlogic"

// Configure the build-logic plugins to target JDK 19
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
    compileOnly(libs.spotless.gradle)
    implementation(libs.truth)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        // Android Plugins
        register("androidApplicationCompose") {
            id = "org.convention.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidApplication") {
            id = "org.convention.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }

        register("androidFlavors") {
            id = "org.convention.android.application.flavors"
            implementationClass = "AndroidApplicationFlavorsConventionPlugin"
        }

        // KMP & CMP Plugins
        register("cmpFeature") {
            id = "org.convention.cmp.feature"
            implementationClass = "CMPFeatureConventionPlugin"
        }

        register("kmpKoin") {
            id = "org.convention.kmp.koin"
            implementationClass = "KMPKoinConventionPlugin"
        }
        register("kmpLibrary") {
            id = "org.convention.kmp.library"
            implementationClass = "KMPLibraryConventionPlugin"
        }

        // Static Analysis & Formatting Plugins
        register("detekt") {
            id = "org.convention.detekt.plugin"
            implementationClass = "DetektConventionPlugin"
            description = "Configures detekt for the project"
        }
        register("spotless") {
            id = "org.convention.spotless.plugin"
            implementationClass = "SpotlessConventionPlugin"
            description = "Configures spotless for the project"
        }
        register("ktlint") {
            id = "org.convention.ktlint.plugin"
            implementationClass = "KtlintConventionPlugin"
            description = "Configures kotlinter for the project"
        }
        register("gitHooks") {
            id = "org.convention.git.hooks"
            implementationClass = "GitHooksConventionPlugin"
            description = "Installs git hooks for the project"
        }
    }
}
