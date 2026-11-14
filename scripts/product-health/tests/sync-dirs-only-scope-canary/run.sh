#!/usr/bin/env bash
# sync-dirs-only-scope-canary — a `sync-dirs.sh --only <items>` run touches only <items>.
#
# mbs/reels-downloader, 2026-11-14: `--only deployment` ran post_sync_fork_steps over the WHOLE fork. The demo
# strip used the fork's own (pre-lineage-guard) scripts/remove-demo.sh and deleted 153 files — including the
# entities and migration spec of tables the live v15 database still held — and the run stamped
# .template-version to the new ref although only deployment/ had moved.
#
#   GREEN: the real engine's post_sync_fork_steps skips the strip under --only and runs it on a full sync;
#          the .template-version stamp is guarded by ONLY_ITEMS.
#   RED:   the pre-fix function (red/) strips under --only — the canary must catch it.
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO="$(cd "$HERE/../../../.." && pwd)"
ENGINE="$REPO/scripts/white-label/sync-dirs.sh"

# strips <function-file> <only-items> → prints "stripped" or "kept"
strips() {
  local fn="$1" only="$2" dir; dir="$(mktemp -d)"
  mkdir -p "$dir/app-profile" "$dir/scripts"
  echo "  - { id: alerts, status: removed }" > "$dir/app-profile/package-ledger.yaml"
  printf '#!/usr/bin/env bash\ntouch STRIPPED\n' > "$dir/scripts/remove-demo.sh"
  (
    cd "$dir" || exit 1
    print_step() { :; }; print_warning() { :; }; handle_error() { echo "ERR $*" >&2; exit 1; }
    ONLY_ITEMS="$only"
    # shellcheck source=/dev/null
    . <(awk '/^post_sync_fork_steps\(\) \{/{b=1} b{print} b&&/^}/{exit}' "$fn")
    post_sync_fork_steps >/dev/null 2>&1
    [ -f STRIPPED ] && echo stripped || echo kept
  )
  rm -rf "$dir"
}

rc=0
check() { if [ "$2" = "$3" ]; then echo "   ✅ $1"; else echo "   ❌ $1 (got $2, want $3)"; rc=1; fi; }

echo "── GREEN (real engine) ──"
check "--only deployment: strip skipped" "$(strips "$ENGINE" deployment)" kept
check "full sync: strip runs"            "$(strips "$ENGINE" "")" stripped
if awk '/Recording template sync version/{exit} {print}' "$ENGINE" | tail -8 | grep -q 'if \[ -n "\$ONLY_ITEMS" \]'; then
  echo "   ✅ .template-version stamp guarded by ONLY_ITEMS"
else echo "   ❌ .template-version stamp not guarded by ONLY_ITEMS"; rc=1; fi

echo "── RED (pre-fix function) ──"
if [ "$(strips "$HERE/red/post-sync-fork-steps.before.sh" deployment)" = stripped ]; then
  echo "   ✅ RED: the old function strips under --only (detected)"
else echo "   ❌ RED: canary blind to the old defect"; rc=1; fi

echo ""
[ "$rc" -eq 0 ] && echo "canary: PASS" || echo "canary: FAIL"
exit "$rc"
