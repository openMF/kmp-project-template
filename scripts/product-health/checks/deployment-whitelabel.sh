#!/usr/bin/env bash
# checks/deployment-whitelabel.sh — the white-label boundary between the fork-owned deployment SoT
# (app-profile/) and the template-owned deployment LOGIC (deployment/**) must hold.
#
# app-profile/ carries ALL fork-owned deployment data (identity, org, store copy, distribution ids,
# media). deployment/** is template logic that READS it via _shared/config.rb (AppProfile.get). This
# check asserts the seam: (B1) the SoT exists + is authored for THIS fork, (B2) no template org
# identity leaked as a LITERAL into template-owned deployment logic (must be tokenized), (B3) any
# unset distribution placeholder is surfaced, (B4) the per-platform media dirs are present, (B5) every
# app.yaml#targets platform intent maps to a DEPLOYMENT_MANIFEST platform (no orphan intent), (B6) no
# duplicate/competing Android metadata root shadows the canonical deployment/android/metadata tree,
# (B7) the MANAGED store schema (structured keys — app.yaml#store.*, platforms/<p>/*.yaml, incl.
# trade_representative) is authored + placeholder-free, (B8) a fork must not carry the template's own
# default store copy in that schema, (B9) deployment/**/metadata (DERIVED by syncForkConfig from the
# keys) must not have drifted from the app-profile key SoT.
#
# exit 0 PASS · 1 FAIL (B1/B2/B7/B8 block) · 2 WARN (B3/B4/B5/B6/B9 need attention, non-blocking).
# Self-skips (PASS) on the upstream template (TEMPLATE_SELF_BUILD=1) — same as fork-identity.sh.
set -uo pipefail
# shellcheck source=scripts/product-health/lib.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"

if [ "${TEMPLATE_SELF_BUILD:-}" = "1" ]; then
  echo "TEMPLATE_SELF_BUILD=1 — this IS the upstream template; white-label boundary N/A"; exit 0
fi
: "${HEALTH_ROOT:?deployment-whitelabel: HEALTH_ROOT not set (run via product-health.sh)}"

fail=0; warn=0
APP_YAML="$HEALTH_ROOT/app-profile/app.yaml"

# ── B1 — the fork-owned SoT exists, parses, and carries THIS fork's app id ────
if [ ! -f "$APP_YAML" ]; then
  echo "❌ B1: app-profile/app.yaml is missing — the fork-owned white-label deployment SoT must exist."
  fail=1
else
  app_id="$(ruby -ryaml -e 'begin; y=YAML.load_file(ARGV[0]); print((y.dig("identity","app_id")||"").to_s); rescue => e; STDERR.puts e.message; exit 9; end' "$APP_YAML" 2>/dev/null)"
  if [ $? -ne 0 ]; then
    echo "❌ B1: app-profile/app.yaml does not parse as YAML."
    fail=1
  elif [ -z "$app_id" ]; then
    echo "❌ B1: app-profile/app.yaml identity.app_id is empty — set your fork's app id."
    fail=1
  elif [ "$app_id" = "org.mifos.kmp.template" ]; then
    echo "❌ B1: identity.app_id is still the TEMPLATE default 'org.mifos.kmp.template' — set your fork's app id."
    fail=1
  elif printf '%s' "$app_id" | grep -qE '^YOUR_|^$'; then
    echo "❌ B1: identity.app_id '$app_id' is an unfilled placeholder — set your fork's app id."
    fail=1
  fi
fi

# ── B2 — no template org identity as a LITERAL in template-owned deployment logic ─
# The value must be TOKENIZED (read from app-profile via config.rb), never hardcoded.
# Comments and metadata .txt / README are excluded.
PATTERNS='Mifos Initiative|org\.mifos\.kmp\.template|mifos-x-web|MifosInitiative\.MoneyToolkit'
b2_hits=()
if [ -d "$HEALTH_ROOT/deployment" ]; then
  while IFS= read -r f; do
    case "$f" in
      *.appxmanifest) stripped="$(sed 's/<!--.*-->//g' "$f")" ;;   # strip XML comments
      *)              stripped="$(sed 's/#.*$//' "$f")" ;;          # strip rb/yaml/yml/toml line comments
    esac
    if printf '%s\n' "$stripped" | grep -qE "$PATTERNS"; then
      b2_hits+=("${f#$HEALTH_ROOT/}")
    fi
  done < <(find "$HEALTH_ROOT/deployment" -type f \
             \( -name 'config.yaml' -o -name 'lane.rb' -o -name 'workflow-snippet.yml' \
                -o -name '*.appxmanifest' -o -name 'wrangler.toml' \) 2>/dev/null)
