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
# Play App Signing model (per https://support.google.com/googleplay/android-developer/answer/9842756):
#   Google holds the app signing key; developer holds ONLY the upload key.
#   Android requires ONE keystore family:
#     UPLOAD_KEYSTORE_FILE + _PASSWORD + _ALIAS + _ALIAS_PASSWORD → Play Console upload key
#   90%+ of new apps use this default; Google's KMS holds the app signing key.

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
#
# Dual-write transition: also pushes the actionhub v2 lowercase secret names
# (google_services, upload_keystore, etc.) so consumers can use `secrets: inherit`
# while their old UPPERCASE names still work. After consumers verify lowercase
# names work, the UPPERCASE family can be deleted.
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
      storePassword)
        _set_secret "${prefix}_KEYSTORE_FILE_PASSWORD"  "$v" "$props"
        # Dual-write: actionhub v2 lowercase name (consumer can use `secrets: inherit`)
        _set_secret "keystore_password"                 "$v" "$props (v2 alias)"
        ;;
      keyAlias)
        _set_secret "${prefix}_KEYSTORE_ALIAS"          "$v" "$props"
        _set_secret "keystore_alias"                    "$v" "$props (v2 alias)"
        ;;
      keyPassword)
        _set_secret "${prefix}_KEYSTORE_ALIAS_PASSWORD" "$v" "$props"
        _set_secret "keystore_alias_password"           "$v" "$props (v2 alias)"
        ;;
    esac
  done < "$props"

  _set_from_binary "${prefix}_KEYSTORE_FILE" "$jks"
  # Dual-write: v2 lowercase keystore alias
  if [[ -f "$jks" && $(head -c 16 "$jks") != "CLAUDE-PLHLD-v1" ]]; then
    local b64; b64=$(base64 -i "$jks" 2>/dev/null || base64 "$jks")
    _set_secret "upload_keystore" "$b64" "$jks (base64, v2 alias)"
  fi
}

# ── iOS secrets ───────────────────────────────────────────────────────────────
_sync_ios() {
  echo "📱 iOS / App Store"
  # Dual-write each secret: legacy UPPERCASE + v2 lowercase
  _set_from_file    "APPSTORE_KEY_ID"        "$SECRETS_DIR/appstore/key_id"
  _set_from_file    "appstore_key_id"        "$SECRETS_DIR/appstore/key_id"
  _set_from_file    "APPSTORE_ISSUER_ID"     "$SECRETS_DIR/appstore/issuer_id"
  _set_from_file    "appstore_issuer_id"     "$SECRETS_DIR/appstore/issuer_id"
  _set_from_binary  "APPSTORE_AUTH_KEY"      "$SECRETS_DIR/appstore/AuthKey.p8"
  _set_from_binary  "appstore_auth_key"      "$SECRETS_DIR/appstore/AuthKey.p8"
  _set_from_binary  "MATCH_GIT_PRIVATE_KEY"  "$SECRETS_DIR/match/match_ci_key"
  _set_from_binary  "match_ssh_private_key"  "$SECRETS_DIR/match/match_ci_key"
  _set_from_file    "MATCH_PASSWORD"         "$SECRETS_DIR/match/.match_password"
  _set_from_file    "match_password"         "$SECRETS_DIR/match/.match_password"

  # iOS identity/metadata — read from gradle/fork.properties (non-secret)
  local fork_props="gradle/fork.properties"
  if [[ -f "$fork_props" ]]; then
    echo "  Parsing $fork_props for non-secret iOS metadata …"
    while IFS='=' read -r key val; do
      [[ -z "$key" || "$key" =~ ^# ]] && continue
      [[ -z "$val" || "$val" == "YOUR_"* || "$val" == "your_"* ]] && continue
      _set_secret "IOS_ENV_${key}" "$val" "fork.properties:${key}"
      _set_secret "${key}"         "$val" "fork.properties:${key} (cross-platform)"
    done < "$fork_props"
  fi

  # iOS App Store Connect secrets — read from per-value files (secret)
  if [[ -f "$SECRETS_DIR/apple/appstore/key_id" ]]; then
    local _asc_key_id
    _asc_key_id=$(cat "$SECRETS_DIR/apple/appstore/key_id" 2>/dev/null | tr -d '\n\r')
    [[ -n "$_asc_key_id" ]] && _set_secret "APPSTORE_KEY_ID" "$_asc_key_id" "apple/appstore/key_id"
  fi
  if [[ -f "$SECRETS_DIR/apple/appstore/issuer_id" ]]; then
    local _asc_issuer
    _asc_issuer=$(cat "$SECRETS_DIR/apple/appstore/issuer_id" 2>/dev/null | tr -d '\n\r')
    [[ -n "$_asc_issuer" ]] && _set_secret "APPSTORE_ISSUER_ID" "$_asc_issuer" "apple/appstore/issuer_id"
  fi
  echo ""
}

# ── Android secrets ───────────────────────────────────────────────────────────
# Play App Signing model: ONE keystore (UPLOAD). Google holds the app signing key.
_sync_android() {
  echo "🤖 Android / Play Store (Play App Signing model — single UPLOAD keystore)"

  # UPLOAD keystore (Play Console upload key) — the only keystore the developer holds
  _set_keystore_family "UPLOAD" \
    "$SECRETS_DIR/keystores/upload_keystore.properties" \
    "$SECRETS_DIR/keystores/upload_keystore.keystore"

  # google-services.json — required by all Android builds
  _set_from_binary "GOOGLESERVICES"   "$SECRETS_DIR/firebase/google-services.json"
  _set_from_binary "google_services"  "$SECRETS_DIR/firebase/google-services.json"

  # Firebase / Play Store service account JSON (dual-write)
  _set_from_binary "FIREBASECREDS"  "$SECRETS_DIR/firebase/service-account.json"
  _set_from_binary "firebase_creds" "$SECRETS_DIR/firebase/service-account.json"
  _set_from_binary "PLAYSTORECREDS"  "$SECRETS_DIR/play/service-account.json"
  _set_from_binary "playstore_creds" "$SECRETS_DIR/play/service-account.json"
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
