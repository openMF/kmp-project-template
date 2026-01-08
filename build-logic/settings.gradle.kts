dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.3.0"
    }
}


rootProject.name = "build-logic"
include(":convention")
