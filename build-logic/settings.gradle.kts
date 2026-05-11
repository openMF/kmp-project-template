pluginManagement {
    // OPTIONAL composite-include of the upstream kmp-product-flavors build-logic.
    // When the developer's checkout follows the standard MobileByteSensei
    // workspaces/ layout, the plugin compiles from SOURCE so we can iterate
    // without a release cycle:
    //   workspaces/
    //     ├── mbs/kmp-product-flavors/source/kmp-product-flavors/build-logic
    //     └── mifos-x/kmp-project-template/source/kmp-project-template/build-logic
    //
    // When the path doesn't exist (CI, contributor's flat checkout), the
    // plugin resolves from Maven Central / Gradle Plugin Portal as a published
    // artefact (pinned via libs.versions.toml -> kmpProductFlavors).
    val upstreamFlavorPluginBuildLogic = file("../../../../../mbs/kmp-product-flavors/source/kmp-product-flavors/build-logic")
    if (upstreamFlavorPluginBuildLogic.exists()) {
        includeBuild(upstreamFlavorPluginBuildLogic) {
            name = "kmp-product-flavors-build-logic"
        }
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
