#!/usr/bin/env bash
# scripts/store-listing-preflight.sh
#
# Store-listing PREFLIGHT — CI mirror of `/release` STEP 1.7 (Store Listing Wizard) validation.
# Fails fast if the Play Store listing metadata is missing or exceeds store character limits,
# so the upload never gets rejected by the Play API with "missing required field: title".
#
# The deploy itself still UPLOADS the listing (deployInternal passes metadata_path); this gate
# only validates it is present + within limits BEFORE the (gated, billable) build runs.
#
# Usage: store-listing-preflight.sh [locale] [metadata_root]
#   locale         default: en-US
#   metadata_root  default: deployment/android/metadata
set -euo pipefail

LOCALE="${1:-en-US}"
META_ROOT="${2:-deployment/android/metadata}"
DIR="$META_ROOT/$LOCALE"

FAIL=0
err() { echo "❌ $*"; FAIL=1; }
ok()  { echo "✅ $*"; }
warn() { echo "⚠️  $*"; }

# Character count of a file's content (trailing newline stripped); 0 when missing.
clen() {
  if [ -f "$1" ]; then printf '%s' "$(cat "$1")" | wc -m | tr -d ' '; else echo 0; fi
}

# check <relpath> <max> <label> <severity:hard|soft>
check() {
  local file="$DIR/$1" max="$2" label="$3" sev="${4:-hard}" len
  if [ ! -f "$file" ]; then
    [ "$sev" = "hard" ] && err "$label — MISSING ($1)" || warn "$label — missing ($1); generated at deploy"
    return
  fi
  len=$(clen "$file")
  if [ "$len" -eq 0 ]; then
    [ "$sev" = "hard" ] && err "$label — EMPTY ($1)" || warn "$label — empty ($1); generated at deploy"
    return
  fi
  if [ "$len" -gt "$max" ]; then
    err "$label — $len chars > $max limit ($1)"
    return
  fi
  ok "$label — $len/$max chars"
}

echo "🔎 Store-listing preflight · $DIR"
echo ""

if [ ! -d "$DIR" ]; then
  err "No Play Store listing metadata at '$DIR'."
  echo "   The Play API rejects uploads without a title/description."
  echo "   Generate it with /release (STEP 1.7 — Store Listing Wizard)."
  exit 1
fi

# Play Store hard limits (required-at-upload fields).
check title.txt               30 "Title"             hard
check short_description.txt    80 "Short description" hard
check full_description.txt   4000 "Full description"  hard
# Release notes are (re)generated at deploy time by the lane, so soft.
check changelogs/default.txt  500 "Changelog (default.txt)" soft

echo ""
if [ "$FAIL" -ne 0 ]; then
  echo "❌ Store-listing preflight FAILED — fix the fields above before deploying."
  exit 1
fi
echo "✅ Store-listing preflight PASSED"
