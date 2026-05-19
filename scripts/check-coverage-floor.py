#!/usr/bin/env python3
"""Enforce per-module line-coverage floors after `./gradlew koverXmlReport`.

Reads `.kover-floor.yml` (mapping module gradle-path → minimum line %), walks
every `*/build/reports/kover/report.xml` produced by the build, computes
LINE coverage per module, and fails (exit 1) if any module is below its floor.

Modules listed in `.kover-floor.yml` that produced NO report.xml also fail
(missing coverage data is treated as a regression — the floor file must stay
in sync with the module list).

Modules not listed in `.kover-floor.yml` are exempt.

Usage:
    ./gradlew koverXmlReport
    python3 scripts/check-coverage-floor.py
"""
from __future__ import annotations
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
FLOOR_FILE = REPO_ROOT / ".kover-floor.yml"


def parse_floor_file(path: Path) -> dict[str, float]:
    """Tiny YAML subset parser — only handles `floors:` map of `":x": N` entries.

    Avoids adding a PyYAML dependency just for this one file.
    """
    floors: dict[str, float] = {}
    in_floors = False
    pattern = re.compile(r'^\s+"([^"]+)":\s*([0-9.]+)\s*(#.*)?$')
    for line in path.read_text().splitlines():
        if line.strip() == "floors:":
            in_floors = True
            continue
        if in_floors and line and not line.startswith((" ", "\t")):
            in_floors = False
            continue
        if not in_floors:
            continue
        m = pattern.match(line)
        if m:
            floors[m.group(1)] = float(m.group(2))
    return floors


def module_path_for_report(report_xml: Path) -> str:
    """`./feature/crypto/build/reports/kover/report.xml` → `:feature:crypto`."""
    rel = report_xml.relative_to(REPO_ROOT)
    # Strip "/build/reports/kover/report.xml" tail
    module_parts = rel.parts[:-4]
    return ":" + ":".join(module_parts)


def line_coverage_pct(report_xml: Path) -> float | None:
    """Return LINE coverage %, or None if no LINE counter found."""
    try:
        root = ET.parse(report_xml).getroot()
    except ET.ParseError:
        return None
    for counter in root.findall("counter"):
        if counter.get("type") == "LINE":
            missed = int(counter.get("missed", 0))
            covered = int(counter.get("covered", 0))
            total = missed + covered
            if total == 0:
                return 0.0
            return covered / total * 100.0
    return None


def main() -> int:
    if not FLOOR_FILE.exists():
        print(f"❌ {FLOOR_FILE} not found", file=sys.stderr)
        return 1
    floors = parse_floor_file(FLOOR_FILE)
    if not floors:
        print(f"❌ {FLOOR_FILE} has no floors: entries", file=sys.stderr)
        return 1

    # Find all kover XML reports
    reports = sorted(REPO_ROOT.glob("*/build/reports/kover/report.xml")) + \
              sorted(REPO_ROOT.glob("*/*/build/reports/kover/report.xml"))

    observed: dict[str, float] = {}
    for r in reports:
        path = module_path_for_report(r)
        pct = line_coverage_pct(r)
        if pct is not None:
            observed[path] = pct

    failures: list[tuple[str, float, float]] = []
    missing_hard: list[tuple[str, float]] = []  # floor > 0 → real regression
    missing_soft: list[str] = []                 # floor == 0 → just a warning

    for module, floor in sorted(floors.items()):
        if module not in observed:
            if floor > 0:
                missing_hard.append((module, floor))
            else:
                missing_soft.append(module)
            continue
        actual = observed[module]
        if actual + 0.05 < floor:  # 0.05% slack to absorb FP rounding
            failures.append((module, actual, floor))

    # Report
    print("┌────────────────────────────────────────────────────────────┐")
    print("│ Per-module line coverage vs floor                          │")
    print("├──────────────────────────────────┬──────────┬──────────────┤")
    print("│ Module                           │ Coverage │ Floor / OK?  │")
    print("├──────────────────────────────────┼──────────┼──────────────┤")
    for module in sorted(floors):
        floor = floors[module]
        if module in observed:
            pct = observed[module]
            ok = "✅" if pct + 0.05 >= floor else "❌"
            print(f"│ {module:<32} │ {pct:7.1f}% │ {floor:5.1f}%   {ok}  │")
        else:
            mark = "❌" if floor > 0 else "⚠️ "
            print(f"│ {module:<32} │   MISSING│ {floor:5.1f}%   {mark}  │")
    print("└──────────────────────────────────┴──────────┴──────────────┘")

    if not failures and not missing_hard:
        if missing_soft:
            print(f"\n⚠️  {len(missing_soft)} module(s) at floor=0 produced no "
                  "report.xml (build skipped or failed upstream of tests). "
                  "Not failing because floor is 0; once a real floor is set, "
                  "missing data will fail the gate.")
        print("\n✅ All measurable modules meet their coverage floor.")
        return 0

    print()
    if missing_hard:
        print("❌ Coverage data lost for modules with non-zero floors:")
        for module, floor in missing_hard:
            print(f"     {module}: floor {floor:.1f}% but no report.xml was produced")
        print("   (build broke before tests fired, or kover-floor.yml is "
              "out of sync with module set)")
        print()
    if failures:
        print("❌ Coverage regressions:")
        for module, actual, floor in failures:
            delta = floor - actual
            print(f"     {module}: {actual:.1f}% < {floor:.1f}% floor (down {delta:.1f}pp)")
        print()
    print("Fix: write tests to restore coverage, OR justify the floor change in "
          "the PR description.")
    return 1


if __name__ == "__main__":
    sys.exit(main())
