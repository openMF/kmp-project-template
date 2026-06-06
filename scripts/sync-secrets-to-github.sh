#!/usr/bin/env bash
# scripts/sync-secrets-to-github.sh
#
# Reads credentials from secrets/ and pushes them to GitHub Actions secrets.
# Run this once after filling secrets/ to wire up your fork's CI pipeline.
#
# Prerequisites:
#   - gh CLI installed and authenticated (gh auth login)
#   - secrets/ folder populated (copy from secrets_demo/, fill in real values)
#
# Usage:
#   bash scripts/sync-secrets-to-github.sh
#   bash scripts/sync-secrets-to-github.sh --repo owner/repo
#   bash scripts/sync-secrets-to-github.sh --dry-run
#   bash scripts/sync-secrets-to-github.sh --only ios
#   bash scripts/sync-secrets-to-github.sh --only android
#   bash scripts/sync-secrets-to-github.sh --only firebase
#   bash scripts/sync-secrets-to-github.sh --only web

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SECRETS_DIR="$REPO_ROOT/secrets"

DRY_RUN=false
REPO=""
ONLY=""

# ── Parse args ────────────────────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN=true ;;
    --repo)    REPO="$2"; shift ;;
    --only)    ONLY="$2"; shift ;;
    *) echo "Unknown arg: $1"; exit 1 ;;
  esac
  shift
done

# ── Validate environment ──────────────────────────────────────────────────────
if ! command -v gh &>/dev/null; then
  echo "❌  gh CLI not found. Install: https://cli.github.com"
  exit 1
fi

if [[ -z "$REPO" ]]; then
  REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)
fi

if [[ -z "$REPO" ]]; then
  echo "❌  Could not detect repo. Pass --repo owner/repo"
  exit 1
fi

if [[ ! -d "$SECRETS_DIR" ]]; then
  echo "❌  secrets/ not found at $SECRETS_DIR"
  echo "    Copy secrets_demo/ → secrets/ and fill in real values first."
  exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Sync secrets → GitHub Actions"
echo "  Repo:    $REPO"
echo "  Dry run: $DRY_RUN"
if [[ -n "$ONLY" ]]; then
  echo "  Filter:  --only $ONLY"
fi
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

PUSHED=0
SKIPPED=0
ERRORS=0

# ── Helper: set one GHA secret ────────────────────────────────────────────────
_set_secret() {
  local name="$1"
  local value="$2"
  local source_hint="${3:-}"

  if [[ -z "$value" ]]; then
    echo "  ⚠️  SKIP $name — empty value${source_hint:+ ($source_hint)}"
    ((SKIPPED++)) || true
    return
  fi

  if $DRY_RUN; then
    echo "  [dry-run] Would set: $name${source_hint:+ ← $source_hint}"
    ((PUSHED++)) || true
  else
    if echo "$value" | gh secret set "$name" --repo "$REPO" 2>&1; then
      echo "  ✅  $name${source_hint:+ ← $source_hint}"
      ((PUSHED++)) || true
    else
      echo "  ❌  FAILED: $name"
      ((ERRORS++)) || true
    fi
  fi
}

