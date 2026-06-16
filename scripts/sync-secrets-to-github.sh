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
#   bash scripts/sync-secrets-to-github.sh --only mac
#   bash scripts/sync-secrets-to-github.sh --only windows
#   bash scripts/sync-secrets-to-github.sh --only linux
#   bash scripts/sync-secrets-to-github.sh --only microsoft-store
#   bash scripts/sync-secrets-to-github.sh --only azure-signing
#   bash scripts/sync-secrets-to-github.sh --only web
#
# v2 (Play App Signing model):
#   Android requires BOTH keystore families:
#     ORIGINAL_KEYSTORE_FILE  + _PASSWORD + _ALIAS + _ALIAS_PASSWORD  → app signing key
#     UPLOAD_KEYSTORE_FILE    + _PASSWORD + _ALIAS + _ALIAS_PASSWORD  → Play Console upload key
#   Single-keystore mode (no Play App Signing): point both families at the same file.

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
    if [[ $(head -c 16 "$path") == "CLAUDE-PLHLD-v1" ]]; then
      echo "  ⚠️  SKIP $name — still a placeholder in $path"
      ((SKIPPED++)) || true
      return
    fi
    local value; value=$(base64 -i "$path" 2>/dev/null || base64 "$path")
    _set_secret "$name" "$value" "$path (base64)"
  else
    echo "  ⏭   SKIP $name — file not found: $path"
    ((SKIPPED++)) || true
  fi
}

# Push one keystore family from a .properties file + a .jks/.keystore binary.
#   $1 = family prefix (ORIGINAL | UPLOAD)
#   $2 = properties file path
#   $3 = keystore file path
# Properties keys recognized (case-sensitive):
#   storePassword | keyAlias | keyPassword | storeFile (informational; binary path overrides)
_set_keystore_family() {
  local prefix="$1"
  local props="$2"
  local jks="$3"

  if [[ ! -f "$props" ]]; then
    echo "  ⏭   ${prefix}: props file not found: $props"
    ((SKIPPED+=4)) || true
    return
  fi

  if grep -q "CLAUDE-PLACEHOLDER" "$props"; then
    echo "  ⚠️  ${prefix}: $props is still a placeholder — fill it in"
    ((SKIPPED+=4)) || true
    return
  fi

  while IFS='=' read -r k v; do
    [[ -z "$k" || "$k" =~ ^# ]] && continue
    case "$k" in
      storePassword) _set_secret "${prefix}_KEYSTORE_FILE_PASSWORD"  "$v" "$props" ;;
      keyAlias)      _set_secret "${prefix}_KEYSTORE_ALIAS"          "$v" "$props" ;;
      keyPassword)   _set_secret "${prefix}_KEYSTORE_ALIAS_PASSWORD" "$v" "$props" ;;
    esac
  done < "$props"

  _set_from_binary "${prefix}_KEYSTORE_FILE" "$jks"
}