fi
if [ "${#b2_hits[@]}" -ne 0 ]; then
  echo "❌ B2: template org identity found as a LITERAL in template-owned deployment logic —"
  echo "        these must be TOKENIZED (read from app-profile/ via config.rb), not hardcoded:"
  for h in "${b2_hits[@]}"; do echo "        · $h"; done
  fail=1
fi

# ── B3 — unset distribution placeholders in app-profile (WARN, non-blocking) ──
b3=()
if [ -f "$APP_YAML" ] || [ -d "$HEALTH_ROOT/app-profile" ]; then
  # Placeholder firebase app ids (all-zero project number) + unfilled windows YOUR_* values.
  if grep -rqE '1:0{12}:' "$HEALTH_ROOT/app-profile" 2>/dev/null; then b3+=("firebase.* app id is the 1:000000000000:* placeholder"); fi
  if grep -rqE '\bYOUR_[A-Z_]+' "$HEALTH_ROOT/app-profile" 2>/dev/null; then b3+=("windows.* carries YOUR_* placeholder(s)"); fi
fi
if [ "${#b3[@]}" -ne 0 ]; then
  echo "⚠️  B3: distribution placeholders still unset in app-profile/ (fill before releasing to that store):"
  for w in "${b3[@]}"; do echo "        · $w"; done
  warn=1
fi

# ── B4 — per-platform media dirs present (WARN, non-blocking) ─────────────────
missing_media=()
for p in android apple/ios apple/macos web windows ubuntu; do
  [ -d "$HEALTH_ROOT/app-profile/platforms/$p/media" ] || missing_media+=("app-profile/platforms/$p/media")
done
if [ "${#missing_media[@]}" -ne 0 ]; then
  echo "⚠️  B4: expected per-platform media dir(s) missing:"
  for m in "${missing_media[@]}"; do echo "        · $m"; done
  warn=1
fi

# ── B5 — app.yaml#targets platform intent maps to a DEPLOYMENT_MANIFEST platform (WARN) ─
# Every platform a fork toggles in app-profile/app.yaml#targets must correspond to a platform
# block in deployment/DEPLOYMENT_MANIFEST.yaml — no orphan platform intent. macOS, Windows, and
# Ubuntu deploy targets all live UNDER the manifest's single `desktop` platform block (no separate
# windows/linux manifest platform exists), so `macos`/`windows`/`ubuntu` intent all map to `desktop`.
MANIFEST="$HEALTH_ROOT/deployment/DEPLOYMENT_MANIFEST.yaml"
b5=()
if [ -f "$APP_YAML" ] && [ -f "$MANIFEST" ]; then
  target_platforms="$(ruby -ryaml -e 'begin; y=YAML.load_file(ARGV[0]); print((y["targets"]||{}).keys.join(" ")); rescue; end' "$APP_YAML" 2>/dev/null)"
  manifest_platforms="$(ruby -ryaml -e 'begin; y=YAML.load_file(ARGV[0]); print((y["platforms"]||{}).keys.join(" ")); rescue; end' "$MANIFEST" 2>/dev/null)"
  for pk in $target_platforms; do
    look="$pk"
    case "$pk" in macos|windows|ubuntu) look="desktop" ;; esac
    printf ' %s ' "$manifest_platforms" | grep -q " $look " || b5+=("app.yaml#targets.$pk has no matching platform in DEPLOYMENT_MANIFEST.yaml")
  done
fi
if [ "${#b5[@]}" -ne 0 ]; then
  echo "⚠️  B5: orphan platform intent in app-profile/app.yaml#targets (no manifest platform block):"
  for w in "${b5[@]}"; do echo "        · $w"; done
  warn=1
fi

