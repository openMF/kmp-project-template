#!/usr/bin/env bash
# deployment/desktop/dmg-notarized/script.sh
# Tier-2 Apple-notarized macOS DMG → GitHub Releases.
# Requires: Developer ID Application cert + ASC API key (.p8) + key-id + issuer.
set -euo pipefail

FLAVOR="${FLAVOR:-prod}"
TAG="${TAG:?GitHub release tag must be set}"

DEVELOPER_ID_NAME="${DEVELOPER_ID_NAME:?DEVELOPER_ID_NAME env var required (e.g. 'Developer ID Application: Mifos Initiative (TEAMID)')}"
ASC_API_KEY_PATH="${ASC_API_KEY_PATH:?ASC_API_KEY_PATH env var required (path to AuthKey.p8)}"
ASC_KEY_ID="${ASC_KEY_ID:?ASC_KEY_ID env var required}"
ASC_ISSUER_ID="${ASC_ISSUER_ID:?ASC_ISSUER_ID env var required}"

# 1. Build .dmg + .app via Compose Desktop.
./gradlew :cmp-desktop:packageReleaseDmg -PflavorDimension="$FLAVOR"

APP_PATH="$(find cmp-desktop/build/compose/binaries/main-release/app -name '*.app' -type d | head -1)"
DMG_PATH="$(find cmp-desktop/build/compose/binaries/main-release/dmg -name '*.dmg' -type f | head -1)"
[[ -d "$APP_PATH" && -f "$DMG_PATH" ]] || { echo "Build outputs missing"; exit 1; }

# 2. Codesign the .app with hardened runtime + entitlements.
codesign --force --deep --options runtime \
  --entitlements "$(dirname "$0")/Entitlements.plist" \
  --sign "$DEVELOPER_ID_NAME" \
  "$APP_PATH"

# 3. Submit the DMG for notarization (blocking wait).
xcrun notarytool submit "$DMG_PATH" \
  --key "$ASC_API_KEY_PATH" \
  --key-id "$ASC_KEY_ID" \
  --issuer "$ASC_ISSUER_ID" \
  --wait

# 4. Staple the ticket so the DMG verifies offline.
xcrun stapler staple "$DMG_PATH"
xcrun stapler validate "$DMG_PATH"

# 5. Upload to GH Release.
gh release upload "$TAG" "$DMG_PATH" --clobber
