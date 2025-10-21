#!/bin/bash
#
# Kotlin Multiplatform Project Customizer
#

# Colors and formatting
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Emoji indicators
CHECK_MARK="✅"
WARNING="⚠️"
ROCKET="🚀"
GEAR="⚙️"
PACKAGE="📦"
CLEAN="🧹"
PENCIL="📝"

# Verify bash version. macOS comes with bash 3 preinstalled.
if [[ ${BASH_VERSINFO[0]} -lt 4 ]]
then
  echo -e "${RED}${WARNING} You need at least bash 4 to run this script.${NC}"
  exit 1
fi

# exit when any command fails
set -e

# Print section header with design
print_section() {
    echo
    echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC} ${BOLD}$1${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
    echo
}

# Print success message
print_success() {
    echo -e "${GREEN}${CHECK_MARK} $1${NC}"
}

# Print warning message
print_warning() {
    echo -e "${YELLOW}${WARNING} $1${NC}"
}

# Print info message
print_info() {
    echo -e "${CYAN}${GEAR} $1${NC}"
}

# Print processing message
print_processing() {
    echo -e "${PURPLE}${ROCKET} $1${NC}"
}

print_error() {
    echo -e "${RED}${WARNING} $1${NC}"
}

if [[ $# -lt 2 ]]; then
    echo -e "${RED}${WARNING} Invalid arguments${NC}"
    echo -e "${CYAN}Usage: bash customizer.sh my.new.package MyNewProject [ApplicationName]${NC}"
    echo -e "${CYAN}Example: bash customizer.sh com.example.myapp MyKMPApp${NC}"
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

update_compose_resources() {
    print_section "Updating Compose Resources"

    local count=0
    local file

    while IFS= read -r file; do
        if grep -q "packageOfResClass.*org\.mifos" "$file"; then
            print_processing "Processing: $file"
            if ! sed -i.bak "s/packageOfResClass = \"org\.mifos\.\([^\"]*\)\"/packageOfResClass = \"$ESCAPED_PACKAGE.\1\"/g" "$file"; then
                echo -e "${RED}Error: sed command failed for $file${NC}"
                return 1
            fi
            count=$((count + 1))
        fi
    done < <(find . -type f -name "*.gradle.kts" -not -path "*/build/*")

    if [ $count -eq 0 ]; then
        print_warning "No files found containing Compose Resources"
    else
        print_success "Updated configurations in $count file(s)"
    fi
}

update_package_namespace() {
    print_section "Updating Package Namespace"

    local count=0
    local file

    while IFS= read -r file; do
        if grep -q "namespace = \"org\.mifos" "$file"; then
            print_processing "Updating namespace in: $file"
            if ! sed -i.bak "s/namespace = \"org\.mifos\.\([^\"]*\)\"/namespace = \"$ESCAPED_PACKAGE.\1\"/g" "$file"; then
                echo -e "${RED}Error: sed command failed for namespace in $file${NC}"
                return 1
            fi
            count=$((count + 1))
        fi
    done < <(find . -type f -name "*.gradle.kts" -not -path "*/build/*")

    if [ $count -eq 0 ]; then
        print_warning "No files found containing namespace"
    else
        print_success "Updated configurations in $count file(s)"
    fi
}

update_root_project_name() {
    print_section "Updating Root Project Name"

    local settings_file="settings.gradle.kts"

    if [ ! -f "$settings_file" ]; then
        print_error "settings.gradle.kts file not found in current directory"
        return 1
    fi

    print_processing "Updating rootProject.name in $settings_file"

    # Check if the line already exists
    if grep -qE '^\s*rootProject\.name\s*=' "$settings_file"; then
        # Replace using BSD/GNU compatible sed
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS (BSD sed requires backup suffix)
            sed -i '' -E "s|^\s*rootProject\.name\s*=.*|rootProject.name = \"$PROJECT_NAME\"|" "$settings_file"
        else
            # Linux/GNU sed
            sed -i -E "s|^\s*rootProject\.name\s*=.*|rootProject.name = \"$PROJECT_NAME\"|" "$settings_file"
        fi
        print_success "Updated rootProject.name to \"$PROJECT_NAME\""
    else
        # Insert at the top of the file
        tmpfile=$(mktemp)
        echo "rootProject.name = \"$PROJECT_NAME\"" > "$tmpfile"
        cat "$settings_file" >> "$tmpfile"
        mv "$tmpfile" "$settings_file"
        print_success "Inserted rootProject.name = \"$PROJECT_NAME\" at the top of $settings_file"
    fi
}

update_fastlane_config() {
    print_section "Updating Fastlane Configuration"

    local config_file="fastlane-config/project_config.rb"

    if [ ! -f "$config_file" ]; then
        print_warning "Fastlane config file not found at $config_file"
        return 0
    fi

    print_processing "Updating project configuration in $config_file"

    # Update PROJECT_NAME
    if grep -q "PROJECT_NAME = " "$config_file"; then
        sed -i.bak "s/PROJECT_NAME = \"[^\"]*\"/PROJECT_NAME = \"$PROJECT_NAME\"/g" "$config_file"
        print_success "Updated PROJECT_NAME to '$PROJECT_NAME'"
    fi

    # Update ORGANIZATION_NAME
    if grep -q "ORGANIZATION_NAME = " "$config_file"; then
        sed -i.bak "s/ORGANIZATION_NAME = \"[^\"]*\"/ORGANIZATION_NAME = \"$PROJECT_NAME\"/g" "$config_file"
        print_success "Updated ORGANIZATION_NAME to '$PROJECT_NAME'"
    fi

    # Update Android package_name in ANDROID hash (with original package pattern)
    if grep -q 'package_name: "cmp\.android\.app"' "$config_file"; then
        sed -i.bak 's/package_name: "cmp\.android\.app"/package_name: "'"$PACKAGE"'"/g' "$config_file"
        print_success "Updated Android package_name to '$PACKAGE'"
    elif grep -q 'package_name: "[^"]*"' "$config_file"; then
        # Fallback: if original package not found, update any package_name in ANDROID section
        sed -i.bak '/ANDROID = {/,/^[[:space:]]*}/ s/package_name: "[^"]*"/package_name: "'"$PACKAGE"'"/' "$config_file"
        print_success "Updated Android package_name to '$PACKAGE'"
    fi

    # Update iOS app_identifier (look for any pattern, not just org.mifos)
    if grep -q 'app_identifier: "com\.niyaj' "$config_file"; then
        sed -i.bak 's/app_identifier: "com\.niyaj\.[^"]*"/app_identifier: "'"$PACKAGE"'"/g' "$config_file"
        print_success "Updated iOS app_identifier to '$PACKAGE'"
    elif grep -q 'app_identifier: "org\.mifos' "$config_file"; then
        sed -i.bak 's/app_identifier: "org\.mifos\.[^"]*"/app_identifier: "'"$PACKAGE"'"/g' "$config_file"
        print_success "Updated iOS app_identifier to '$PACKAGE'"
    elif grep -q 'app_identifier: "[^"]*"' "$config_file"; then
        # Fallback: update any app_identifier in IOS section
        sed -i.bak '/IOS = {/,/^[[:space:]]*}/ s/app_identifier: "[^"]*"/app_identifier: "'"$PACKAGE"'"/' "$config_file"
        print_success "Updated iOS app_identifier to '$PACKAGE'"
    fi

    # Update iOS provisioning profile names (AdHoc)
    if grep -q '"match AdHoc com\.niyaj' "$config_file"; then
        sed -i.bak 's/"match AdHoc com\.niyaj\.[^"]*"/"match AdHoc '"$PACKAGE"'"/g' "$config_file"
        print_success "Updated iOS AdHoc provisioning profile"
    elif grep -q '"match AdHoc org\.mifos' "$config_file"; then
        sed -i.bak 's/"match AdHoc org\.mifos\.[^"]*"/"match AdHoc '"$PACKAGE"'"/g' "$config_file"
        print_success "Updated iOS AdHoc provisioning profile"
    fi

    # Update iOS provisioning profile names (AppStore)
    if grep -q '"match AppStore com\.niyaj' "$config_file"; then
        sed -i.bak 's/"match AppStore com\.niyaj\.[^"]*"/"match AppStore '"$PACKAGE"'"/g' "$config_file"
        print_success "Updated iOS AppStore provisioning profile"
    elif grep -q '"match AppStore org\.mifos' "$config_file"; then
        sed -i.bak 's/"match AppStore org\.mifos\.[^"]*"/"match AppStore '"$PACKAGE"'"/g' "$config_file"
        print_success "Updated iOS AppStore provisioning profile"
    fi

    print_success "Fastlane configuration updated successfully"
}

update_libs_versions_toml() {
    print_section "Updating libs.versions.toml"

    local toml_file="gradle/libs.versions.toml"

    if [ ! -f "$toml_file" ]; then
        print_warning "libs.versions.toml file not found at $toml_file"
        return 0
    fi

    print_processing "Updating package configurations in $toml_file"

    # Update desktopPackageName
    if grep -q "desktopPackageName = " "$toml_file"; then
        sed -i.bak "s/desktopPackageName = \"[^\"]*\"/desktopPackageName = \"${PROJECT_NAME_CAPITALIZED}Desktop\"/g" "$toml_file"
        print_success "Updated desktopPackageName to '${PROJECT_NAME_CAPITALIZED}Desktop'"
    fi

    # Update desktopPackageNamespace
    if grep -q "desktopPackageNamespace = " "$toml_file"; then
        sed -i.bak "s/desktopPackageNamespace = \"[^\"]*\"/desktopPackageNamespace = \"$PACKAGE\"/g" "$toml_file"
        print_success "Updated desktopPackageNamespace to '$PACKAGE'"
    fi

    # Update androidPackageNamespace
    if grep -q "androidPackageNamespace = " "$toml_file"; then
        sed -i.bak "s/androidPackageNamespace = \"[^\"]*\"/androidPackageNamespace = \"$PACKAGE\"/g" "$toml_file"
        print_success "Updated androidPackageNamespace to '$PACKAGE'"
    fi

    print_success "libs.versions.toml updated successfully"
}

update_google_services_json() {
    print_section "Updating Google Services Configuration"

    local google_services_file="cmp-android/google-services.json"

    if [ ! -f "$google_services_file" ]; then
        print_warning "google-services.json file not found at $google_services_file"
        return 0
    fi

    print_processing "Updating package names in $google_services_file"

    # Original package name to search for (escaped for sed)
    local original_package="cmp\\.android\\.app"

    # Check if the original package exists in the file
    if grep -q "cmp\.android\.app" "$google_services_file"; then
        # Update variants in order from most specific to least specific
        # This prevents partial matches from breaking the replacement

        # Update .demo.debug variant (most specific)
        if grep -q "\"cmp\.android\.app\.demo\.debug\"" "$google_services_file"; then
            sed -i.bak "s/\"${original_package}\\.demo\\.debug\"/\"$PACKAGE.demo.debug\"/g" "$google_services_file"
            print_success "Updated demo.debug variant: 'cmp.android.app.demo.debug' → '$PACKAGE.demo.debug'"
        fi

        # Update .demo variant
        if grep -q "\"cmp\.android\.app\.demo\"" "$google_services_file"; then
            sed -i.bak "s/\"${original_package}\\.demo\"/\"$PACKAGE.demo\"/g" "$google_services_file"
            print_success "Updated demo variant: 'cmp.android.app.demo' → '$PACKAGE.demo'"
        fi

        # Update .debug variant
        if grep -q "\"cmp\.android\.app\.debug\"" "$google_services_file"; then
            sed -i.bak "s/\"${original_package}\\.debug\"/\"$PACKAGE.debug\"/g" "$google_services_file"
            print_success "Updated debug variant: 'cmp.android.app.debug' → '$PACKAGE.debug'"
        fi

        # Update base package name (release variant) - must be last
        if grep -q "\"cmp\.android\.app\"" "$google_services_file"; then
            sed -i.bak "s/\"${original_package}\"/\"$PACKAGE\"/g" "$google_services_file"
            print_success "Updated release package: 'cmp.android.app' → '$PACKAGE'"
        fi

        print_success "Google Services configuration updated successfully"
        print_info "All 4 app variants configured in google-services.json"
    else
        print_warning "No 'cmp.android.app' package names found in google-services.json"
    fi

    print_info "Note: Only Release and Demo are registered in Firebase (Debug variants share credentials)"
    print_warning "Important: Remember to replace this file with your own Firebase configuration!"
}

# Function to process module directories
process_module_dirs() {
    local module_path=$1
    local src_dirs=("main" "commonMain" "commonTest" "androidMain" "androidTest" "iosMain" "nativeMain" "iosTest" "desktopMain" "desktopTest" "jvmMain" "jvmTest" "jsMain" "jsTest" "wasmJsMain" "wasmJsTest" "nonAndroidMain" "jsCommonMain" "nonJsCommonMain" "jvmCommonMain" "nonJvmCommonMain" "jvmJsCommonMain" "nonNativeMain" "mobileMain")

    for src_dir in "${src_dirs[@]}"
    do
        local kotlin_dir="$module_path/src/$src_dir/kotlin"
        if [ -d "$kotlin_dir" ]; then
            print_processing "Processing $kotlin_dir"

            mkdir -p "$kotlin_dir/$SUBDIR"

            if [ -d "$kotlin_dir/org/mifos" ]; then
                print_info "Moving files from org/mifos to $SUBDIR"
                cp -r "$kotlin_dir/org/mifos"/* "$kotlin_dir/$SUBDIR/" 2>/dev/null || true

                if [ -d "$kotlin_dir/$SUBDIR" ]; then
                    print_info "Updating package declarations and imports"
                    find "$kotlin_dir/$SUBDIR" -type f -name "*.kt" -exec sed -i.bak \
                        -e "s/package org\.mifos/package $PACKAGE/g" \
                        -e "s/package com\.niyaj/package $PACKAGE/g" \
                        -e "s/import org\.mifos/import $PACKAGE/g" \
                        -e "s/import com\.niyaj/import $PACKAGE/g" {} \;
                fi

                print_info "Cleaning up old directory structure"
                rm -rf "$kotlin_dir/org/mifos"
                rmdir "$kotlin_dir/org" 2>/dev/null || true
            fi
        fi
    done

    find "$module_path" -type f -name "*.kt" -exec sed -i.bak "s/import org\.mifos/import $PACKAGE/g" {} \;
    find "$module_path" -type f -name "*.kt" -exec sed -i.bak "s/package org\.mifos/package $PACKAGE/g" {} \;
}

process_module_content() {
    print_section "Processing Modules"
    local base_dirs=("core" "feature" "cmp-navigation" "cmp-android")

    print_processing "Processing module contents..."
    for base_dir in "${base_dirs[@]}"
    do
        if [ -d "$base_dir" ]; then
            print_info "Checking in $base_dir directory..."
            while IFS= read -r module; do
                if [ -n "$module" ]; then
                    print_info "Found module: $module"
                    process_module_dirs "$module"
                fi
            done < <(find "$base_dir" -type f -name "build.gradle.kts" -not -path "*/build/*" -exec dirname {} \;)
        else
            print_warning "Directory $base_dir not found"
        fi
    done
}

update_android_app_imports() {
    print_section "Updating Imports in cmp-android/src/main/kotlin/cmp/android/app"

    local target_dir="cmp-android/src/main/kotlin/cmp/android/app"

    if [ ! -d "$target_dir" ]; then
        print_warning "Directory not found: $target_dir"
        return 0
    fi

    local count=0
    while IFS= read -r file; do
        if grep -q "import org\.mifos" "$file"; then
            print_processing "Updating imports in: $file"
            sed -i.bak "s/import org\.mifos/import $PACKAGE/g" "$file"
            ((count++))
        fi
    done < <(find "$target_dir" -type f -name "*.kt")

    if [ "$count" -eq 0 ]; then
        print_warning "No org.mifos imports found in $target_dir"
    else
        print_success "Updated imports in $count file(s) under $target_dir"
    fi
}

# Function to rename files
rename_files() {
    print_section "Renaming Files"

    print_processing "Renaming files with Mifos prefix..."
    find . -type f -name "Mifos*.kt" | while read -r file; do
        local newfile=$(echo "$file" | sed "s/MifosApp/$PROJECT_NAME_CAPITALIZED/g; s/Mifos/$PROJECT_NAME_CAPITALIZED/g")
        print_info "Renaming $file to $newfile"
        mv "$file" "$newfile"
    done

    print_processing "Updating code elements with Mifos prefix..."
    find . -type f -name "*.kt" -exec sed -i.bak \
        -e "s/MifosApp\([^A-Za-z0-9]\|$\)/$PROJECT_NAME_CAPITALIZED\1/g" \
        -e "s/Mifos\([A-Z][a-zA-Z0-9]*\)/$PROJECT_NAME_CAPITALIZED\1/g" {} \;

    find . -type f -name "*.kt" -exec sed -i.bak \
        -e "s/mifosApp\([^A-Za-z0-9]\|$\)/${PROJECT_NAME_LOWERCASE}\1/g" \
        -e "s/mifos\([A-Z][a-zA-Z0-9]*\)/${PROJECT_NAME_LOWERCASE}\1/g" {} \;

    print_processing "Updating import statements..."
    find . -type f -name "*.kt" -exec sed -i.bak \
        -e "s/import.*\.MifosApp/import $PACKAGE.$PROJECT_NAME_CAPITALIZED/g" \
        -e "s/import.*\.Mifos/import $PACKAGE.$PROJECT_NAME_CAPITALIZED/g" {} \;
}

# Function to clean up backup files
cleanup_backup_files() {
    print_section "Cleanup"
    print_processing "Cleaning up backup files..."
    find . -name "*.bak" -type f -delete
    print_success "Backup files cleaned up successfully"
}

print_final_summary() {
    print_section "Summary of Changes"
    echo -e "${GREEN}${CHECK_MARK} Your Kotlin Multiplatform project has been customized with the following changes:${NC}"
    echo
    echo -e "${CYAN}1. Package Updates:${NC}"
    echo "   - Base package updated to: $PACKAGE"
    echo "   - Compose Resources package updated"
    echo "   - Android Manifest package updated"
    echo
    echo -e "${CYAN}2. Project Naming:${NC}"
    echo "   - Project name set to: $PROJECT_NAME"
    echo "   - Application name set to: $APPNAME"
    echo
    echo -e "${CYAN}3. Version Catalog (libs.versions.toml):${NC}"
    echo "   - Updated desktopPackageName to: ${PROJECT_NAME_CAPITALIZED}Desktop"
    echo "   - Updated desktopPackageNamespace to: $PACKAGE"
    echo "   - Updated androidPackageNamespace to: $PACKAGE"
    echo
    echo -e "${CYAN}4. Fastlane Configuration:${NC}"
    echo "   - Updated PROJECT_NAME to: $PROJECT_NAME"
    echo "   - Updated ORGANIZATION_NAME to: $PROJECT_NAME"
    echo "   - Updated Android package name"
    echo "   - Updated iOS bundle identifier"
    echo "   - Updated provisioning profile names"
    echo
    echo -e "${CYAN}5. Google Services (Firebase):${NC}"
    echo "   - Updated Android package names in google-services.json"
    echo "   - All 4 app variants configured:"
    echo "     • Release: $PACKAGE"
    echo "     • Debug: $PACKAGE.debug"
    echo "     • Demo: $PACKAGE.demo"
    echo "     • Demo Debug: $PACKAGE.demo.debug"
    echo "   - Note: Debug variants share Firebase credentials with release variants"
    echo
    echo -e "${CYAN}6. Module Updates:${NC}"
    echo "   - Renamed all mifos-prefixed modules"
    echo "   - Updated module references in Gradle files"
    echo "   - Updated module imports and packages"
    echo
    echo -e "${CYAN}7. Code Updates:${NC}"
    echo "   - Renamed Mifos-prefixed files to $PROJECT_NAME_CAPITALIZED"
    echo "   - Updated package declarations and imports"
    echo "   - Updated typesafe accessors"
    echo
    echo -e "${YELLOW}${WARNING} Important Next Steps:${NC}"
    echo "   1. Replace cmp-android/google-services.json with your own Firebase configuration"
    echo "   2. Review and update fastlane-config/project_config.rb with your:"
    echo "      - Firebase App IDs"
    echo "      - App Store Connect credentials"
    echo "      - Code signing configuration"
    echo "      - Keystore passwords (if different)"
    echo "   3. Place required secret files in the secrets/ directory"
    echo "   4. Update build output paths if your module names changed"
    echo "   5. Review gradle/libs.versions.toml for any additional customizations"
    echo
    echo -e "${GREEN}${ROCKET} Project customization completed successfully!${NC}"
}

print_welcome_banner() {
    echo -e "${BLUE}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║                                                              ║"
    echo "║           Kotlin Multiplatform Project Customizer            ║"
    echo "║                                                              ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# Main execution function
main() {
    print_welcome_banner

    print_section "Starting Customization"
    print_info "Package: $PACKAGE"
    print_info "Project Name: $PROJECT_NAME"
    print_info "Application Name: $APPNAME"

    # Core updates
    update_compose_resources
    update_package_namespace
    update_root_project_name
    update_fastlane_config
    update_libs_versions_toml
    update_google_services_json
    process_module_content
    update_android_app_imports
    rename_files
    cleanup_backup_files
    print_final_summary
}

# Execute main function
main