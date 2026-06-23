#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════════════════════════════
# cert-renewal.sh — Local certificate renewal for openMF iOS/macOS signing
# ═══════════════════════════════════════════════════════════════════════════════
#
# Manages ALL signing cert types stored in github.com/openMF/ios-provisioning-profile:
#
#   TYPE                         PLATFORM  COVERS
#   appstore                     iOS       TestFlight + App Store
#   adhoc                        iOS       Firebase App Distribution
#   appstore                     macOS     Mac App Store .app codesign
#   mac_installer_distribution   macOS     Mac App Store .pkg signing (productbuild)
#
# HOW TO USE — two modes:
#
#   Mode A: Standalone from ios-provisioning-profile (CANONICAL — commit script here)
#     git clone git@github.com:openMF/ios-provisioning-profile.git
#     cd ios-provisioning-profile
#     bash cert-renewal.sh [OPTIONS]
#     # Requires: gem install fastlane  (one-time setup)
#
#   Mode B: From kmp-project-template/deployment/ (Bundler context — used by CI)
#     cd deployment
#     bash _shared/scripts/cert-renewal.sh [OPTIONS]
#     # Uses `bundle exec fastlane` automatically — no global install needed.
#
# OPTIONS:
#   --force                    Renew all certs regardless of expiry (default: false)
#   --type TYPE                all | appstore | adhoc | mac_installer_distribution
#   --threshold DAYS           Trigger renewal if expiry within N days (default: 30)
#   --app-ids "id1 id2"        Space-separated bundle IDs (default: see APP_IDENTIFIERS below)
#   --primary-id ID            Primary bundle ID for macOS-only certs
#   --git-url URL              Match git repo URL (default: git@github.com:openMF/ios-provisioning-profile.git)
#   --git-branch BRANCH        Match git branch (default: master)
#   --ssh-key PATH             Path to SSH private key for Match repo write access
#   --fastlane-cmd CMD         Override fastlane invocation (e.g. "bundle exec fastlane")
#   --run-dir DIR              Working directory for fastlane commands
#   --dry-run                  Print what would happen; do not call fastlane
#
# REQUIRED SECRETS (ENV vars take priority; file fallback for local use):
#
#   ENV var                  File (relative to this script's directory)
#   ─────────────────────    ────────────────────────────────────────────
#   MATCH_PASSWORD           .match_password   (decrypts the Match git repo)
#   MATCH_GIT_PRIVATE_KEY    match_ci_key      (SSH key with write access)
#   APPSTORE_KEY_ID          key_id            (App Store Connect API key ID)
#   APPSTORE_ISSUER_ID       issuer_id         (App Store Connect API issuer ID)
#   APPSTORE_AUTH_KEY        AuthKey.p8        (p8 content — base64 or raw PEM)
#
#   When run from deployment/: secrets auto-discovered from secrets/apple/match/ and secrets/apple/appstore/.
#   When run from ios-provisioning-profile/: place secrets in a secrets/ subdirectory OR export
#   the ENV vars directly before running the script.
#
# This script is the local equivalent of .github/workflows/ios-cert-renewal.yml.
# It can be committed to openMF/ios-provisioning-profile as cert-renewal.sh so
# collaborators can perform renewals without needing the kmp-project-template repo.
#
# EXAMPLE — first-time bootstrap of the full Match repo:
#   export MATCH_PASSWORD="..."
#   export APPSTORE_KEY_ID="..."
#   export APPSTORE_ISSUER_ID="..."
#   export APPSTORE_AUTH_KEY="$(cat ~/Downloads/AuthKey_ABC.p8 | base64)"
#   bash cert-renewal.sh --force --type all
# ═══════════════════════════════════════════════════════════════════════════════

set -euo pipefail

