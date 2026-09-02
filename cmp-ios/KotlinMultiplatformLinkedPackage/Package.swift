// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "KotlinMultiplatformLinkedPackage",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "KotlinMultiplatformLinkedPackage",
      type: .none,
      targets: ["KotlinMultiplatformLinkedPackage"]
    )
  ],
  dependencies: [
    .package(path: "subpackages/dev_gitlive_firebase_crashlytics_3_0_0_alpha01"),
    .package(path: "subpackages/dev_gitlive_firebase_analytics_3_0_0_alpha01"),
    .package(path: "subpackages/dev_gitlive_firebase_app_3_0_0_alpha01")
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackage",
      dependencies: [
        .product(name: "dev_gitlive_firebase_crashlytics_3_0_0_alpha01", package: "dev_gitlive_firebase_crashlytics_3_0_0_alpha01"),
        .product(name: "dev_gitlive_firebase_analytics_3_0_0_alpha01", package: "dev_gitlive_firebase_analytics_3_0_0_alpha01"),
        .product(name: "dev_gitlive_firebase_app_3_0_0_alpha01", package: "dev_gitlive_firebase_app_3_0_0_alpha01")
      ]
    )
  ]
)
