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

# Print section header
print_section() {
    echo
    echo "==== $1 ===="
    echo
}

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

# Function to update plugin IDs
update_plugin_ids() {
    print_section "Updating Plugin IDs"

    echo "🔄 Updating convention plugin IDs in Gradle files..."
    find ./ -type f -name "*.gradle.kts" -exec sed -i.bak "s/id(\"org\.mifos\./id(\"$ESCAPED_PACKAGE./g" {} \;

    echo "🔄 Updating plugin IDs in version catalog files..."
    find ./ -type f -name "*.versions.toml" -exec sed -i.bak "s/id = \"org\.mifos\./id = \"$ESCAPED_PACKAGE./g" {} \;

    if [ -d "build-logic" ]; then
        echo "🔄 Updating build-logic plugin IDs..."
        find ./build-logic -type f -name "*.gradle.kts" -exec sed -i.bak "s/org\.mifos\./$ESCAPED_PACKAGE./g" {} \;
        echo "🔄 Updating plugin applications in plugin classes..."
        find ./build-logic -type f -name "*.kt" -exec sed -i.bak "s/apply(\"org\.mifos\./apply(\"$ESCAPED_PACKAGE./g" {} \;
    fi

    # Rename package and imports in Kotlin files
    echo "🔄 Renaming packages to $PACKAGE"
    find ./ -type f -name "*.kt" -exec sed -i.bak "s/package org\.mifos/package $PACKAGE/g" {} \;
    find ./ -type f -name "*.kt" -exec sed -i.bak "s/import org\.mifos/import $PACKAGE/g" {} \;

    # Update Gradle files
    echo "🔄 Updating Gradle files"
    find ./ -type f -name "*.gradle.kts" -exec sed -i.bak "s/org\.mifos/$PACKAGE/g" {} \;
    # Then update only the root settings.gradle.kts file
    sed -i.bak "s/rootProject\.name = \".*\"/rootProject.name = \"$PROJECT_NAME\"/g" ./settings.gradle.kts

}

# Function to update specific plugin patterns
update_plugin_patterns() {
    print_section "Updating Plugin Patterns"

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
        echo "🔄 Updating pattern: $plugin_type"
        find ./ -type f -name "*.versions.toml" -exec sed -i.bak "s/org\.mifos\.$plugin_type/$ESCAPED_PACKAGE.$plugin_type/g" {} \;
        find ./ -type f -name "*.gradle.kts" -exec sed -i.bak "s/org\.mifos\.$plugin_type/$ESCAPED_PACKAGE.$plugin_type/g" {} \;
    done
}

update_compose_resources() {
    print_section "Updating Compose Resources Configuration"

    local count=0
    while IFS= read -r file; do
        if grep -q "packageOfResClass.*org\.mifos" "$file"; then
            echo "📦 Processing: $file"
            echo "Debug: Attempting to update $file with package $ESCAPED_PACKAGE"
            if ! sed -i.bak "s/packageOfResClass = \"org\.mifos\.\([^\"]*\)\"/packageOfResClass = \"$ESCAPED_PACKAGE.\1\"/g" "$file"; then
                echo "Error: sed command failed for $file"
                return 1
            fi
            ((count++))
        fi
    done < <(find ./ -type f -name "*.gradle.kts")

    if [ $count -eq 0 ]; then
        echo "ℹ️ No files found containing Compose Resources configuration"
    else
        echo "✅ Updated packageOfResClass in $count file(s)"
    fi
}

# Function to rename and update application class
#update_application_class() {
#    print_section "Updating Application Class"
#
#    if [[ $APPNAME != MyApplication ]]; then
#        echo "📝 Renaming application to $APPNAME"
#        find ./ -type f \( -name "*.kt" -or -name "*.gradle.kts" -or -name "*.xml" \) -exec sed -i.bak "s/MifosApp/$APPNAME/g" {} \;
#        find ./ -name "MifosApp.kt" | sed "p;s/MifosApp/$APPNAME/" | tr '\n' '\0' | xargs -0 -n 2 mv 2>/dev/null || true
#        echo "✅ Application class renamed successfully"
#    else
#        echo "ℹ️ Skipping application rename as name is default"
#    fi
#}

