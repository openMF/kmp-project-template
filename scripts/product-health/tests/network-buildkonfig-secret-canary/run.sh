#!/usr/bin/env bash
# Canary for NAP-15 (network-access-points.sh) — RED must FAIL on NAP-15, GREEN must PASS.
#
# RED is the pre-fix generation: app-profile declares `MY_CLIENT_KEY` with `secret_alias:` and
# `required_in_release: true`, but secrets/LAYOUT.yaml has no row for it, the buildkonfig field reads
# env / local.properties only, and nothing stops a release build compiling it as "". GREEN is what
# syncForkConfig now generates: the fork-secrets LAYOUT row, the readSecretValue() read, the check.
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
CHECK="$HERE/../../checks/network-access-points.sh"
rc=0
run_leg_out() {
  local leg="$1"
  HEALTH_ROOT="$HERE/$leg" \
  NAP_YAML="$HERE/$leg/app.yaml" \
  NAP_NET_DIR="$HERE/$leg/core/network/src/commonMain/kotlin/kpt/core/network" \
  NAP_BUILD_FILE="$HERE/$leg/core/network/build.gradle.kts" \
  NAP_LAYOUT="$HERE/$leg/secrets/LAYOUT.yaml" \
    bash "$CHECK" 2>&1
}
echo "── RED (expect FAIL on NAP-15) ──"
red_out="$(run_leg_out red)"
if echo "$red_out" | grep -q '❌ NAP-15'; then
  echo "  ✓ RED failed on NAP-15 ($(echo "$red_out" | grep -c '❌ NAP-15') finding(s))"
else
  echo "  ✗ RED did not fail on NAP-15 — reported: $(echo "$red_out" | grep -oE '❌ NAP-[0-9]+' | sort -u | tr '\n' ' ')"; rc=1
fi
echo "── GREEN (expect PASS) ──"
if green_out="$(run_leg_out green)"; then echo "  ✓ GREEN passed"; else echo "  ✗ GREEN failed:"; echo "$green_out" | sed 's/^/    /'; rc=1; fi
echo ""
[ $rc = 0 ] && echo "CANARY: ✅ PASS" || echo "CANARY: ❌ FAIL"
exit $rc
