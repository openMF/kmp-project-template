#!/bin/bash
#
# Kotlin Multiplatform Project Customizer
# Based on Android template customizer
#

# Verify bash version. macOS comes with bash 3 preinstalled.
if [[ ${BASH_VERSINFO[0]} -lt 4 ]]
then
  echo "You need at least bash 4 to run this script."
  exit 1
fi

# exit when any command fails
set -e

if [[ $# -lt 2 ]]; then
   echo "Usage: bash customizer.sh my.new.package MyNewProject [ApplicationName]" >&2
   echo "Example: bash customizer.sh com.example.myapp MyKMPApp" >&2
   exit 2
fi

PACKAGE=$1
PROJECT_NAME=$2
APPNAME=${3:-$PROJECT_NAME}
SUBDIR=${PACKAGE//.//} # Replaces . with /
PROJECT_NAME_LOWERCASE=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]')

# Capitalize first letter for replacing "Mifos" prefix
capitalize_first() {
    echo "$1" | awk '{print toupper(substr($0,1,1)) substr($0,2)}'
}
PROJECT_NAME_CAPITALIZED=$(capitalize_first "$PROJECT_NAME")

# Convert kebab case to camel case
kebab_to_camel() {
    echo "$1" | sed -E 's/-([a-z])/\U\1/g'
}

# Function to escape dots in package name for sed
escape_dots() {
    echo "$1" | sed 's/\./\\./g'
}

# Escape dots in package for sed commands
ESCAPED_PACKAGE=$(escape_dots "$PACKAGE")

# Update convention plugin IDs in Gradle files and version catalogs
echo "Updating convention plugin IDs..."

# For .gradle.kts files
find ./ -type f -name "*.gradle.kts" -exec sed -i.bak "s/id(\"org\.mifos\./id(\"$ESCAPED_PACKAGE./g" {} \;

# For version catalog files (libs.versions.toml, build-logic/build-logic.versions.toml)
find ./ -type f -name "*.versions.toml" -exec sed -i.bak "s/id = \"org\.mifos\./id = \"$ESCAPED_PACKAGE./g" {} \;

# Update packageOfResClass in Gradle files
echo "Updating packageOfResClass..."
find ./ -type f -name "*.gradle.kts" -exec sed -i.bak "s/packageOfResClass = \"org\.mifos\.designsystem\.generated\.resources\"/packageOfResClass = \"$ESCAPED_PACKAGE.designsystem.generated.resources\"/g" {} \;

# Update references to renamed files in imports
echo "Updating import statements..."
find ./ -type f -name "*.kt" -exec sed -i.bak "s/import.*Mifos/import $PACKAGE.$PROJECT_NAME_CAPITALIZED/g" {} \;

# For convention plugins in build-logic
if [ -d "build-logic" ]; then
    echo "Updating build-logic plugin IDs..."
    find ./build-logic -type f -name "*.gradle.kts" -exec sed -i.bak "s/org\.mifos\./$ESCAPED_PACKAGE./g" {} \;
    echo "Update applied plugin applications in plugin classes"
    find ./build-logic -type f -name "*.kt" -exec sed -i.bak "s/apply(\"org\.mifos\./apply(\"$ESCAPED_PACKAGE./g" {} \;
fi

# Update iOS project files if they exist
if [ -d "mifos-ios" ]; then
    echo "Updating iOS project files"
    find ./mifos-ios -type f -name "*.xcodeproj" -exec sed -i.bak "s/PRODUCT_BUNDLE_IDENTIFIER = .*$/PRODUCT_BUNDLE_IDENTIFIER = $PACKAGE;/g" {} \;
    find ./mifos-ios -type f -name "*.plist" -exec sed -i.bak "s/PRODUCT_BUNDLE_IDENTIFIER = .*$/PRODUCT_BUNDLE_IDENTIFIER = $PACKAGE;/g" {} \;
fi

# Update specific plugin naming patterns
echo "Updating specific plugin names..."
PLUGIN_TYPES=(
    "cmp.feature"
    "kmp.koin"
    "kmp.library"
    "detekt.plugin"
    "git.hooks"
    "ktlint.plugin"
    "spotless.plugin"
)

for plugin_type in "${PLUGIN_TYPES[@]}"; do
    # Update in version catalog files
    find ./ -type f -name "*.versions.toml" -exec sed -i.bak "s/org\.mifos\.$plugin_type/$ESCAPED_PACKAGE.$plugin_type/g" {} \;
    # Update in gradle files
    find ./ -type f -name "*.gradle.kts" -exec sed -i.bak "s/org\.mifos\.$plugin_type/$ESCAPED_PACKAGE.$plugin_type/g" {} \;
done

# Function to process common module directories
process_module_dirs() {
    local module_path=$1
    local src_dirs=("main" "commonMain" "commonTest" "androidMain" "androidTest" "iosMain" "nativeMain" "iosTest" "desktopMain" "desktopTest" "jvmMain" "jvmTest" "jsMain" "jsTest" "wasmJsMain" "wasmJsTest")

    for src_dir in "${src_dirs[@]}"
    do
        local kotlin_dir="$module_path/src/$src_dir/kotlin"
        if [ -d "$kotlin_dir" ]; then
            echo "Processing $kotlin_dir"
            
            # Create new package directory structure if it doesn't exist
            mkdir -p "$kotlin_dir/$SUBDIR"

            # First, handle files in org/mifos if they exist
            if [ -d "$kotlin_dir/org/mifos" ]; then
                echo "Moving files from org/mifos to $SUBDIR"

                # Copy all files and directories recursively
                cp -r "$kotlin_dir/org/mifos"/* "$kotlin_dir/$SUBDIR/" 2>/dev/null || true

                # Update package declarations and imports in all Kotlin files under the new package directory
                if [ -d "$kotlin_dir/$SUBDIR" ]; then
                    echo "Updating package declarations and imports in $kotlin_dir/$SUBDIR"
                    find "$kotlin_dir/$SUBDIR" -type f -name "*.kt" -exec sed -i.bak \
                        -e "s/package org\.mifos/package $PACKAGE/g" \
                        -e "s/package com\.niyaj/package $PACKAGE/g" \
                        -e "s/import org\.mifos/import $PACKAGE/g" \
                        -e "s/import com\.niyaj/import $PACKAGE/g" {} \;
                fi

                # Remove old directory structure after copying
                echo "Removing old directory structure at $kotlin_dir/org/mifos"
                rm -rf "$kotlin_dir/org/mifos"
            fi
        fi
    done

    # Update imports in all Kotlin files within the module that might reference the old package
    find "$module_path" -type f -name "*.kt" -exec sed -i.bak "s/import org\.mifos/import $PACKAGE/g" {} \;
    find "$module_path" -type f -name "*.kt" -exec sed -i.bak "s/package org\.mifos/package $PACKAGE/g" {} \;
}

# Rename files that start with Mifos
echo "Renaming files with Mifos prefix..."
find . -type f -name "Mifos*.kt" | while read -r file; do
    newfile=$(echo "$file" | sed "s/Mifos/$PROJECT_NAME_CAPITALIZED/")
    echo "Renaming $file to $newfile"
    mv "$file" "$newfile"
done

# Update code elements that start with Mifos
echo "Updating code elements with Mifos prefix..."
find ./ -type f -name "*.kt" -exec sed -i.bak "s/Mifos\([A-Z][a-zA-Z0-9]*\)/$PROJECT_NAME_CAPITALIZED\1/g" {} \;
find ./ -type f -name "*.kt" -exec sed -i.bak "s/mifos\([A-Z][a-zA-Z0-9]*\)/${PROJECT_NAME_LOWERCASE}\1/g" {} \;

# Update settings.gradle.kts for module names
echo "Updating module names in settings.gradle.kts"
sed -i.bak "s/include(\":mifos-shared\")/include(\":$PROJECT_NAME_LOWERCASE-shared\")/g" settings.gradle.kts
sed -i.bak "s/include(\":mifos-android\")/include(\":$PROJECT_NAME_LOWERCASE-android\")/g" settings.gradle.kts
sed -i.bak "s/include(\":mifos-desktop\")/include(\":$PROJECT_NAME_LOWERCASE-desktop\")/g" settings.gradle.kts
sed -i.bak "s/include(\":mifos-web\")/include(\":$PROJECT_NAME_LOWERCASE-web\")/g" settings.gradle.kts
sed -i.bak "s/include(\":mifos-ios\")/include(\":$PROJECT_NAME_LOWERCASE-web\")/g" settings.gradle.kts

# Rename module directories
echo "Renaming module directories"
for platform in "shared" "android" "desktop" "web" "ios"
do
    if [ -d "mifos-$platform" ]; then
        echo "Renaming mifos-$platform to $PROJECT_NAME_LOWERCASE-$platform"
        mv "mifos-$platform" "$PROJECT_NAME_LOWERCASE-$platform"
    fi
done

# Update build.gradle.kts files to reference new module names using typesafe project accessors
echo "Updating module references in build files"
OLD_CAMEL_SHARED=$(kebab_to_camel "mifos-shared")
OLD_CAMEL_ANDROID=$(kebab_to_camel "mifos-android")
OLD_CAMEL_DESKTOP=$(kebab_to_camel "mifos-desktop")
OLD_CAMEL_WEB=$(kebab_to_camel "mifos-web")

NEW_CAMEL_SHARED=$(kebab_to_camel "$PROJECT_NAME_LOWERCASE-shared")
NEW_CAMEL_ANDROID=$(kebab_to_camel "$PROJECT_NAME_LOWERCASE-android")
NEW_CAMEL_DESKTOP=$(kebab_to_camel "$PROJECT_NAME_LOWERCASE-desktop")
NEW_CAMEL_WEB=$(kebab_to_camel "$PROJECT_NAME_LOWERCASE-web")

find ./ -type f -name "*.gradle.kts" -exec sed -i.bak "s/projects.$OLD_CAMEL_SHARED/projects.$NEW_CAMEL_SHARED/g" {} \;
find ./ -type f -name "*.gradle.kts" -exec sed -i.bak "s/projects.$OLD_CAMEL_ANDROID/projects.$NEW_CAMEL_ANDROID/g" {} \;
find ./ -type f -name "*.gradle.kts" -exec sed -i.bak "s/projects.$OLD_CAMEL_DESKTOP/projects.$NEW_CAMEL_DESKTOP/g" {} \;
find ./ -type f -name "*.gradle.kts" -exec sed -i.bak "s/projects.$OLD_CAMEL_WEB/projects.$NEW_CAMEL_WEB/g" {} \;

# Update run configurations
echo "Updating run configurations"
if [ -d ".run" ]; then
    find .run -type f -name "*.xml" -exec sed -i.bak "s/name=\"mifos-/name=\"$PROJECT_NAME_LOWERCASE-/g" {} \;
    find .run -type f -name "*.xml" -exec sed -i.bak "s/module name=\"[^\"]*\.mifos-/module name=\"$PROJECT_NAME_LOWERCASE\.$PROJECT_NAME_LOWERCASE-/g" {} \;
    find .run -type f -name "*.xml" -exec sed -i.bak "s/kmp-project-template\.mifos-/$PROJECT_NAME_LOWERCASE\.$PROJECT_NAME_LOWERCASE-/g" {} \;

    # Update taskNames in run configurations for desktop and web
    find .run -type f -name "*.xml" -exec sed -i.bak "s/:mifos-desktop/:$PROJECT_NAME_LOWERCASE-desktop/g" {} \;
    find .run -type f -name "*.xml" -exec sed -i.bak "s/:mifos-web/:$PROJECT_NAME_LOWERCASE-web/g" {} \;

    # Handle specific taskNames patterns
    find .run -type f -name "*.xml" -exec sed -i.bak "s/value=\":mifos-desktop:/value=\":$PROJECT_NAME_LOWERCASE-desktop:/g" {} \;
    find .run -type f -name "*.xml" -exec sed -i.bak "s/value=\":mifos-web:/value=\":$PROJECT_NAME_LOWERCASE-web:/g" {} \;

    # Then, rename the run configuration files themselves
    for config_file in .run/mifos-*.run.xml; do
        if [ -f "$config_file" ]; then
            new_config_file=$(echo "$config_file" | sed "s/mifos-/$PROJECT_NAME_LOWERCASE-/")
            echo "Renaming run configuration file: $config_file → $new_config_file"
            mv "$config_file" "$new_config_file"
        fi
    done
fi

# Process all modules
echo "Processing module contents..."
for module in $(find . -type f -name "build.gradle.kts" -not -path "*/build/*" -exec dirname {} \;)
do
    echo "Found module: $module"
    process_module_dirs "$module"
done

# Rename package and imports in Kotlin files
echo "Renaming packages to $PACKAGE"
find ./ -type f -name "*.kt" -exec sed -i.bak "s/package org\.mifos/package $PACKAGE/g" {} \;
find ./ -type f -name "*.kt" -exec sed -i.bak "s/import org\.mifos/import $PACKAGE/g" {} \;

# Update Gradle files
echo "Updating Gradle files"
find ./ -type f -name "*.gradle.kts" -exec sed -i.bak "s/org\.mifos/$PACKAGE/g" {} \;
find ./ -type f -name "settings.gradle.kts" -exec sed -i.bak "s/rootProject\.name = \".*\"/rootProject.name = \"$PROJECT_NAME\"/g" {} \;

# Update Android manifest if it exists
if [ -f "$PROJECT_NAME_LOWERCASE-android/src/main/AndroidManifest.xml" ]; then
    echo "Updating Android Manifest"
    sed -i.bak "s/package=\".*\"/package=\"$PACKAGE\"/" "$PROJECT_NAME_LOWERCASE-android/src/main/AndroidManifest.xml"
fi

# Rename application class if needed
if [[ $APPNAME != MyApplication ]]
then
    echo "Renaming application to $APPNAME"
    find ./ -type f \( -name "*.kt" -or -name "*.gradle.kts" -or -name "*.xml" \) -exec sed -i.bak "s/MifosApp/$APPNAME/g" {} \;
    find ./ -name "MifosApp.kt" | sed "p;s/MifosApp/$APPNAME/" | tr '\n' '\0' | xargs -0 -n 2 mv 2>/dev/null || true
fi

# Clean up backup files
echo "Cleaning up"
find . -name "*.bak" -type f -delete

# Remove script and git directory if needed
echo "Removing additional files"
#rm -rf customizer.sh
#rm -rf .git/

echo "Done! Your Kotlin Multiplatform project has been customized with:"
echo "- Updated convention plugin IDs to use $PACKAGE"
echo "- Renamed modules and configurations"
echo "- Updated file and function names"
echo "- Renamed modules"
echo "- Updated run configurations"
echo "- Renamed Mifos-prefixed files and functions to $PROJECT_NAME_CAPITALIZED"
echo "- projects.$NEW_CAMEL_SHARED"
echo "- projects.$NEW_CAMEL_ANDROID"
echo "- projects.$NEW_CAMEL_DESKTOP"
echo "- projects.$NEW_CAMEL_WEB"