#!/usr/bin/env bash
# deployment/_shared/scripts/materialize-android-secrets.sh
#
# Manual-mode (no vault) secrets materialization for Android lanes.
# Vault-mode is handled by secrets-pull.sh — this script is the fallback when
# the vault is unavailable, reading GitHub Secrets from env vars.
#
# Env vars consumed (set by GHA workflow):
#   GOOGLESERVICES                      Google Services JSON content
#   PLAYSTORECREDS                      Play Store SA JSON content
#   FIREBASECREDS                       Firebase App Distribution SA JSON content
#   ORIGINAL_KEYSTORE_FILE              Base64 release keystore
#   ORIGINAL_KEYSTORE_FILE_PASSWORD     Keystore password
#   ORIGINAL_KEYSTORE_ALIAS             Keystore alias
#   ORIGINAL_KEYSTORE_ALIAS_PASSWORD    Keystore alias password
#   UPLOAD_KEYSTORE_FILE                Base64 upload keystore
#   UPLOAD_KEYSTORE_FILE_PASSWORD       Upload keystore password
#   UPLOAD_KEYSTORE_ALIAS               Upload keystore alias
#   UPLOAD_KEYSTORE_ALIAS_PASSWORD      Upload keystore alias password
set -euo pipefail

mkdir -p secrets keystores cmp-android/src/prod cmp-android/src/demo

# Google Services — required for prod + demo flavors.
if [[ -n "${GOOGLESERVICES:-}" ]]; then
  printf '%s' "$GOOGLESERVICES" > cmp-android/src/prod/google-services.json
  printf '%s' "$GOOGLESERVICES" > cmp-android/src/demo/google-services.json
fi

# Play Store + Firebase distribution credentials.
[[ -n "${PLAYSTORECREDS:-}"  ]] && printf '%s' "$PLAYSTORECREDS"  > secrets/playStorePublishServiceCredentialsFile.json
[[ -n "${FIREBASECREDS:-}"   ]] && printf '%s' "$FIREBASECREDS"   > secrets/firebaseAppDistributionServiceCredentialsFile.json

# ORIGINAL keystore (app signing key — Play App Signing identity).
if [[ -n "${ORIGINAL_KEYSTORE_FILE:-}" ]]; then
  echo "$ORIGINAL_KEYSTORE_FILE" | base64 -d > keystores/original_keystore.keystore
fi

# UPLOAD keystore (signs the bundle that goes to Play Console — Play re-signs with the app signing key).
if [[ -n "${UPLOAD_KEYSTORE_FILE:-}" ]]; then
  echo "$UPLOAD_KEYSTORE_FILE" | base64 -d > keystores/upload_keystore.keystore
fi

echo "✅ Android secrets materialized (manual-mode)"
