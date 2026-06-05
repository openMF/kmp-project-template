#!/usr/bin/env bash
# deployment/desktop/linux-deb/script.sh
# Tier-1 unsigned Linux DEB → GitHub Releases.
set -euo pipefail

FLAVOR="${FLAVOR:-prod}"
TAG="${TAG:?GitHub release tag must be set (e.g. v2026.06.04)}"

# 1. Build the DEB via Compose Desktop.
./gradlew :cmp-desktop:packageReleaseDeb -PflavorDimension="$FLAVOR"

# 2. Locate the artifact.
DEB_PATH="$(find cmp-desktop/build/compose/binaries/main-release/deb -name '*.deb' -type f | head -1)"
[[ -f "$DEB_PATH" ]] || { echo "DEB not found at expected path"; exit 1; }

# 3. Upload to the GH Release (uses GH_TOKEN from env / vault).
gh release upload "$TAG" "$DEB_PATH" --clobber