# ── Defaults ──────────────────────────────────────────────────────────────────
FORCE=false
CERT_TYPE="all"
RENEW_THRESHOLD_DAYS=30
APP_IDENTIFIERS="org.mifos.kmp.template org.mifospay"
PRIMARY_APP_ID="org.mifos.kmp.template"
GIT_URL="git@github.com:openMF/ios-provisioning-profile.git"
GIT_BRANCH="master"
DRY_RUN=false
SSH_KEY_OVERRIDE=""
FASTLANE_CMD_OVERRIDE=""
RUN_DIR_OVERRIDE=""

# ── Argument parsing ──────────────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
  case "$1" in
    --force)            FORCE=true;                    shift ;;
    --type)             CERT_TYPE="$2";                shift 2 ;;
    --threshold)        RENEW_THRESHOLD_DAYS="$2";     shift 2 ;;
    --app-ids)          APP_IDENTIFIERS="$2";          shift 2 ;;
    --primary-id)       PRIMARY_APP_ID="$2";           shift 2 ;;
    --git-url)          GIT_URL="$2";                  shift 2 ;;
    --git-branch)       GIT_BRANCH="$2";               shift 2 ;;
    --ssh-key)          SSH_KEY_OVERRIDE="$2";         shift 2 ;;
    --fastlane-cmd)     FASTLANE_CMD_OVERRIDE="$2";    shift 2 ;;
    --run-dir)          RUN_DIR_OVERRIDE="$2";         shift 2 ;;
    --dry-run)          DRY_RUN=true;                  shift ;;
    -h|--help)
      head -60 "$0" | grep '^#' | sed 's/^# \{0,1\}//'
      exit 0
      ;;
    *)
      echo "Unknown option: $1  (use --help for usage)" >&2
      exit 1
      ;;
  esac
done

# ── Locate this script's directory ───────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Detect run context:
#   Mode A (canonical): ios-provisioning-profile/ → standalone, uses system fastlane
#   Mode B (CI/bundler): kmp-project-template/deployment/_shared/scripts/ → uses bundle exec
# --fastlane-cmd and --run-dir override auto-detection (used by CI to inject bundler context).
if [[ "$SCRIPT_DIR" == *"/deployment/_shared/scripts" ]]; then
  REPO_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"   # kmp-project-template root
  SECRETS_DIR="$REPO_ROOT/secrets"
  FASTLANE_CMD="${FASTLANE_CMD_OVERRIDE:-bundle exec fastlane}"
  RUN_DIR="${RUN_DIR_OVERRIDE:-$REPO_ROOT/deployment}"
else
  REPO_ROOT="$SCRIPT_DIR"                           # ios-provisioning-profile root
  SECRETS_DIR="$SCRIPT_DIR/secrets"
  FASTLANE_CMD="${FASTLANE_CMD_OVERRIDE:-fastlane}"
  RUN_DIR="${RUN_DIR_OVERRIDE:-$SCRIPT_DIR}"
fi

# ── Colour helpers ────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
info()    { echo -e "${BLUE}ℹ  $*${NC}"; }
success() { echo -e "${GREEN}✅ $*${NC}"; }
warn()    { echo -e "${YELLOW}⚠️  $*${NC}"; }
error()   { echo -e "${RED}❌ $*${NC}" >&2; }

# ── Secret resolution ─────────────────────────────────────────────────────────
# ENV vars take priority (CI mode); files under secrets/ are the local fallback.
_read_secret() {
  local env_var="$1" file_path="$2"
  local val="${!env_var:-}"
  if [[ -z "$val" ]] && [[ -n "$file_path" ]] && [[ -f "$file_path" ]]; then
    val="$(cat "$file_path")"
  fi
  echo "$val"
}

MATCH_PASSWORD_VAL="$(_read_secret MATCH_PASSWORD "$SECRETS_DIR/match/.match_password")"
MATCH_SSH_KEY="$SSH_KEY_OVERRIDE"
if [[ -z "$MATCH_SSH_KEY" ]]; then
  if [[ -f "$SECRETS_DIR/match/match_ci_key" ]]; then
    MATCH_SSH_KEY="$SECRETS_DIR/match/match_ci_key"
  elif [[ -n "${MATCH_GIT_PRIVATE_KEY:-}" ]]; then
    # Write the env-var key content to a temp file.
    MATCH_SSH_KEY="$(mktemp /tmp/match_ci_key.XXXXXX)"
    printf '%s' "$MATCH_GIT_PRIVATE_KEY" > "$MATCH_SSH_KEY"
    chmod 600 "$MATCH_SSH_KEY"
    trap 'rm -f "$MATCH_SSH_KEY"' EXIT
  fi