# ── iOS secrets ───────────────────────────────────────────────────────────────
_sync_ios() {
  echo "📱 iOS / App Store"
  _set_from_file    "APPSTORE_KEY_ID"    "$SECRETS_DIR/appstore/key_id"
  _set_from_file    "APPSTORE_ISSUER_ID" "$SECRETS_DIR/appstore/issuer_id"
  _set_from_binary  "APPSTORE_AUTH_KEY"  "$SECRETS_DIR/appstore/AuthKey.p8"
  _set_from_binary  "MATCH_GIT_PRIVATE_KEY" "$SECRETS_DIR/match/match_ci_key"
  _set_from_file    "MATCH_PASSWORD"     "$SECRETS_DIR/match/.match_password"

  # shared_keys.env — parse and set individual vars (FRED_API_KEY etc.)
  local env_file="$SECRETS_DIR/shared_keys.env"
  if [[ -f "$env_file" ]] && ! grep -q "CLAUDE-PLACEHOLDER" "$env_file"; then
    echo "  Parsing $env_file …"
    while IFS='=' read -r key val; do
      [[ -z "$key" || "$key" =~ ^# ]] && continue
      key="${key#export }"
      val="${val%\"}"
      val="${val#\"}"
      [[ -z "$val" || "$val" == "YOUR_"* || "$val" == "your_"* ]] && continue
      # Push as IOS_ENV_<KEY> for iOS Fastlane consumption AND as plain <KEY> for cross-platform use
      _set_secret "IOS_ENV_${key}" "$val" "shared_keys.env:${key}"
      _set_secret "${key}"         "$val" "shared_keys.env:${key} (cross-platform)"
    done < "$env_file"
  fi
  echo ""
}

# ── Android secrets ───────────────────────────────────────────────────────────
# v2 model — two keystores: ORIGINAL (app signing) + UPLOAD (Play Console).
# In single-keystore mode, point both families at the same .jks file.
_sync_android() {
  echo "🤖 Android / Play Store (v2 — Play App Signing model)"

  # ORIGINAL keystore (app signing key — Play App Signing identity)
  _set_keystore_family "ORIGINAL" \
    "$SECRETS_DIR/keystores/original.properties" \
    "$SECRETS_DIR/keystores/original_keystore.keystore"

  # UPLOAD keystore (Play Console upload key)
  _set_keystore_family "UPLOAD" \
    "$SECRETS_DIR/keystores/upload.properties" \
    "$SECRETS_DIR/keystores/upload_keystore.keystore"

  # Single-keystore fallback — if neither original.properties nor upload.properties
  # has REAL credentials (missing OR still a placeholder) but legacy release.properties
  # does, push it as both ORIGINAL and UPLOAD families so v2 workflows still sign.
  local _orig="$SECRETS_DIR/keystores/original.properties"
  local _upld="$SECRETS_DIR/keystores/upload.properties"
  local _rels="$SECRETS_DIR/keystores/release.properties"
  if   { [[ ! -f "$_orig" ]] || grep -q "CLAUDE-PLACEHOLDER" "$_orig"; } \
    && { [[ ! -f "$_upld" ]] || grep -q "CLAUDE-PLACEHOLDER" "$_upld"; } \
    &&   [[   -f "$_rels" ]] && ! grep -q "CLAUDE-PLACEHOLDER" "$_rels"; then
    echo "  ℹ️  legacy single-keystore mode detected (release.properties) — pushing as ORIGINAL + UPLOAD"
    local jks_name
    jks_name=$(grep '^storeFile=' "$_rels" 2>/dev/null | cut -d'=' -f2-)
    _set_keystore_family "ORIGINAL" "$_rels" "$SECRETS_DIR/keystores/${jks_name:-release.jks}"
    _set_keystore_family "UPLOAD"   "$_rels" "$SECRETS_DIR/keystores/${jks_name:-release.jks}"
  fi

  # google-services.json — required by all Android builds
  _set_from_binary "GOOGLESERVICES" "$SECRETS_DIR/firebase/google-services.json"
  echo ""
}

# ── Firebase secrets ──────────────────────────────────────────────────────────
_sync_firebase() {
  echo "🔥 Firebase"
  _set_from_binary "FIREBASECREDS"          "$SECRETS_DIR/firebase/service-account.json"
  _set_from_file   "FIREBASE_ANDROID_APP_ID"     "$SECRETS_DIR/firebase/android_app_id"
  _set_from_file   "FIREBASE_ANDROID_DEMO_APP_ID" "$SECRETS_DIR/firebase/android_demo_app_id"
  _set_from_file   "FIREBASE_IOS_APP_ID"     "$SECRETS_DIR/firebase/ios_app_id"
  _set_from_file   "FIREBASE_IOS_DEMO_APP_ID" "$SECRETS_DIR/firebase/ios_demo_app_id"
  _set_from_file   "FIREBASE_IOS_PROD_APP_ID" "$SECRETS_DIR/firebase/ios_prod_app_id"
  echo ""
}

# ── Play Store secrets ────────────────────────────────────────────────────────
_sync_play() {
  echo "▶  Play Store"
  _set_from_binary "PLAYSTORECREDS" "$SECRETS_DIR/play/service-account.json"
  echo ""
}

# ── macOS signing secrets (Mac App Store / TestFlight) ────────────────────────
_sync_mac() {
  echo "🍏 macOS / App Store"
  _set_from_binary "MACOS_SIGNING_KEY"          "$SECRETS_DIR/mac/signing_key.p12"
  _set_from_file   "MACOS_SIGNING_PASSWORD"     "$SECRETS_DIR/mac/signing_password"
  _set_from_binary "MACOS_SIGNING_CERTIFICATE"  "$SECRETS_DIR/mac/signing_certificate.cer"
  _set_from_binary "MACOS_INSTALLER_KEY"        "$SECRETS_DIR/mac/installer_key.p12"
  _set_from_file   "MACOS_INSTALLER_PASSWORD"   "$SECRETS_DIR/mac/installer_password"
  _set_from_binary "MAC_PROVISIONING_PROFILE_BASE64" "$SECRETS_DIR/mac/provisioning_profile.provisionprofile"
  _set_from_file   "MAC_BUNDLE_IDENTIFIER"      "$SECRETS_DIR/mac/bundle_identifier"
  echo ""
}

# ── Windows signing secrets ───────────────────────────────────────────────────
_sync_windows() {
  echo "🪟 Windows / Desktop"
  _set_from_binary "WINDOWS_SIGNING_KEY"          "$SECRETS_DIR/windows/signing_key.pfx"
  _set_from_file   "WINDOWS_SIGNING_PASSWORD"     "$SECRETS_DIR/windows/signing_password"
  _set_from_binary "WINDOWS_SIGNING_CERTIFICATE"  "$SECRETS_DIR/windows/signing_certificate.cer"
  echo ""
}

# ── Linux signing secrets ─────────────────────────────────────────────────────
_sync_linux() {
  echo "🐧 Linux / Desktop"
  _set_from_binary "LINUX_SIGNING_KEY"          "$SECRETS_DIR/linux/signing_key.gpg"
  _set_from_file   "LINUX_SIGNING_PASSWORD"     "$SECRETS_DIR/linux/signing_password"
  _set_from_binary "LINUX_SIGNING_CERTIFICATE"  "$SECRETS_DIR/linux/signing_certificate.pub"
  echo ""
}

# ── Microsoft Store (Partner Center API) ──────────────────────────────────────
_sync_microsoft_store() {
  echo "🛒 Microsoft Store / Partner Center"
  _set_from_file "MS_STORE_TENANT_ID"     "$SECRETS_DIR/microsoft-store/tenant_id"
  _set_from_file "MS_STORE_CLIENT_ID"     "$SECRETS_DIR/microsoft-store/client_id"
  _set_from_file "MS_STORE_CLIENT_SECRET" "$SECRETS_DIR/microsoft-store/client_secret"
  _set_from_file "MS_STORE_SELLER_ID"     "$SECRETS_DIR/microsoft-store/seller_id"
  _set_from_file "MS_STORE_APP_ID"        "$SECRETS_DIR/microsoft-store/app_id"
  echo ""
}

# ── Azure Trusted Signing (Windows MSI signing) ───────────────────────────────
_sync_azure_signing() {
  echo "☁️  Azure Trusted Signing (Windows MSI)"
  _set_from_file "AZURE_TENANT_ID"             "$SECRETS_DIR/azure-signing/tenant_id"
  _set_from_file "AZURE_CLIENT_ID"             "$SECRETS_DIR/azure-signing/client_id"
  _set_from_file "AZURE_CLIENT_SECRET"         "$SECRETS_DIR/azure-signing/client_secret"
  _set_from_file "AZURE_SIGNING_ENDPOINT"      "$SECRETS_DIR/azure-signing/endpoint"
  _set_from_file "AZURE_SIGNING_ACCOUNT_NAME"  "$SECRETS_DIR/azure-signing/account_name"
  _set_from_file "AZURE_SIGNING_CERT_PROFILE"  "$SECRETS_DIR/azure-signing/cert_profile_name"
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
  ios)              _sync_ios ;;
  android)          _sync_android ;;
  firebase)         _sync_firebase ;;
  play)             _sync_play ;;
  mac|macos)        _sync_mac ;;
  windows|win)      _sync_windows ;;
  linux)            _sync_linux ;;
  microsoft-store|ms-store) _sync_microsoft_store ;;
  azure-signing|azure)      _sync_azure_signing ;;
  web)              _sync_web ;;
  all)
    _sync_ios
    _sync_android
    _sync_firebase
    _sync_play
    _sync_mac
    _sync_windows
    _sync_linux
    _sync_microsoft_store
    _sync_azure_signing
    _sync_web
    ;;
  *)
    echo "Unknown --only value: $ONLY"
    echo "Valid: ios | android | firebase | play | mac | windows | linux | microsoft-store | azure-signing | web | all"
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