# ── B6 — no duplicate/competing Android metadata root (WARN) ──────────────────
# The canonical Play metadata root is deployment/android/metadata (the lanes' metadata_path). A
# second fastlane-default root at deployment/fastlane/metadata/android competes with it and drifts.
canon_imgs="$HEALTH_ROOT/deployment/android/metadata/en-US/images"
dup_root="$HEALTH_ROOT/deployment/fastlane/metadata/android/en-US"
if [ -d "$canon_imgs" ] && [ -n "$(ls -A "$canon_imgs" 2>/dev/null)" ] \
   && [ -d "$dup_root" ] && [ -n "$(find "$dup_root" -type f 2>/dev/null | head -1)" ]; then
  echo "⚠️  B6: duplicate Android metadata root — both trees are populated:"
  echo "        · deployment/android/metadata/en-US/images (canonical — the lanes' metadata_path)"
  echo "        · deployment/fastlane/metadata/android/en-US (fastlane-default duplicate — remove)"
  warn=1
fi

# ── B7 — the managed store schema is authored + placeholder-free (blocking) ──
# Store copy is MANAGED STRUCTURED KEYS (app.yaml#store.*, platforms/<p>/*.yaml — NOT a file dump).
# syncForkConfig binds those keys → deployment/**/metadata. Verify the schema is filled, not blank/placeholder.
APP_YAML="$HEALTH_ROOT/app-profile/app.yaml"
APPLE_YAML="$HEALTH_ROOT/app-profile/platforms/apple/apple.yaml"
STORE_YAMLS="$APP_YAML $APPLE_YAML $HEALTH_ROOT/app-profile/platforms/android/android.yaml"
if ! grep -qE '^[[:space:]]*title:[[:space:]]*\S' "$APP_YAML" 2>/dev/null; then
  echo "❌ B7: app-profile/app.yaml#store.title is missing or empty (store schema not authored)."; fail=1
fi
# App Store trade-representative contact must be filled when the block is present (Apple review requires it).
if [ -f "$APPLE_YAML" ] && grep -q 'trade_representative:' "$APPLE_YAML" 2>/dev/null; then
  if ! grep -qE '^[[:space:]]*email_address:[[:space:]]*\S' "$APPLE_YAML"; then
    echo "❌ B7: apple.yaml#trade_representative.email_address is empty (App Store review contact incomplete)."; fail=1
  fi
fi
# shellcheck disable=SC2086
if grep -rIlE 'TODO|FIXME|PLACEHOLDER|lorem ipsum' $STORE_YAMLS >/dev/null 2>&1; then
  echo "❌ B7: placeholder literal (TODO/FIXME/PLACEHOLDER/lorem) in the app-profile store schema — author your copy."; fail=1
fi

# ── B8 — a fork must not ship the template's default store copy (blocking; template self-skips at top) ──
# shellcheck disable=SC2086
if grep -rIlE 'Money Toolkit|Mifos Initiative|org\.mifos' $STORE_YAMLS >/dev/null 2>&1; then
  echo "❌ B8: template default store copy ('Money Toolkit' / 'Mifos Initiative' / 'org.mifos') in the app-profile store schema — author this fork's own listing."; fail=1
fi

# ── B9 — deployment/ metadata drifted from the app-profile key SoT (WARN, non-blocking) ──
# deployment/**/metadata is DERIVED from the keys by syncForkConfig; a mismatch means it wasn't re-derived.
dp_name="$HEALTH_ROOT/deployment/ios/appstore/metadata/en-US/name.txt"
sot_title="$(grep -E '^[[:space:]]*title:[[:space:]]*' "$APP_YAML" 2>/dev/null | head -1 | sed -E 's/^[[:space:]]*title:[[:space:]]*//; s/[[:space:]]*#.*$//; s/^["'"'"']//; s/["'"'"']$//')"
if [ -f "$dp_name" ] && [ -n "$sot_title" ] && [ "$(cat "$dp_name")" != "$sot_title" ]; then
  echo "⚠️  B9: deployment/ios/appstore/metadata/en-US/name.txt drifted from app-profile store.title ('$sot_title') — run ./gradlew syncForkConfig."; warn=1
fi

if [ "$fail" -ne 0 ]; then
  echo "→ Fix: author app-profile/app.yaml for your fork + tokenize any hardcoded identity in deployment/**."
  exit 1
fi
if [ "$warn" -ne 0 ]; then
  echo "   (warnings don't block — resolve before releasing.)"
  exit 2
fi
echo "white-label boundary holds: app-profile/ is the authored fork SoT; deployment/** carries no identity literals"
exit 0
