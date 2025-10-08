#!/bin/bash

################################################################################
# Documentation Generation Script
#
# This script generates Dokka documentation for the KMP project.
#
# Usage:
#   ./docs-generator/scripts/generate-docs.sh [options]
#
# Options:
#   --clean        Clean previous documentation before generating
#   --no-color     Disable colored output
#   --help         Show this help message
#
# Environment Variables:
#   OUTPUT_DIR     Directory for generated documentation (default: build/docs-output)
#
# Examples:
#   ./docs-generator/scripts/generate-docs.sh
#   ./docs-generator/scripts/generate-docs.sh --clean
#   OUTPUT_DIR=/tmp/docs ./docs-generator/scripts/generate-docs.sh
#
################################################################################

set -e  # Exit on error

# Colors for output
# Check if colors are supported and not disabled
if [ "$NO_COLOR" = false ] && [ -t 1 ] && command -v tput >/dev/null 2>&1 && [ "$(tput colors 2>/dev/null || echo 0)" -ge 8 ]; then
    # Terminal supports colors
    RED='\033[0;31m'
    GREEN='\033[0;32m'
    YELLOW='\033[1;33m'
    BLUE='\033[0;34m'
    NC='\033[0m' # No Color
else
    # No color support or disabled
    RED=''
    GREEN=''
    YELLOW=''
    BLUE=''
    NC=''
fi

# Default options
CLEAN=false
NO_COLOR=false
OUTPUT_DIR="${OUTPUT_DIR:-build/docs-output}"

# Script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

################################################################################
# Functions
################################################################################

print_header() {
    echo -e "${BLUE}================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

show_help() {
    sed -n '/^################################################################################/,/^################################################################################/p' "$0" | 
    grep -E '^#' | 
    sed 's/^# \?//'
    exit 0
}

check_requirements() {
    print_header "Checking Requirements"
    
    # Check if we're in the project root
    if [ ! -f "$PROJECT_ROOT/settings.gradle.kts" ]; then
        print_error "Not in a Gradle project root directory"
        exit 1
    fi
    print_success "Found Gradle project"
    
    # Check for gradlew
    if [ ! -f "$PROJECT_ROOT/gradlew" ]; then
        print_error "gradlew not found in project root"
        exit 1
    fi
    print_success "Found gradlew"
    
    # Make gradlew executable
    chmod +x "$PROJECT_ROOT/gradlew"
    
    echo ""
}

clean_previous_docs() {
    if [ "$CLEAN" = true ]; then
        print_header "Cleaning Previous Documentation"
        
        rm -rf "$PROJECT_ROOT/$OUTPUT_DIR"
        rm -rf "$PROJECT_ROOT/build/dokka"
        
        print_success "Cleaned previous documentation"
        echo ""
    fi
}

generate_dokka() {
    print_header "Generating Dokka Documentation"
    
    cd "$PROJECT_ROOT"
    
    if ./gradlew dokkaGenerate -x :cmp-web:dokkaGenerate --no-configuration-cache --no-daemon --stacktrace; then
        print_success "Dokka documentation generated successfully"
    else
        print_error "Failed to generate Dokka documentation"
        exit 1
    fi
    
    echo ""
}

organize_documentation() {
    print_header "Organizing Documentation"
    
    mkdir -p "$PROJECT_ROOT/$OUTPUT_DIR"
    
    # Copy Dokka documentation
    if [ -d "$PROJECT_ROOT/build/dokka" ]; then
        print_info "Copying Dokka documentation..."
        cp -r "$PROJECT_ROOT/build/dokka/"* "$PROJECT_ROOT/$OUTPUT_DIR/"
        print_success "Dokka documentation copied"
    fi
    
    # Create .nojekyll for GitHub Pages
    touch "$PROJECT_ROOT/$OUTPUT_DIR/.nojekyll"
    
    echo ""
}

print_summary() {
    print_header "Documentation Generation Complete"
    
    echo -e "${GREEN}Documentation generated successfully!${NC}"
    echo ""
    echo "Output directory: ${BLUE}$OUTPUT_DIR${NC}"
    echo ""
    echo "To view the documentation:"
    echo "  1. Open ${BLUE}$OUTPUT_DIR/index.html${NC} in your browser"
    echo "  2. Or run a local server:"
    echo "     ${YELLOW}cd $OUTPUT_DIR && python3 -m http.server 8000${NC}"
    echo "     Then open: ${BLUE}http://localhost:8000/index.html${NC}"
    echo ""
    
    # Calculate total size
    if command -v du &> /dev/null; then
        TOTAL_SIZE=$(du -sh "$PROJECT_ROOT/$OUTPUT_DIR" 2>/dev/null | cut -f1 || echo "unknown")
        echo "Total documentation size: ${BLUE}$TOTAL_SIZE${NC}"
    fi
    
    echo ""
}

################################################################################
# Parse arguments
################################################################################

while [[ $# -gt 0 ]]; do
    case $1 in
        --clean)
            CLEAN=true
            shift
            ;;
        --help|-h)
            show_help
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help to see available options"
            exit 1
            ;;
    esac
done

################################################################################
# Main execution
################################################################################

print_header "KMP Project Documentation Generator"
echo ""

check_requirements
clean_previous_docs
generate_dokka
organize_documentation
print_summary

exit 0
