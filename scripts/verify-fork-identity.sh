#!/usr/bin/env bash
# verify-fork-identity.sh — blocks a FORK from shipping the template's (openMF / Mifos) identity.
#
# When you fork kmp-project-template you build your app, but it is easy to forget to re-fork the
# SIGNING + ORG identity in gradle/fork.properties — leaving apple.team.id / apple.match.git.url /
# org.name at the template's defaults. Then iOS/macOS signing uses the wrong team + a Match repo that
# doesn't hold your certs, and Apple rejects the upload (90288 entitlements-mismatch). This gate catches
# that at YOUR ci, before a broken release. Pure bash + grep. Exit 0 PASS / 1 FAIL.
#
# Skip legitimately ONLY on the upstream template itself: export TEMPLATE_SELF_BUILD=1 (the template's
# own CI), since the template genuinely IS the Mifos identity.
set -uo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FP="$ROOT/gradle/fork.properties"
[ -f "$FP" ] || FP="$ROOT/gradle/fork.properties.template"
[ -f "$FP" ] || { echo "❌ fork-identity: no gradle/fork.properties(.template) found"; exit 1; }

if [ "${TEMPLATE_SELF_BUILD:-}" = "1" ]; then
  echo "ℹ fork-identity: TEMPLATE_SELF_BUILD=1 — skipping (this IS the upstream template)"; exit 0
fi

fail=0
_get() { grep -E "^$1=" "$FP" 2>/dev/null | head -1 | cut -d= -f2- | sed 's/[[:space:]]*#.*$//; s/[[:space:]]*$//'; }

# Signing/org identity fields that MUST be re-forked (not the template's placeholders/defaults).
declare -A TEMPLATE_DEFAULTS=(
  [apple.team.id]='L432S2FZP5|^$|CHANGEME'
  [apple.match.git.url]='openMF/ios-provisioning-profile|^$|CHANGEME'
  [org.name]='Mifos Initiative|Your Organization Name|^$|CHANGEME'
)
for key in "${!TEMPLATE_DEFAULTS[@]}"; do
  val="$(_get "$key")"
  if echo "$val" | grep -qE "${TEMPLATE_DEFAULTS[$key]}"; then
    echo "❌ fork-identity: '$key=$val' is still the TEMPLATE default — set your fork's value in gradle/fork.properties" >&2
    fail=1
  fi
done

# The template bundle id must never survive into a fork's build config.
if grep -rqE 'org\.mifos\.kmp\.template' "$ROOT/gradle/libs.versions.toml" 2>/dev/null; then
  echo "❌ fork-identity: appId is still 'org.mifos.kmp.template' in libs.versions.toml — set your fork's appId" >&2
  fail=1
fi

[ "$fail" = 0 ] && { echo "✅ fork-identity: gradle/fork.properties carries this fork's own signing + org identity (not the template's)"; exit 0; }
echo "" >&2
echo "→ Fix: edit gradle/fork.properties — apple.team.id (your Apple team), apple.match.git.url (your" >&2
echo "  fastlane-match repo), org.name (your org). See gradle/fork.properties.template + docs/FORK_QUICKSTART.md." >&2
exit 1
