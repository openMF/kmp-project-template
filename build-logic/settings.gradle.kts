pluginManagement {
    // Composite-include the upstream kmp-product-flavors build-logic so this
    // template (and every downstream consumer that syncs build-logic from us)
    // compiles against the plugin's SOURCE rather than a published artefact.
    // Lets us iterate on plugin fixes without going through a release cycle.
    // Path assumes the standard MobileByteSensei workspaces/ layout:
    //   workspaces/
    //     ├── mbs/kmp-product-flavors/source/kmp-product-flavors/build-logic
    //     └── mifos-x/kmp-project-template/source/kmp-project-template/build-logic   <-- you are here
    includeBuild("../../../../../mbs/kmp-product-flavors/source/kmp-product-flavors/build-logic") {
        name = "kmp-product-flavors-build-logic"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

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

rootProject.name = "build-logic"
include(":convention")