fi

APPSTORE_KEY_ID="$(_read_secret APPSTORE_KEY_ID "$SECRETS_DIR/appstore/key_id")"
APPSTORE_ISSUER_ID="$(_read_secret APPSTORE_ISSUER_ID "$SECRETS_DIR/appstore/issuer_id")"

# AuthKey.p8 — may be raw PEM or base64-encoded; write to temp file.
AUTH_KEY_PATH="$SECRETS_DIR/appstore/AuthKey.p8"
if [[ ! -f "$AUTH_KEY_PATH" ]]; then
  AUTH_KEY_CONTENT="${APPSTORE_AUTH_KEY:-}"
  if [[ -n "$AUTH_KEY_CONTENT" ]]; then
    AUTH_KEY_PATH="$(mktemp /tmp/AuthKey.XXXXXX.p8)"
    if echo "$AUTH_KEY_CONTENT" | grep -q "BEGIN PRIVATE KEY"; then
      printf '%s' "$AUTH_KEY_CONTENT" > "$AUTH_KEY_PATH"
    else
      echo "$AUTH_KEY_CONTENT" | base64 --decode > "$AUTH_KEY_PATH"
    fi
    chmod 600 "$AUTH_KEY_PATH"
    trap 'rm -f "$AUTH_KEY_PATH" "${MATCH_SSH_KEY:-}"' EXIT
  fi
fi

# ── Preflight validation ───────────────────────────────────────────────────────
PREFLIGHT_OK=true
[[ -z "$MATCH_PASSWORD_VAL" ]] && { error "MATCH_PASSWORD not set and secrets/apple/match/.match_password not found"; PREFLIGHT_OK=false; }
[[ -z "$MATCH_SSH_KEY" ]]      && { error "SSH key not found — set MATCH_GIT_PRIVATE_KEY or place key at secrets/apple/match/match_ci_key"; PREFLIGHT_OK=false; }
[[ -z "$APPSTORE_KEY_ID" ]]    && { error "APPSTORE_KEY_ID not set and secrets/apple/appstore/key_id not found"; PREFLIGHT_OK=false; }
[[ -z "$APPSTORE_ISSUER_ID" ]] && { error "APPSTORE_ISSUER_ID not set and secrets/apple/appstore/issuer_id not found"; PREFLIGHT_OK=false; }
[[ ! -f "$AUTH_KEY_PATH" ]]    && { error "AuthKey.p8 not found — set APPSTORE_AUTH_KEY or place file at secrets/apple/appstore/AuthKey.p8"; PREFLIGHT_OK=false; }

if ! "$PREFLIGHT_OK"; then
  echo ""
  echo "Set the missing secrets as ENV vars or place files in $(dirname "$SECRETS_DIR")/secrets/:"
  echo "  secrets/apple/match/.match_password"
  echo "  secrets/apple/match/match_ci_key"
  echo "  secrets/apple/appstore/key_id"
  echo "  secrets/apple/appstore/issuer_id"
  echo "  secrets/apple/appstore/AuthKey.p8"
  exit 1
fi

# ── Check fastlane availability ───────────────────────────────────────────────
if ! command -v fastlane >/dev/null 2>&1 && ! (cd "$RUN_DIR" && bundle exec fastlane --version >/dev/null 2>&1); then
  error "fastlane not found."
  echo "  Install globally:  gem install fastlane"
  echo "  Or from deployment/: bundle install"
  exit 1
fi

