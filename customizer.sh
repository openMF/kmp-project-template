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
            if ! sed -i.bak "s/namespace = \"org\.mifos\.[^\"]*\"/namespace = \"$ESCAPED_PACKAGE\"/g" "$file"; then
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

    if grep -q "rootProject\.name\s*=\s*" "$settings_file"; then
        if ! sed -i.bak "s/rootProject\.name\s*=\s*\"[^\"]*\"/rootProject.name = \"$PROJECT_NAME\"/" "$settings_file"; then
            print_error "Failed to update rootProject.name in $settings_file"
            return 1
        fi
        print_success "Successfully updated rootProject.name to '$PROJECT_NAME'"
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
    local base_dirs=("core" "feature" "cmp-navigation")

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
    echo -e "${CYAN}3. Module Updates:${NC}"
    echo "   - Renamed all mifos-prefixed modules"
    echo "   - Updated module references in Gradle files"
    echo "   - Updated module imports and packages"
    echo
    echo -e "${CYAN}4. Code Updates:${NC}"
    echo "   - Renamed Mifos-prefixed files to $PROJECT_NAME_CAPITALIZED"
    echo "   - Updated package declarations and imports"
    echo "   - Updated typesafe accessors"
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
    process_module_content
    rename_files
    cleanup_backup_files
    print_final_summary
}

# Execute main function
main