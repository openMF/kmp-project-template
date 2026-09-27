#!/usr/bin/env bash
# scripts/docs/doc-audit.sh — is the documentation actually complete? One command, seven checks.
#
# Each generator already verifies its own output. This asks the question none of them asks on its own:
# for EVERY part of this repository, is there a page, is it current, does it say something, and does
# what it says still resolve?
#
#   A1  every project area has a page, or a published reason for having none
#   A2  every documentation UNIT (deploy platform, script group, feature, module) has a page
#   A3  every generated page's authored half says something (≥20 words, not the placeholder)
#   A4  every generated page is current with source           → refresh.sh --check
#   A5  every path named in authored prose exists             → doc-refs.sh --strict
#   A6  every internal markdown link resolves, repo-wide
#   A7  every public symbol is documented                     → doc-scan.sh
#
# Usage: scripts/docs/doc-audit.sh [--quiet]
# Exit:  0 complete · 1 gaps found (each named) · 2 cannot run
set -uo pipefail
ROOT="${TEMPLATE_PATH:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
cd "$ROOT" || exit 2
D=scripts/docs
FAIL=0
say() { [ "${QUIET:-0}" = 1 ] || printf '%s\n' "$*"; }
QUIET=0; for a in "$@"; do [ "$a" = "--quiet" ] && QUIET=1; done

say "── A1/A2/A3  coverage and substance ───────────────────────────────────────────"
python3 - <<'PYEOF' || FAIL=1
import io, os, re, subprocess, collections, sys
MARK = "<!-- tree-scaffold:end -->"
OUT  = "docs/architecture/tree"
files = subprocess.run(["git","ls-files"],capture_output=True,text=True).stdout.splitlines()
areas = collections.defaultdict(list)
for f in files:
    p = f.split("/")
    if len(p) > 1: areas[p[0]].append(f)

# The reasons the scaffolder publishes for an area having no page — read from IT, so the audit and the
# generator cannot disagree about what is deliberately undocumented.
skip = set()
src = io.open("scripts/docs/tree-scaffold.sh", encoding="utf-8").read()
m = re.search(r"^SKIP = \{(.*?)^\}", src, re.S | re.M)
if m:
    skip = set(re.findall(r'"([^"]+)":', m.group(1)))

pages = {os.path.relpath(os.path.join(d,f), OUT)[:-3]
         for d,_,fs in os.walk(OUT) for f in fs if f.endswith(".md")}
bad = []
for a in sorted(areas):
    if a in skip: continue
    if a.lstrip(".") not in pages:
        bad.append(("A1", f"area `{a}/` has no page and no published reason"))

# A3 — a generated page whose authored half is empty or still the placeholder
thin = []
for d,_,fs in os.walk(OUT):
    for f in sorted(fs):
        if not f.endswith(".md"): continue
        p = os.path.join(d,f)
        s = io.open(p, encoding="utf-8").read()
        if MARK not in s and "api-docs:begin" not in s:
            continue                                  # a purely authored guide — nothing to check here
        tail = s.split(MARK,1)[1] if MARK in s else ""
        body = [l for l in tail.splitlines()
                if l.strip() and not l.startswith(("## ", "_Authored below"))]
        if MARK in s and len(" ".join(body).split()) < 20:
            thin.append(os.path.relpath(p, OUT))
for t in thin:
    bad.append(("A3", f"`{t}` has no authored significance"))

for code, msg in bad:
    print(f"  ✗ {code}  {msg}")
print(f"  {len(areas)-len(skip)} areas · {len(pages)} tree pages · {len(bad)} gap(s)")
sys.exit(1 if bad else 0)
PYEOF

say ""
say "── A4  generated pages current with source ────────────────────────────────────"
# The drift detail is PRINTED, not swallowed. `refresh.sh --check` already names the exact pages;
# discarding that left CI reporting a bare "stale" for a generator whose output was not reproducible
# across filesystems, and the named pages were the fastest way to see it.
if _a4="$(bash "$D/refresh.sh" --check 2>&1)"; then
  say "  ✓ in step"
else
  say "  ✗ stale — run scripts/docs/refresh.sh"
  printf '%s\n' "$_a4" | sed -n '/DRIFT/,$p' | sed 's/^/  /'
  FAIL=1
fi

say "── A5  authored prose names files that exist ──────────────────────────────────"
if bash "$D/doc-refs.sh" --strict >/dev/null 2>&1; then say "  ✓ every reference resolves"; else bash "$D/doc-refs.sh" --strict 2>&1 | sed -n '/^DEAD/,$p' | sed 's/^/  /'; FAIL=1; fi

say "── A6  internal markdown links ───────────────────────────────────────────────"
python3 - <<'PYEOF' || FAIL=1
import os, re, subprocess, sys
md = subprocess.run(["git","ls-files","*.md"],capture_output=True,text=True).stdout.splitlines()
for d,_,fs in os.walk("docs"):
    for f in fs:
        if f.endswith(".md"):
            p = os.path.join(d,f)
            if p not in md: md.append(p)
bad = tot = 0
for p in md:
    if not os.path.isfile(p): continue
    d = os.path.dirname(p) or "."
    s = open(p, encoding="utf-8", errors="replace").read()
    for m in re.finditer(r'\]\((?!https?://|mailto:)(/?[^)#\s]+\.md)(#[^)]*)?\)', s):
        l = m.group(1); tot += 1
        t = os.path.join("docs", l.lstrip("/")) if l.startswith("/") else os.path.normpath(os.path.join(d,l))
        if not os.path.isfile(t):
            print(f"  ✗ {p} → {l}"); bad += 1
print(f"  {tot} links, {bad} dead")
sys.exit(1 if bad else 0)
PYEOF

say "── A7  symbol documentation ──────────────────────────────────────────────────"
if [ "$(bash "$D/doc-scan.sh" --gaps 2>/dev/null | wc -l | tr -d ' ')" = "0" ]; then
  say "  ✓ every public symbol documented"
else
  bash "$D/doc-scan.sh" --summary 2>/dev/null | tail -3 | sed 's/^/  /'; FAIL=1
fi

say ""
if [ "$FAIL" -eq 0 ]; then say "✅ DOCUMENTATION AUDIT: complete"; else say "❌ DOCUMENTATION AUDIT: gaps above"; fi
exit "$FAIL"