# ── SSH agent setup ──────────────────────────────────────────────────────────
info "Starting SSH agent and adding Match key..."
eval "$(ssh-agent -s)" >/dev/null
ssh-add "$MATCH_SSH_KEY" 2>/dev/null || { error "ssh-add failed for $MATCH_SSH_KEY"; exit 1; }
trap 'ssh-agent -k >/dev/null 2>&1; rm -f "${MATCH_SSH_KEY:-}" "${AUTH_KEY_PATH:-}"' EXIT

# Validate SSH access to the Match repo.
info "Validating SSH access to $GIT_URL ..."
GIT_SSH_COMMAND="ssh -i $MATCH_SSH_KEY -o StrictHostKeyChecking=no" \
  git ls-remote "$GIT_URL" HEAD >/dev/null 2>&1 \
  || { error "Cannot access $GIT_URL with the provided SSH key"; exit 1; }
success "SSH access confirmed"

# ── Expiry check ──────────────────────────────────────────────────────────────
NEEDS_RENEWAL=false
RENEWAL_REASON=""

if [[ "$FORCE" == "true" ]]; then
  NEEDS_RENEWAL=true
  RENEWAL_REASON="--force flag set"
  warn "Force mode: skipping expiry check, renewing all certs"
else
  info "Checking certificate expiry (threshold: ${RENEW_THRESHOLD_DAYS} days)..."

  MATCH_TMP="$(mktemp -d /tmp/match-check.XXXXXX)"
  trap 'rm -rf "$MATCH_TMP"; ssh-agent -k >/dev/null 2>&1' EXIT

  GIT_SSH_COMMAND="ssh -i $MATCH_SSH_KEY -o StrictHostKeyChecking=no" \
    git clone --depth 1 --single-branch --branch "$GIT_BRANCH" \
    "$GIT_URL" "$MATCH_TMP" >/dev/null 2>&1

  while IFS= read -r -d '' CER_FILE; do
    EXPIRY="$(openssl x509 -in "$CER_FILE" -noout -enddate 2>/dev/null | sed 's/notAfter=//')"
    [[ -z "$EXPIRY" ]] && continue

    # macOS date (BSD) and Linux date (GNU) differ in format-string syntax.
    if date --version >/dev/null 2>&1; then
      # GNU date
      EXPIRY_TS="$(date -d "$EXPIRY" "+%s" 2>/dev/null || echo 0)"
    else
      # BSD date (macOS)
      EXPIRY_TS="$(date -j -f "%b %d %T %Y %Z" "$EXPIRY" "+%s" 2>/dev/null || echo 0)"
    fi

    NOW_TS="$(date "+%s")"
    DAYS_LEFT=$(( (EXPIRY_TS - NOW_TS) / 86400 ))
    CERT_NAME="$(basename "$CER_FILE")"
    echo "  📜  $CERT_NAME → ${DAYS_LEFT} days left (expires: $EXPIRY)"

    if [[ "$DAYS_LEFT" -le "$RENEW_THRESHOLD_DAYS" ]]; then
      NEEDS_RENEWAL=true
      RENEWAL_REASON="$CERT_NAME expires in ${DAYS_LEFT}d (threshold ${RENEW_THRESHOLD_DAYS}d)"
    fi
  done < <(find "$MATCH_TMP/certs" -name "*.cer" -print0 2>/dev/null)

  rm -rf "$MATCH_TMP"

  if [[ "$NEEDS_RENEWAL" == "true" ]]; then
    warn "Renewal triggered: $RENEWAL_REASON"
  else
    success "All certificates are fresh (>${RENEW_THRESHOLD_DAYS} days remaining). No renewal needed."
    echo "  Run with --force to force a full refresh."
    exit 0
  fi
fi

