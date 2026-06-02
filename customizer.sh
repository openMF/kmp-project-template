#!/bin/bash
#
# Kotlin Multiplatform Project Customizer
#
# Usage:
#   bash customizer.sh <package_id> <project_name> [app_display_name] [ios_team_id]
#
# Example:
#   bash customizer.sh com.mybank.app MyBankApp "My Bank" ABCDE12345
#
# What this does:
#   1. Writes fork identity into gradle/libs.versions.toml (the single source of truth)
#   2. Runs ./gradlew syncForkConfig to propagate to iOS Config.xcconfig,
#      local.properties (Fastlane), and gradle.properties (rootProject.name)
#
# That's it. No source file scanning, no package renaming, no sync-dirs conflicts.
# The convention plugin derives all module namespaces from baseNamespace automatically.
#

set -e

# ── Colors ──────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m'

print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_info()    { echo -e "${BLUE}⚙️  $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_error()   { echo -e "${RED}✘ $1${NC}"; exit 1; }

# ── Verify bash version ──────────────────────────────────────────────────────
if [[ ${BASH_VERSINFO[0]} -lt 4 ]]; then
  print_error "Bash 4+ required. macOS ships with Bash 3 — run: brew install bash"
fi

# ── Args ─────────────────────────────────────────────────────────────────────
if [[ $# -lt 2 ]]; then
  echo -e "${BOLD}Usage:${NC} bash customizer.sh <package_id> <project_name> [app_display_name] [ios_team_id]"
  echo
  echo -e "${BOLD}Examples:${NC}"
  echo "  bash customizer.sh com.mybank.app MyBankApp"
  echo "  bash customizer.sh com.mybank.app MyBankApp \"My Bank\" ABCDE12345"
  exit 2
fi

PACKAGE=$1
PROJECT_NAME=$2
APPNAME=${3:-$PROJECT_NAME}
TEAM_ID=${4:-"XXXXXXXXXX"}

# Derive baseNamespace: com.mybank.app → com.mybank
BASE_NS=$(echo "$PACKAGE" | rev | cut -d. -f2- | rev)

LIBS_TOML="gradle/libs.versions.toml"

if [[ ! -f "$LIBS_TOML" ]]; then
  print_error "$LIBS_TOML not found. Run this script from the project root."
fi

echo
echo -e "${BLUE}╔══════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║        Kotlin Multiplatform Project Customizer       ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════╝${NC}"
echo
print_info "Package ID:       $PACKAGE"
print_info "Base namespace:   $BASE_NS"
print_info "Project name:     $PROJECT_NAME"
print_info "Display name:     $APPNAME"
print_info "iOS Team ID:      $TEAM_ID"
echo

# ── Update libs.versions.toml ────────────────────────────────────────────────
print_info "Updating $LIBS_TOML..."

sed -i.bak "s|appId[[:space:]]*=.*|appId            = \"$PACKAGE\"|"             "$LIBS_TOML"
sed -i.bak "s|appDisplayName[[:space:]]*=.*|appDisplayName   = \"$APPNAME\"|"    "$LIBS_TOML"
sed -i.bak "s|baseNamespace[[:space:]]*=.*|baseNamespace    = \"$BASE_NS\"|"     "$LIBS_TOML"
sed -i.bak "s|projectName[[:space:]]*=.*|projectName      = \"$PROJECT_NAME\"|"  "$LIBS_TOML"
sed -i.bak "s|iosTeamId[[:space:]]*=.*|iosTeamId        = \"$TEAM_ID\"|"         "$LIBS_TOML"
find . -name "*.bak" -not -path "*/build/*" -delete

print_success "libs.versions.toml updated"

# ── Regenerate all platform config files ─────────────────────────────────────
print_info "Running ./gradlew syncForkConfig..."
if ./gradlew syncForkConfig --quiet; then
  print_success "iOS Config.xcconfig regenerated"
  print_success "local.properties updated (Fastlane)"
  print_success "gradle.properties updated (rootProject.name)"
else
  print_warning "syncForkConfig failed — you may need to run it manually after Gradle syncs."
fi

echo
echo -e "${GREEN}${BOLD}✨ Customization complete!${NC}"
echo
echo -e "${YELLOW}Next steps:${NC}"
echo "  1. Replace cmp-android/google-services.json with your Firebase config"
echo "  2. Update fastlane-config/project_config.rb Firebase App IDs"
echo "  3. Replace cmp-ios/iosApp/Assets.xcassets with your app icon"
echo "  4. Run ./gradlew build to verify everything compiles"
echo
echo -e "  To update identity later: edit ${BOLD}gradle/libs.versions.toml${NC} → run ${BOLD}./gradlew syncForkConfig${NC}"
