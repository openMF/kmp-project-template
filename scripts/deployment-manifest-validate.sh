#!/usr/bin/env bash
#
# deployment-manifest-validate.sh — validate the deploy-target ownership split (E1 / D-1).
#
# After the white-label separation (epic pure-white-label-store5-network), deploy-target state
# is split across TWO files with distinct owners:
#
#   • deployment/DEPLOYMENT_MANIFEST.yaml   — TEMPLATE CATALOG: which targets EXIST (owner: template).
#   • app-profile/deploy-targets.yaml       — FORK STATE: which of those a fork ENABLES + at what
#                                             tier / confirm gate (owner: fork).
#
# This validator asserts the two are coherent:
#   V1  every canonical_name in app-profile/deploy-targets.yaml exists in the template catalog
#       (a fork can't enable a target the template doesn't ship).
#   V2  every enabled fork target declares a tier ∈ {1,2}.
#   V3  the promotion log lives at deploy-state/PROMOTION_LOG.yaml (relocated out of deployment/).
#
# Pure bash + grep — no Gradle, no network, no YAML lib (line-oriented reads only).
# Usage: bash scripts/deployment-manifest-validate.sh   # exit 0 clean, exit 1 on any violation
set -uo pipefail

# repo root = parent of scripts/
cd "$(dirname "$0")/.."

CATALOG="deployment/DEPLOYMENT_MANIFEST.yaml"
TARGETS="app-profile/deploy-targets.yaml"
LOG="deploy-state/PROMOTION_LOG.yaml"

violations=0
viol() { echo "VIOLATION [$1]: $2" >&2; violations=$((violations + 1)); }

[ -f "$CATALOG" ] || { echo "FATAL: missing template catalog $CATALOG" >&2; exit 1; }
[ -f "$TARGETS" ] || { echo "FATAL: missing fork deploy-targets $TARGETS (E1/D-1 relocation)" >&2; exit 1; }

# Catalog target names (from the template DEPLOYMENT_MANIFEST canonical_name rows).
catalog_names="$(grep -oE 'canonical_name:[[:space:]]*[a-z0-9-]+' "$CATALOG" | sed -E 's/.*:[[:space:]]*//' | sort -u)"

# ── V1 + V2: walk each fork target block ──────────────────────────────────────────────
current=""
while IFS= read -r line; do
  case "$line" in
    *canonical_name:*)
      current="$(printf '%s\n' "$line" | sed -E 's/.*canonical_name:[[:space:]]*//; s/[[:space:]].*//')"
      if ! printf '%s\n' "$catalog_names" | grep -qx "$current"; then
        viol V1-unknown-target "app-profile/deploy-targets.yaml declares '$current' which is not in the template catalog ($CATALOG)"
      fi
      ;;
    *tier:*)
      [ -z "$current" ] && continue
      tier="$(printf '%s\n' "$line" | sed -E 's/.*tier:[[:space:]]*//; s/[[:space:]].*//')"
      case "$tier" in
        1|2) : ;;
        *) viol V2-bad-tier "target '$current' has tier '$tier' (must be 1 or 2)" ;;
      esac
      ;;
  esac
done < "$TARGETS"

# ── V3: promotion log relocated out of deployment/ ────────────────────────────────────
if [ ! -f "$LOG" ]; then
  viol V3-log-location "promotion log not found at $LOG (E1/D-2 — must live outside deployment/)"
fi
if [ -f "deployment/PROMOTION_LOG.yaml" ]; then
  viol V3-log-location "stale deployment/PROMOTION_LOG.yaml present — the log relocated to $LOG (E1/D-2)"
fi

if [ "$violations" -eq 0 ]; then
  echo "✅ deployment-manifest-validate: clean (V1 catalog-membership · V2 tier · V3 log-relocation)"
  exit 0
else
  echo "❌ deployment-manifest-validate: $violations violation(s) — see above" >&2
  exit 1
fi