# ── Dry-run gate ──────────────────────────────────────────────────────────────
if [[ "$DRY_RUN" == "true" ]]; then
  warn "DRY RUN — the following fastlane commands would be executed:"
  echo ""
  echo "  export MATCH_PASSWORD=... (from secrets)"
  for APP_ID in $APP_IDENTIFIERS; do
    if [[ "$CERT_TYPE" == "all" || "$CERT_TYPE" == "appstore" ]]; then
      echo "  fastlane run match type:appstore platform:ios app_identifier:$APP_ID git_url:$GIT_URL git_branch:$GIT_BRANCH readonly:false force_for_new_certificates:true"
    fi
    if [[ "$CERT_TYPE" == "all" || "$CERT_TYPE" == "adhoc" ]]; then
      echo "  fastlane run match type:adhoc platform:ios app_identifier:$APP_ID git_url:$GIT_URL git_branch:$GIT_BRANCH readonly:false force_for_new_certificates:true"
    fi
  done
  if [[ "$CERT_TYPE" == "all" || "$CERT_TYPE" == "appstore" ]]; then
    echo "  fastlane run match type:appstore platform:macos app_identifier:$PRIMARY_APP_ID git_url:$GIT_URL git_branch:$GIT_BRANCH readonly:false"
  fi
  if [[ "$CERT_TYPE" == "all" || "$CERT_TYPE" == "mac_installer_distribution" ]]; then
    echo "  fastlane run match type:mac_installer_distribution platform:macos app_identifier:$PRIMARY_APP_ID git_url:$GIT_URL git_branch:$GIT_BRANCH readonly:false"
  fi
  exit 0
fi

# ── Export secrets into environment for fastlane ──────────────────────────────
export MATCH_PASSWORD="$MATCH_PASSWORD_VAL"
export APPSTORE_KEY_ID
export APPSTORE_ISSUER_ID

# ── Renewal runner ────────────────────────────────────────────────────────────
# Invokes `fastlane run match` directly — works with or without a Fastfile.
# `cd "$RUN_DIR"` ensures bundler uses the right Gemfile when in deployment/.
_run_match() {
  local type="$1" platform="$2" app_id="$3"
  info "match type:$type platform:$platform app_identifier:$app_id"
  (cd "$RUN_DIR" && $FASTLANE_CMD run match \
    "type:$type" \
    "platform:$platform" \
    "app_identifier:$app_id" \
    "git_url:$GIT_URL" \
    "git_branch:$GIT_BRANCH" \
    "git_private_key:$MATCH_SSH_KEY" \
    "api_key_path:$AUTH_KEY_PATH" \
    "api_key_id:$APPSTORE_KEY_ID" \
    "api_key_issuer_id:$APPSTORE_ISSUER_ID" \
    "readonly:false" \
    "force_for_new_certificates:true" \
    "skip_confirmation:true" \
  )
  success "match $type ($platform / $app_id) done"
}

# ── Nuke Distribution cert from Match repo (shared cert — only need once) ────
info "Nuking existing Distribution cert from Match repo..."
(cd "$RUN_DIR" && $FASTLANE_CMD run match_nuke \
  "type:distribution" \
  "git_url:$GIT_URL" \
  "git_branch:$GIT_BRANCH" \
  "git_private_key:$MATCH_SSH_KEY" \
  "api_key_path:$AUTH_KEY_PATH" \
  "api_key_id:$APPSTORE_KEY_ID" \
  "api_key_issuer_id:$APPSTORE_ISSUER_ID" \
  "skip_confirmation:true" \
)
success "Distribution cert nuked from Match repo"

# ── iOS appstore (TestFlight + App Store) ─────────────────────────────────────
if [[ "$CERT_TYPE" == "all" || "$CERT_TYPE" == "appstore" ]]; then
  echo ""
  info "━━ iOS appstore (TestFlight + App Store) ━━"
  for APP_ID in $APP_IDENTIFIERS; do
    _run_match "appstore" "ios" "$APP_ID"
  done
fi

# ── iOS adhoc (Firebase App Distribution) ────────────────────────────────────
if [[ "$CERT_TYPE" == "all" || "$CERT_TYPE" == "adhoc" ]]; then
  echo ""
  info "━━ iOS adhoc (Firebase App Distribution) ━━"
  for APP_ID in $APP_IDENTIFIERS; do
    _run_match "adhoc" "ios" "$APP_ID"
  done
