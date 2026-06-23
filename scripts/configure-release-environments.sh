#!/usr/bin/env bash
# scripts/configure-release-environments.sh
#
# Creates + protects the GitHub Environments the multi-platform release pipeline
# binds to, so that store/distribution stages PAUSE for a manual "Approve and
# deploy" before publishing. Run this once per repo that DISPATCHES releases.
#
# WHY THIS EXISTS
#   The publish-* reusable workflows declare `environment: <name>` on every
#   store stage (e.g. publish-android-kmp/release.yaml → `android-play-internal`).
#   GitHub resolves that environment against the REPO WHERE THE RUN EXECUTES.
#   If the environment is absent OR has no required-reviewer rule there, GitHub
#   runs the job immediately — NO approval prompt. (Observed empirically: a run
#   on openMF/kmp-project-template went straight to the Play upload and failed,
#   `approvals: []`, because openMF's `android-play-internal` had 0 reviewers.)
#   Configuring required reviewers here is what turns the stage into a gate.
#
# Prerequisites:
#   - gh CLI installed and authenticated (gh auth login)
#   - You MUST have ADMIN on the target repo (environment protection rules are
#     an admin-only API — write/maintain is NOT enough; you'll get HTTP 403
#     "Must have admin rights to Repository.").
#
# Usage:
#   bash scripts/configure-release-environments.sh --repo owner/repo --reviewer <login-or-id>
#   bash scripts/configure-release-environments.sh --repo openMF/kmp-project-template --reviewer therajanmaurya
#   bash scripts/configure-release-environments.sh --repo owner/repo --reviewer 5724482 --dry-run
#   bash scripts/configure-release-environments.sh --repo owner/repo --reviewer me --only android,ios
#   bash scripts/configure-release-environments.sh --repo owner/repo --reviewer me --desktop-target windows-msi-signed --web-host cloudflare-pages
#
# Flags:
#   --repo owner/repo        Target repo (REQUIRED). Where releases are dispatched.
#   --reviewer <id|login>    Required reviewer added to every gated environment
#                            (REQUIRED). Accepts a numeric user id, a @login, or
#                            the literal `me` (resolves to the authenticated user).
#                            The reviewer needs >=write access on the target repo.
#   --only <list>            Comma-separated platform subset to gate:
#                            android,ios,mac,desktop,web (default: all).
#   --desktop-target <slug>  Desktop artifact slug used in env names
#                            `desktop-<slug>-{prerelease,beta,stable}`
#                            (default: linux-deb). Repeatable via comma list.
#   --web-host <slug>        Web host slug used in env names
#                            `web-<slug>-{preview,staging,production}`
#                            (default: gh-pages). Repeatable via comma list.
#   --wait-prod <minutes>    Optional wait timer (minutes) on the top rung
#                            (production / app-store / stable) (default: 0).
#   --dry-run                Print what would change; make no API calls.
#
# Firebase note: `android-firebase` / `ios-firebase` (App Distribution test
# channels) are intentionally NOT gated by default — they are pre-store test
# distribution, not a store release. Add them by hand if your team wants them
# gated too.
#
# Idempotent: re-running re-applies the same reviewer set (PUT replaces config).

set -euo pipefail

REPO=""
REVIEWER=""
ONLY="android,ios,mac,desktop,web"
DESKTOP_TARGETS="linux-deb"
WEB_HOSTS="gh-pages"
WAIT_PROD=0
DRY_RUN=false

# ── Parse args ────────────────────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo)           REPO="$2"; shift ;;
    --reviewer)       REVIEWER="$2"; shift ;;
    --only)           ONLY="$2"; shift ;;
    --desktop-target) DESKTOP_TARGETS="$2"; shift ;;
    --web-host)       WEB_HOSTS="$2"; shift ;;
    --wait-prod)      WAIT_PROD="$2"; shift ;;
    --dry-run)        DRY_RUN=true ;;
    -h|--help)        sed -n '2,60p' "$0"; exit 0 ;;
    *) echo "❌ Unknown arg: $1" >&2; exit 2 ;;
  esac
  shift
done

[[ -z "$REPO" ]]     && { echo "❌ --repo owner/repo is required" >&2; exit 2; }
[[ -z "$REVIEWER" ]] && { echo "❌ --reviewer <id|login|me> is required" >&2; exit 2; }

command -v gh >/dev/null || { echo "❌ gh CLI not found — install + 'gh auth login'" >&2; exit 3; }

