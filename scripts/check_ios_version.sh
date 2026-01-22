#!/bin/bash

# ==============================================================================
# iOS Version Check Script
# ==============================================================================
# This script displays current iOS version configuration
# ==============================================================================

set -e

# Color codes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ $1${NC}"
}

print_section() {
    echo
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo
}

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

print_section "📱 iOS Version Configuration Check"

# Check version.txt (Gradle-generated)
if [ -f "version.txt" ]; then
    VERSION_TXT=$(cat version.txt)
    print_success "Gradle version.txt: $VERSION_TXT"
else
    echo "⚠️  version.txt not found. Run: ./gradlew versionFile"
fi
echo

# Check Xcode project settings
print_info "Xcode Project Settings:"
MARKETING_VERSION=$(xcodebuild -project cmp-ios/iosApp.xcodeproj -showBuildSettings 2>/dev/null | grep "MARKETING_VERSION" | awk '{print $3}' | head -1)
CURRENT_PROJECT_VERSION=$(xcodebuild -project cmp-ios/iosApp.xcodeproj -showBuildSettings 2>/dev/null | grep "CURRENT_PROJECT_VERSION" | awk '{print $3}' | head -1)

if [ -n "$MARKETING_VERSION" ]; then
    print_success "MARKETING_VERSION: $MARKETING_VERSION"
else
    echo "⚠️  MARKETING_VERSION not found"
fi

if [ -n "$CURRENT_PROJECT_VERSION" ]; then
    print_success "CURRENT_PROJECT_VERSION: $CURRENT_PROJECT_VERSION"
else
    echo "⚠️  CURRENT_PROJECT_VERSION not found"
fi
echo

# Check Info.plist
print_info "Info.plist Configuration:"
SHORT_VERSION=$(plutil -p cmp-ios/iosApp/Info.plist 2>/dev/null | grep CFBundleShortVersionString | awk -F'"' '{print $4}')
BUNDLE_VERSION=$(plutil -p cmp-ios/iosApp/Info.plist 2>/dev/null | grep "\"CFBundleVersion\"" | awk -F'"' '{print $4}')

echo "  CFBundleShortVersionString: $SHORT_VERSION"
echo "  CFBundleVersion: $BUNDLE_VERSION"

if [[ "$SHORT_VERSION" == "\$(MARKETING_VERSION)" ]]; then
    print_success "Using dynamic versioning (MARKETING_VERSION)"
else
    echo "  ⚠️  Warning: Info.plist has hardcoded version"
fi

if [[ "$BUNDLE_VERSION" == "\$(CURRENT_PROJECT_VERSION)" ]]; then
    print_success "Using dynamic build number (CURRENT_PROJECT_VERSION)"
else
    echo "  ⚠️  Warning: Info.plist has hardcoded build number"
fi
echo

# Summary
print_section "📊 Summary"
echo "When you deploy iOS:"
echo "  1. Fastlane runs: gradle(tasks: [\"versionFile\"])"
echo "  2. Reads version from: version.txt ($VERSION_TXT)"
echo "  3. Updates Xcode MARKETING_VERSION to: $VERSION_TXT"
echo "  4. Auto-increments CURRENT_PROJECT_VERSION from TestFlight/Firebase"
echo "  5. Info.plist uses: \$(MARKETING_VERSION) and \$(CURRENT_PROJECT_VERSION)"
echo

print_info "To update version for next release:"
echo "  1. Update version in Gradle (where project.version is defined)"
echo "  2. Run: ./gradlew versionFile"
echo "  3. Deploy: bash scripts/deploy_testflight.sh"
echo