fi

# ── macOS appstore (.app codesign) ───────────────────────────────────────────
if [[ "$CERT_TYPE" == "all" || "$CERT_TYPE" == "appstore" ]]; then
  echo ""
  info "━━ macOS appstore (.app codesign) ━━"
  _run_match "appstore" "macos" "$PRIMARY_APP_ID"
fi

# ── mac_installer_distribution (.pkg signing) ────────────────────────────────
if [[ "$CERT_TYPE" == "all" || "$CERT_TYPE" == "mac_installer_distribution" ]]; then
  echo ""
  info "━━ macOS mac_installer_distribution (.pkg signing) ━━"
  # mac_installer_distribution is a separate cert type — nuke it independently.
  (cd "$RUN_DIR" && $FASTLANE_CMD run match_nuke \
    "type:mac_installer_distribution" \
    "git_url:$GIT_URL" \
    "git_branch:$GIT_BRANCH" \
    "git_private_key:$MATCH_SSH_KEY" \
    "api_key_path:$AUTH_KEY_PATH" \
    "api_key_id:$APPSTORE_KEY_ID" \
    "api_key_issuer_id:$APPSTORE_ISSUER_ID" \
    "skip_confirmation:true" \
  ) || true   # non-fatal: may not exist yet on first run
  _run_match "mac_installer_distribution" "macos" "$PRIMARY_APP_ID"
fi

# ── Post-renewal verification ─────────────────────────────────────────────────
echo ""
info "Verifying renewed certificates..."

VERIFY_TMP="$(mktemp -d /tmp/match-verify.XXXXXX)"
GIT_SSH_COMMAND="ssh -i $MATCH_SSH_KEY -o StrictHostKeyChecking=no" \
  git clone --depth 1 --single-branch --branch "$GIT_BRANCH" \
  "$GIT_URL" "$VERIFY_TMP" >/dev/null 2>&1

FAILED=0
while IFS= read -r -d '' CER_FILE; do
  EXPIRY="$(openssl x509 -in "$CER_FILE" -noout -enddate 2>/dev/null | sed 's/notAfter=//')"
  [[ -z "$EXPIRY" ]] && continue

  if date --version >/dev/null 2>&1; then
    EXPIRY_TS="$(date -d "$EXPIRY" "+%s" 2>/dev/null || echo 0)"
  else
    EXPIRY_TS="$(date -j -f "%b %d %T %Y %Z" "$EXPIRY" "+%s" 2>/dev/null || echo 0)"
  fi

  NOW_TS="$(date "+%s")"
  DAYS_LEFT=$(( (EXPIRY_TS - NOW_TS) / 86400 ))
  CERT_NAME="$(basename "$CER_FILE")"

  if [[ "$DAYS_LEFT" -le 30 ]]; then
    error "$CERT_NAME still expires in ${DAYS_LEFT}d — renewal may have failed"
    FAILED=1
  else
    success "$CERT_NAME → ${DAYS_LEFT} days remaining"
  fi
done < <(find "$VERIFY_TMP/certs" -name "*.cer" -print0 2>/dev/null)

PROFILE_COUNT="$(find "$VERIFY_TMP/profiles" -name "*.mobileprovision" -o -name "*.provisionprofile" 2>/dev/null | wc -l | tr -d ' ')"
info "Profiles in repo: $PROFILE_COUNT"
rm -rf "$VERIFY_TMP"

if [[ "$FAILED" -ne 0 ]]; then
  error "Cert verification failed after renewal — check Apple Developer Portal for issues"
  exit 1
fi

echo ""
success "All certs renewed and verified — ios-provisioning-profile is fully stocked."
echo ""
echo "  Next steps:"
echo "    iOS build:   cd deployment && bundle exec fastlane ios beta"
echo "    macOS build: cd deployment && bundle exec fastlane mac desktop_testflight"
