#!/usr/bin/env bash
# Phase-7 secrets-resolver guard. Enforces that the build-secrets resolver stays the
# single source of truth for the secrets layout. Run in pre-push / CI.
#
#   SR-7  no resurrected drift literals (the bugs Phase 7 fixed) + workflow uses the resolver
#   SR-8  source_env contract — every LAYOUT source_env is an env var the workflow exposes
#   SR-9  sample completeness — every LAYOUT file/value/literal key has a committed sample file
#   SR-10 no real secrets committed under secrets/sample (placeholder-marked files exempt)
set -uo pipefail
cd "$(git rev-parse --show-toplevel 2>/dev/null || echo .)"
fail=0
LAYOUT=secrets/LAYOUT.yaml
WF=.github/workflows/release-multi-platform.yml

# ── SR-7: the drifts Phase 7 fixed must not return; the workflow materializes via resolver ──
# (CODE only — exclude comment lines so the "DRIFT FIX:" doc comment doesn't self-trip.)
if grep -rnE 'SECRETS_DIR, *"play"|"secrets/play/service-account|firebaseAppDistributionServiceCredentialsFile' \
     deployment --include='*.rb' 2>/dev/null | grep -v build_secrets.rb | grep -vE ':[0-9]+:[[:space:]]*#'; then
  echo "❌ SR-7: a fixed drift literal returned (secrets/play or the 2nd firebase filename)"; fail=1
fi
if grep -nE 'base64 -d > secrets/|base64 -d > cmp-android|printf .* > secrets/apple' "$WF" 2>/dev/null; then
  echo "❌ SR-7: workflow has an inline secret-materialization (use build-secrets materialize)"; fail=1
fi

# ── SR-8: contract — every `--from-env VAR` the workflow materializes maps to a LAYOUT
#     source_env (catches a typo'd/renamed env var in a materialize call). Forwarded-to-
#     publish secrets (azure/vercel/ms/mac) are NOT consumer-materialized, so not checked. ──
LAYOUT_ENVS=$(ruby -ryaml -e 'y=YAML.load_file(ARGV[0]); puts y["secrets"].flat_map{|k,s| (s["by_flavor"]||{"_"=>s}).values.map{|v|v["source_env"]}}.compact.uniq' "$LAYOUT")
for e in $(grep -oE '\-\-from-env [A-Z0-9_]+' "$WF" 2>/dev/null | awk '{print $2}' | sort -u); do
  echo "$LAYOUT_ENVS" | grep -qx "$e" || { echo "❌ SR-8: workflow materializes --from-env $e but no LAYOUT secret declares it"; fail=1; }
done

# ── SR-9: sample completeness (by_flavor expanded; env_var + consume_at exempt) ──
ruby -ryaml -e '
  y=YAML.load_file("'"$LAYOUT"'"); root=y["roots"]["sample"]; miss=[]
  y["secrets"].each{|k,s| next if s["kind"]=="env_var" || s["consume_at"]
    rels = s["by_flavor"] ? s["by_flavor"].values.map{|v|v["rel"]} : [s["rel"]]
    rels.compact.each{|r| p=File.join(root,r); miss<<p unless File.exist?(p)} }
  unless miss.empty?; warn "❌ SR-9: missing sample files:\n  #{miss.join("\n  ")}"; exit 1; end
' || fail=1

# ── SR-10: no real secrets in sample/ (placeholder-marked files are exempt) ──
for f in $(grep -rlE 'sk_live_|sk_test_|-----BEGIN .*PRIVATE KEY-----' secrets/sample 2>/dev/null); do
  grep -q "CLAUDE-PLACEHOLDER" "$f" || { echo "❌ SR-10: real-looking secret (no placeholder marker): $f"; fail=1; }
done

[ "$fail" = 0 ] && echo "✅ secrets-resolver guards pass (SR-7/8/9/10)"
exit $fail