# ── Resolve reviewer → numeric user id ────────────────────────────────────────
resolve_reviewer_id () {
  local r="$1"
  if [[ "$r" == "me" ]]; then
    gh api user -q '.id'
  elif [[ "$r" =~ ^[0-9]+$ ]]; then
    echo "$r"
  else
    gh api "users/${r#@}" -q '.id'
  fi
}
REVIEWER_ID="$(resolve_reviewer_id "$REVIEWER")"
[[ -z "$REVIEWER_ID" ]] && { echo "❌ Could not resolve reviewer '$REVIEWER' to a user id" >&2; exit 4; }

# ── Admin pre-flight (fail fast with a clear message) ─────────────────────────
PERM="$(gh api "repos/$REPO" -q '.permissions.admin' 2>/dev/null || echo "unknown")"
if [[ "$PERM" != "true" ]]; then
  echo "⚠️  You do not appear to have ADMIN on $REPO (admin=$PERM)." >&2
  echo "    Environment protection rules are an admin-only API. Expect HTTP 403." >&2
  echo "    Ask a repo/org owner to run this script, or grant admin first." >&2
  $DRY_RUN || { echo "    (continuing anyway — the first PUT will surface the 403)"; }
fi

# ── Build the gate list per --only ────────────────────────────────────────────
declare -a ENVS=()
has () { [[ ",$ONLY," == *",$1,"* ]]; }

if has android; then
  ENVS+=(android-play-internal android-play-closed android-play-beta android-play-production)
fi
if has ios; then
  ENVS+=(ios-testflight-internal ios-testflight-external ios-app-store)
fi
if has mac; then
  ENVS+=(mac-testflight-internal mac-testflight-external mac-app-store)
fi
if has desktop; then
  IFS=',' read -ra DT <<< "$DESKTOP_TARGETS"
  for t in "${DT[@]}"; do
    ENVS+=("desktop-${t}-prerelease" "desktop-${t}-beta" "desktop-${t}-stable")
  done
fi
if has web; then
  IFS=',' read -ra WH <<< "$WEB_HOSTS"
  for h in "${WH[@]}"; do
    ENVS+=("web-${h}-preview" "web-${h}-staging" "web-${h}-production")
  done
fi

[[ ${#ENVS[@]} -eq 0 ]] && { echo "❌ --only '$ONLY' selected no platforms" >&2; exit 2; }

# Top-rung envs that get the optional wait timer.
is_top_rung () {
  case "$1" in
    *-production|*-app-store|*-stable) return 0 ;;
    *) return 1 ;;
  esac
}

echo "════════════════════════════════════════════════════════════════"
echo " Configure release approval gates"
echo "   repo:      $REPO"
echo "   reviewer:  $REVIEWER (id=$REVIEWER_ID)"
echo "   platforms: $ONLY"
echo "   gates:     ${#ENVS[@]} environments"
$DRY_RUN && echo "   MODE:      DRY-RUN (no changes)"
echo "════════════════════════════════════════════════════════════════"

fail=0
for env in "${ENVS[@]}"; do
  wait=0
  is_top_rung "$env" && wait="$WAIT_PROD"
  body="$(printf '{"wait_timer":%s,"reviewers":[{"type":"User","id":%s}],"deployment_branch_policy":null}' "$wait" "$REVIEWER_ID")"
  if $DRY_RUN; then
    printf "  [dry-run] PUT %-32s reviewers=1 wait=%smin\n" "$env" "$wait"
    continue
  fi
  if revs=$(printf '%s' "$body" | gh api -X PUT "repos/$REPO/environments/$env" --input - 2>/dev/null \
        | python3 -c "import sys,json; d=json.load(sys.stdin); print(sum(len(r.get('reviewers',[])) for r in d.get('protection_rules',[]) if r.get('type')=='required_reviewers'))" 2>/dev/null); then
    printf "  ✅ %-32s reviewers=%s wait=%smin\n" "$env" "${revs:-?}" "$wait"
  else
    printf "  ❌ %-32s FAILED (admin required? reviewer lacks write?)\n" "$env"
    fail=1
  fi
done

echo "════════════════════════════════════════════════════════════════"
if [[ $fail -ne 0 ]]; then
  echo "⚠️  Some environments failed. Most common cause: you lack ADMIN on $REPO,"
  echo "    or the reviewer (id=$REVIEWER_ID) does not have >=write access there."
  exit 5
fi
$DRY_RUN || echo "✅ Done. Dispatched releases on $REPO now pause at each store stage for approval."