# Set secret from a text file (strips whitespace)
_set_from_file() {
  local name="$1"
  local path="$2"
  if [[ -f "$path" ]]; then
    local value; value=$(cat "$path" | tr -d '\r' | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
    # Skip placeholder files
    if echo "$value" | grep -q "CLAUDE-PLACEHOLDER"; then
      echo "  ⚠️  SKIP $name — still a placeholder in $path"
      ((SKIPPED++)) || true
      return
    fi
    _set_secret "$name" "$value" "$path"
  else
    echo "  ⏭   SKIP $name — file not found: $path"
    ((SKIPPED++)) || true
  fi
}

# Set secret from a binary file (base64-encoded)
_set_from_binary() {
  local name="$1"
  local path="$2"
  if [[ -f "$path" ]]; then
    local value; value=$(base64 -i "$path" 2>/dev/null || base64 "$path")
    # Skip placeholder files
    if [[ $(head -c 16 "$path") == "CLAUDE-PLHLD-v1" ]]; then
      echo "  ⚠️  SKIP $name — still a placeholder in $path"
      ((SKIPPED++)) || true
      return
    fi
    _set_secret "$name" "$value" "$path (base64)"
  else
    echo "  ⏭   SKIP $name — file not found: $path"
    ((SKIPPED++)) || true
  fi
}

# ── iOS secrets ───────────────────────────────────────────────────────────────
_sync_ios() {
  echo "📱 iOS / App Store"
  _set_from_file    "APPSTORE_KEY_ID"    "$SECRETS_DIR/appstore/key_id"
  _set_from_file    "APPSTORE_ISSUER_ID" "$SECRETS_DIR/appstore/issuer_id"
  _set_from_binary  "APPSTORE_AUTH_KEY"  "$SECRETS_DIR/appstore/AuthKey.p8"
  _set_from_binary  "MATCH_GIT_PRIVATE_KEY" "$SECRETS_DIR/match/match_ci_key"
  _set_from_file    "MATCH_PASSWORD"     "$SECRETS_DIR/match/.match_password"

  # shared_keys.env — parse and set individual vars
  local env_file="$SECRETS_DIR/shared_keys.env"
  if [[ -f "$env_file" ]] && ! grep -q "CLAUDE-PLACEHOLDER" "$env_file"; then
    echo "  Parsing $env_file …"
    while IFS='=' read -r key val; do
      [[ -z "$key" || "$key" =~ ^# ]] && continue
      key="${key#export }"
      val="${val%\"}"
      val="${val#\"}"
      [[ -z "$val" || "$val" == "YOUR_"* || "$val" == "your_"* ]] && continue
      _set_secret "IOS_ENV_${key}" "$val" "shared_keys.env:${key}"
    done < "$env_file"
  fi
  echo ""
}

# ── Android secrets ───────────────────────────────────────────────────────────
_sync_android() {
  echo "🤖 Android / Play Store"
  if [[ -f "$SECRETS_DIR/keystores/release.properties" ]]; then
    local props="$SECRETS_DIR/keystores/release.properties"
    while IFS='=' read -r k v; do
      [[ -z "$k" || "$k" =~ ^# ]] && continue
      case "$k" in
        storePassword) _set_secret "KEYSTORE_PASSWORD"     "$v" "$props" ;;
        keyAlias)      _set_secret "KEY_ALIAS"             "$v" "$props" ;;
        keyPassword)   _set_secret "KEY_PASSWORD"          "$v" "$props" ;;
      esac
    done < "$props"
  fi
  local jks_name
  jks_name=$(grep '^storeFile=' "$SECRETS_DIR/keystores/release.properties" 2>/dev/null | cut -d'=' -f2-)
  _set_from_binary "KMP_TEMPLATE_RELEASE_KEYSTORE" "$SECRETS_DIR/keystores/${jks_name:-release.jks}"
  echo ""
}

# ── Firebase secrets ──────────────────────────────────────────────────────────
_sync_firebase() {
  echo "🔥 Firebase"
  _set_from_binary "FIREBASECREDS"          "$SECRETS_DIR/firebase/service-account.json"
  _set_from_file   "FIREBASE_ANDROID_APP_ID"     "$SECRETS_DIR/firebase/android_app_id"
  _set_from_file   "FIREBASE_ANDROID_DEMO_APP_ID" "$SECRETS_DIR/firebase/android_demo_app_id"
  _set_from_file   "FIREBASE_IOS_APP_ID"     "$SECRETS_DIR/firebase/ios_app_id"
  echo ""
}

# ── Play Store secrets ────────────────────────────────────────────────────────
_sync_play() {
  echo "▶  Play Store"
  _set_from_binary "PLAYSTORECREDS" "$SECRETS_DIR/play/service-account.json"
  echo ""
}

# ── Web hosting secrets ───────────────────────────────────────────────────────
_sync_web() {
  echo "🌐 Web / Hosting"
  _set_from_file "CLOUDFLARE_API_TOKEN"  "$SECRETS_DIR/cloudflare/api_token"
  _set_from_file "CLOUDFLARE_ACCOUNT_ID" "$SECRETS_DIR/cloudflare/account_id"
  _set_from_file "NETLIFY_AUTH_TOKEN"    "$SECRETS_DIR/netlify/auth_token"
  _set_from_file "NETLIFY_SITE_ID"       "$SECRETS_DIR/netlify/site_id"
  _set_from_file "VERCEL_TOKEN"          "$SECRETS_DIR/vercel/token"
  _set_from_file "VERCEL_ORG_ID"         "$SECRETS_DIR/vercel/org_id"
  _set_from_file "VERCEL_PROJECT_ID"     "$SECRETS_DIR/vercel/project_id"
  echo ""
}

# ── Dispatch ──────────────────────────────────────────────────────────────────
case "${ONLY:-all}" in
  ios)      _sync_ios ;;
  android)  _sync_android ;;
  firebase) _sync_firebase ;;
  play)     _sync_play ;;
  web)      _sync_web ;;
  all)
    _sync_ios
    _sync_android
    _sync_firebase
    _sync_play
    _sync_web
    ;;
  *)
    echo "Unknown --only value: $ONLY"
    echo "Valid: ios | android | firebase | play | web"
    exit 1
    ;;
esac

# ── Summary ───────────────────────────────────────────────────────────────────
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if $DRY_RUN; then
  echo "  [DRY RUN] Would push: $PUSHED  |  Skipped: $SKIPPED  |  Errors: $ERRORS"
else
  echo "  Pushed: $PUSHED  |  Skipped: $SKIPPED  |  Errors: $ERRORS"
fi
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [[ $ERRORS -gt 0 ]]; then
  echo "⚠️  Some secrets failed to push. Check output above."
  exit 1
fi

if ! $DRY_RUN && [[ $PUSHED -gt 0 ]]; then
  echo "✅  Secrets synced to $REPO"
  echo "    Trigger a workflow run to verify: gh workflow run --repo $REPO"
fi