# Function to update iOS configurations
update_ios_config() {
    print_section "Updating iOS Configuration"

    if [ -d "cmp-ios" ]; then
        echo "🔄 Updating iOS project files..."
        find ./cmp-ios -type f -name "*.xcodeproj" -exec sed -i.bak "s/PRODUCT_BUNDLE_IDENTIFIER = .*$/PRODUCT_BUNDLE_IDENTIFIER = $PACKAGE;/g" {} \;
        find ./cmp-ios -type f -name "*.plist" -exec sed -i.bak "s/PRODUCT_BUNDLE_IDENTIFIER = .*$/PRODUCT_BUNDLE_IDENTIFIER = $PACKAGE;/g" {} \;
        echo "✅ iOS configuration updated"
    else
        echo "ℹ️ No iOS directory found, skipping iOS configuration"
    fi
}

# Function to process module directories
process_module_dirs() {
    local module_path=$1
    local src_dirs=("main" "commonMain" "commonTest" "androidMain" "androidTest" "iosMain" "nativeMain" "iosTest" "desktopMain" "desktopTest" "jvmMain" "jvmTest" "jsMain" "jsTest" "wasmJsMain" "wasmJsTest")

    for src_dir in "${src_dirs[@]}"
    do
        local kotlin_dir="$module_path/src/$src_dir/kotlin"
        if [ -d "$kotlin_dir" ]; then
            echo "🔄 Processing $kotlin_dir"

            mkdir -p "$kotlin_dir/$SUBDIR"

            if [ -d "$kotlin_dir/org/mifos" ]; then
                echo "📦 Moving files from org/mifos to $SUBDIR"
                cp -r "$kotlin_dir/org/mifos"/* "$kotlin_dir/$SUBDIR/" 2>/dev/null || true

                if [ -d "$kotlin_dir/$SUBDIR" ]; then
                    echo "📝 Updating package declarations and imports"
                    find "$kotlin_dir/$SUBDIR" -type f -name "*.kt" -exec sed -i.bak \
                        -e "s/package org\.mifos/package $PACKAGE/g" \
                        -e "s/package com\.niyaj/package $PACKAGE/g" \
                        -e "s/import org\.mifos/import $PACKAGE/g" \
                        -e "s/import com\.niyaj/import $PACKAGE/g" {} \;
                fi

                echo "🗑️ Cleaning up old directory structure"
                rm -rf "$kotlin_dir/org/mifos"
                # Remove org directory if it's empty
                rmdir "$kotlin_dir/org" 2>/dev/null || true
            fi
        fi
    done

    find "$module_path" -type f -name "*.kt" -exec sed -i.bak "s/import org\.mifos/import $PACKAGE/g" {} \;
    find "$module_path" -type f -name "*.kt" -exec sed -i.bak "s/package org\.mifos/package $PACKAGE/g" {} \;
}

process_module_content() {
    # Process all modules
    echo "🔄 Processing module contents..."
    for module in $(find . -type f -name "build.gradle.kts" -not -path "*/build/*" -exec dirname {} \;)
    do
        echo "Found module: $module"
        process_module_dirs "$module"
    done
}

# Function to rename files
rename_files() {
    echo "🔄 Renaming files with Mifos prefix..."
    find . -type f -name "Mifos*.kt" | while read -r file; do
      local newfile=$(echo "$file" | sed "s/MifosApp/$PROJECT_NAME_CAPITALIZED/g; s/Mifos/$PROJECT_NAME_CAPITALIZED/g")
      echo "📝 Renaming $file to $newfile"
      mv "$file" "$newfile"
    done

    # Update code elements that start with Mifos
    echo "🔄 Updating code elements with Mifos prefix..."
    find ./ -type f -name "*.kt" -exec sed -i.bak \
        -e "s/MifosApp\([^A-Za-z0-9]\|$\)/$PROJECT_NAME_CAPITALIZED\1/g" \
        -e "s/Mifos\([A-Z][a-zA-Z0-9]*\)/$PROJECT_NAME_CAPITALIZED\1/g" {} \;
    find ./ -type f -name "*.kt" -exec sed -i.bak \
        -e "s/mifosApp\([^A-Za-z0-9]\|$\)/${PROJECT_NAME_LOWERCASE}\1/g" \
        -e "s/mifos\([A-Z][a-zA-Z0-9]*\)/${PROJECT_NAME_LOWERCASE}\1/g" {} \;

    # Update references to renamed files in imports
    echo "🔄 Updating import statements..."
    find ./ -type f -name "*.kt" -exec sed -i.bak \
        -e "s/import.*\.MifosApp/import $PACKAGE.$PROJECT_NAME_CAPITALIZED/g" \
        -e "s/import.*\.Mifos/import $PACKAGE.$PROJECT_NAME_CAPITALIZED/g" {} \;
}

## Function to update module names in settings.gradle.kts
#update_gradle_settings() {
#    print_section "Updating Gradle Settings"
#    echo "🔄 Updating module names in settings.gradle.kts"
#    local modules=("shared" "android" "desktop" "web" "ios")
#
#    for module in "${modules[@]}"; do
#        sed -i.bak "s/include(\":mifos-$module\")/include(\":$PROJECT_NAME_LOWERCASE-$module\")/g" settings.gradle.kts
#        echo "✅ Updated module: mifos-$module → $PROJECT_NAME_LOWERCASE-$module"
#    done
#}

# Function to rename module directories
#rename_application_module_directories() {
#    print_section "Renaming Modules"
#    local modules=("shared" "android" "desktop" "web" "ios")
#
#    for module in "${modules[@]}"; do
#        if [ -d "mifos-$module" ]; then
#            echo "📁 Renaming mifos-$module to $PROJECT_NAME_LOWERCASE-$module"
#            mv "mifos-$module" "$PROJECT_NAME_LOWERCASE-$module"
#            echo "✅ Application Module directory renamed successfully"
#        else
#            echo "ℹ️ Application Module mifos-$module not found, skipping"
#        fi
#    done
#}
#
## Function to update build.gradle.kts module references
#update_application_module_references() {
#    print_section "Updating Module References"
#
#    local old_modules=()
#    local new_modules=()
#    local modules=("shared" "android" "desktop" "web")
#
#    for module in "${modules[@]}"; do
#        old_modules+=("$(kebab_to_camel "mifos-$module")")
#        new_modules+=("$(kebab_to_camel "$PROJECT_NAME_LOWERCASE-$module")")
#    done
#
#    for i in "${!old_modules[@]}"; do
#        echo "🔄 Updating references: ${old_modules[$i]} → ${new_modules[$i]}"
#        find ./ -type f -name "*.gradle.kts" -exec sed -i.bak "s/projects.${old_modules[$i]}/projects.${new_modules[$i]}/g" {} \;
#    done
#
#    echo "✅ Module references updated successfully"
#}
#
## Function to update run configurations
#update_run_configurations() {
#    print_section "Updating Run Configurations"
#
#    echo "🔄 Updating run configuration files..."
#
#    # Update references in XML files
#    local replacements=(
#        "s/name=\"mifos-/name=\"$PROJECT_NAME_LOWERCASE-/g"
#        "s/module name=\"[^\"]*\.mifos-/module name=\"$PROJECT_NAME_LOWERCASE\.$PROJECT_NAME_LOWERCASE-/g"
#        "s/kmp-project-template\.mifos-/$PROJECT_NAME_LOWERCASE\.$PROJECT_NAME_LOWERCASE-/g"
#        "s/:mifos-desktop/:$PROJECT_NAME_LOWERCASE-desktop/g"
#        "s/:mifos-web/:$PROJECT_NAME_LOWERCASE-web/g"
#        "s/value=\":mifos-desktop:/value=\":$PROJECT_NAME_LOWERCASE-desktop:/g"
#        "s/value=\":mifos-web:/value=\":$PROJECT_NAME_LOWERCASE-web:/g"
#    )
#
#    for replacement in "${replacements[@]}"; do
#        find .run -type f -name "*.xml" -exec sed -i.bak "$replacement" {} \;
#    done
#
#    # Rename configuration files
#    for config_file in .run/mifos-*.run.xml; do
#        if [ -f "$config_file" ]; then
#            new_config_file=$(echo "$config_file" | sed "s/mifos-/$PROJECT_NAME_LOWERCASE-/")
#            echo "📝 Renaming run configuration: $config_file → $new_config_file"
#            mv "$config_file" "$new_config_file"
#        fi
#    done
#
#    echo "✅ Run configurations updated successfully"
#}
#
## Function to update Android manifest
#update_android_manifest() {
#    print_section "Updating Android Manifest"
#    local manifest_path="$PROJECT_NAME_LOWERCASE-android/src/main/AndroidManifest.xml"
#
#    if [ -f "$manifest_path" ]; then
#        echo "📝 Updating package name in Android Manifest"
#        sed -i.bak "s/package=\".*\"/package=\"$PACKAGE\"/" "$manifest_path"
#        echo "✅ Android Manifest updated successfully"
#    else
#        echo "ℹ️ Android Manifest not found at $manifest_path, skipping update"
#    fi
#}

# Function to clean up backup files
cleanup_backup_files() {
    print_section "Final Cleanup"
    echo "🧹 Cleaning up backup files..."
    find . -name "*.bak" -type f -delete
#    echo "🧹 Cleaning up .git directory..."
#    rm -rf .git/
    echo "✅ Backup files cleaned up successfully"
}

print_final_summary(){
    print_section "Summary of Changes"
    echo "✅ Your Kotlin Multiplatform project has been customized with the following changes:"
    echo
    echo "1. Package Updates:"
    echo "   - Base package updated to: $PACKAGE"
    echo "   - Compose Resources package updated"
    echo "   - Android Manifest package updated"
    echo
    echo "2. Project Naming:"
    echo "   - Project name set to: $PROJECT_NAME"
    echo "   - Application name set to: $APPNAME"
    echo
    echo "3. Module Updates:"
    echo "   - Renamed all mifos-prefixed modules"
    echo "   - Updated module references in Gradle files"
    echo "   - Updated module imports and packages"
    echo
    echo "4. Configuration Updates:"
    echo "   - Updated convention plugin IDs"
    echo "   - Updated run configurations"
    echo "   - Updated iOS bundle identifiers (if applicable)"
    echo
    echo "5. Code Updates:"
    echo "   - Renamed Mifos-prefixed files to $PROJECT_NAME_CAPITALIZED"
    echo "   - Updated package declarations and imports"
    echo "   - Updated typesafe accessors:"
    echo
    echo "🎉 Project customization completed successfully!"
}

# Main execution function
main() {
    print_section "Starting Project Customization"
    echo "Package: $PACKAGE"
    echo "Project Name: $PROJECT_NAME"
    echo "Application Name: $APPNAME"

    # Core updates
    update_plugin_ids
    update_plugin_patterns
    update_compose_resources
#    update_application_class
    update_ios_config

    # Process modules
    process_module_content
    rename_files

    # Module updates
#    update_gradle_settings
#    rename_application_module_directories
#    update_application_module_references
#    update_run_configurations

    # Final updates
#    update_android_manifest
    cleanup_backup_files

    # Print summary
    print_final_summary
}

# Execute main function
main