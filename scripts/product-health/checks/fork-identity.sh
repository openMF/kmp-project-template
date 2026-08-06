#!/usr/bin/env bash
# checks/fork-identity.sh — a fork must not ship the TEMPLATE's (openMF / Mifos) signing + org identity.
#
# When you fork kmp-project-template it is easy to forget to re-fork the SIGNING + ORG identity in
# gradle/fork.properties, leaving apple.team.id / apple.match.git.url / org.name at the template's
# values. Then iOS/macOS signing uses the wrong Apple team + a Match repo that doesn't hold your
# certs, and Apple rejects the upload (90288 entitlements-mismatch). This check catches it before a
# broken release. exit 0 PASS / 1 FAIL. Self-skips (PASS) on the upstream template (TEMPLATE_SELF_BUILD=1).
set -uo pipefail
# shellcheck source=scripts/product-health/lib.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"

if [ "${TEMPLATE_SELF_BUILD:-}" = "1" ]; then
  echo "TEMPLATE_SELF_BUILD=1 — this IS the upstream template; identity N/A"; exit 0
fi
: "${FORK_PROPERTIES:?fork-identity: FORK_PROPERTIES not set (run via project-health.sh)}"
: "${HEALTH_ROOT:?fork-identity: HEALTH_ROOT not set (run via project-health.sh)}"

fail=0
# Signing/org fields that MUST be re-forked (regex = the template defaults / placeholders / empty).
declare -A TEMPLATE_DEFAULTS=(
  [apple.team.id]='L432S2FZP5|^$|CHANGEME'
  [apple.match.git.url]='openMF/ios-provisioning-profile|^$|CHANGEME'
  [org.name]='Mifos Initiative|Your Organization Name|^$|CHANGEME'
)
for key in "${!TEMPLATE_DEFAULTS[@]}"; do
  val="$(fp_get "$key")"
  if echo "$val" | grep -qE "${TEMPLATE_DEFAULTS[$key]}"; then
    echo "❌ '$key=$val' is still the TEMPLATE default — set your fork's value in gradle/fork.properties"
    fail=1
  fi
done

# The template bundle id must never survive into a fork's build config (its own SoT: libs.versions.toml).
if grep -rqE 'org\.mifos\.kmp\.template' "$HEALTH_ROOT/gradle/libs.versions.toml" 2>/dev/null; then
  echo "❌ appId is still 'org.mifos.kmp.template' in libs.versions.toml — set your fork's appId"
  fail=1
fi

[ "$fail" = 0 ] && { echo "gradle/fork.properties carries this fork's own signing + org identity"; exit 0; }
echo "→ Fix: edit gradle/fork.properties (apple.team.id, apple.match.git.url, org.name) + libs.versions.toml appId."
exit 1
