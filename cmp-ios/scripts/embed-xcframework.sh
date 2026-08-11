#!/usr/bin/env bash
#
# embed-xcframework.sh — flavor-aware KMP framework embed for Xcode (E6).
#
# Invoked as an Xcode Run-Script build phase on the `iosApp` target (see
# iosApp.xcodeproj/project.pbxproj → PBXShellScriptBuildPhase "[KMP] Embed and
# Sign ComposeApp XCFramework"). It runs BEFORE Compile Sources so the exported
# `ComposeApp` framework is built, code-signed and copied into the app bundle for
# every build — the SwiftPM/XCFramework replacement for the CocoaPods
# `[CP] Embed Pods Frameworks` phase this project used to carry.
#
# ── Flavor-aware build-type mapping (the piece the CocoaPods plugin used to do) ──
# The old `cmp-shared/build.gradle.kts` `cocoapods { xcodeConfigurationToNativeBuildType[…] }`
# block mapped every `{flavor}{BuildType}` Xcode configuration (demoDebug, demoStaging,
# demoRelease, prodDebug, prodStaging, prodRelease — from the kmp-product-flavors DSL)
# to a Kotlin/Native build type: a *debuggable* build type → DEBUG, everything else
# → RELEASE. We reproduce EXACTLY that here from Xcode's `$CONFIGURATION`:
#     *Debug  -> Debug   (debuggable)
#     *        -> Release (Staging + Release are optimized/non-debuggable)
# The KMP `embedAndSignAppleFrameworkForXcode` task only understands the canonical
# `Debug`/`Release` configuration names, so we hand it the mapped name via the
# `CONFIGURATION` env var — that is what preserves per-variant iOS builds after the
# CocoaPods removal.
set -euo pipefail

# SRCROOT is exported by Xcode (…/cmp-ios). The Gradle wrapper lives at the repo root,
# one level up from cmp-ios. Fall back to a relative resolve when run outside Xcode.
SRCROOT="${SRCROOT:-$(cd "$(dirname "$0")/.." && pwd)}"
REPO_ROOT="$(cd "$SRCROOT/.." && pwd)"
GRADLEW="$REPO_ROOT/gradlew"

CONFIG="${CONFIGURATION:-prodRelease}"
case "$CONFIG" in
  *Debug|*debug) KOTLIN_BUILD_TYPE="Debug" ;;   # debuggable → DEBUG slice
  *)             KOTLIN_BUILD_TYPE="Release" ;;  # Staging + Release → RELEASE slice
esac

echo "note: [KMP] embed-xcframework — Xcode CONFIGURATION=$CONFIG → Kotlin build type $KOTLIN_BUILD_TYPE"

if [ ! -x "$GRADLEW" ]; then
  echo "error: gradlew not found or not executable at $GRADLEW" >&2
  exit 1
fi

# Hand the KMP embed task the canonical Debug/Release name it recognizes (overriding
# the custom {flavor}{BuildType} CONFIGURATION), while leaving Xcode's own build
# environment (SDK_NAME, ARCHS, TARGET_BUILD_DIR, FRAMEWORKS_FOLDER_PATH,
# EXPANDED_CODE_SIGN_IDENTITY, …) intact so the framework is signed + embedded into
# the right slice/arch. embedAndSignAppleFrameworkForXcode builds the matching
# `binaries.framework { baseName = "ComposeApp" }` and copies it into the app bundle.
CONFIGURATION="$KOTLIN_BUILD_TYPE" \
  "$GRADLEW" -p "$REPO_ROOT" \
  ":cmp-shared:embedAndSignAppleFrameworkForXcode"

# Mirror the built framework to the raw `$CONFIGURATION`-named search dir so the
# project's `FRAMEWORK_SEARCH_PATHS`
#   $(SRCROOT)/../cmp-shared/build/xcode-frameworks/$(KMPF_VARIANT)/$(SDK_NAME)
# (KMPF_VARIANT = demoDebug / prodStaging / …, i.e. the raw Xcode CONFIGURATION)
# resolves at LINK time for every flavor — the embed task writes only to the
# canonical Debug/Release dir we passed above. No-op when the names already match
# (the plain Debug/Release configurations).
SDK="${SDK_NAME:-iphoneos}"
XCF_ROOT="$REPO_ROOT/cmp-shared/build/xcode-frameworks"
SRC_DIR="$XCF_ROOT/$KOTLIN_BUILD_TYPE/$SDK"
DST_DIR="$XCF_ROOT/$CONFIG/$SDK"
if [ "$SRC_DIR" != "$DST_DIR" ] && [ -d "$SRC_DIR" ]; then
  echo "note: [KMP] mirroring $KOTLIN_BUILD_TYPE framework → xcode-frameworks/$CONFIG/$SDK for FRAMEWORK_SEARCH_PATHS"
  mkdir -p "$DST_DIR"
  # -a preserves the .framework bundle; delete stale contents first for a clean mirror.
  rm -rf "${DST_DIR:?}/ComposeApp.framework"
  cp -a "$SRC_DIR/." "$DST_DIR/"
fi
