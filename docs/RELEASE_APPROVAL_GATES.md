# Release Approval Gates — Admin Setup Guide

> **Audience:** a repo **admin** / org **owner** of the repo where releases are dispatched
> (e.g. `openMF/kmp-project-template`).
> **Script:** [`scripts/configure-release-environments.sh`](../scripts/configure-release-environments.sh)
> **Time:** ~2 minutes.

---

## TL;DR

```bash
# Recommended: gate only the public/production-facing stages.
bash scripts/configure-release-environments.sh \
  --repo openMF/kmp-project-template \
  --reviewer <your-github-login> \
  --production-only \
  --wait-prod 30
```

That's it. After this, any release dispatched on the repo **pauses for a manual
"Approve and deploy"** before it publishes to a public store track (Play beta/production,
TestFlight external, App Store, etc.). Internal/Firebase test builds stay un-gated.

---

## Is this required? (No.)

**The release pipeline works without these gates.** GitHub Actions runs every release
stage fine whether or not an environment has a required reviewer — an un-gated stage just
runs immediately and publishes. These gates are a **safety control**, not a functional
dependency.

| | Without gates | With gates |
|---|---|---|
| Builds + signing | ✅ works | ✅ works |
| Upload to a store | publishes **immediately** | **pauses** for human approval, then publishes |

Set them up **only** if you want a human checkpoint before a build reaches a public store
track. Most teams want this on **production-facing** stages and not on internal testing.

---

## Why it has to be an admin

GitHub gates **environment protection rules** (required reviewers, wait timers) behind
**Admin** permission on the repo. The `Maintain` role and org `member` status are **not**
enough — the API returns `403 "Must have admin rights to Repository."` So this one-time
setup must be run by a repo admin or org owner. (Everything else about releases — secrets,
workflows, signing — does **not** need admin.)

### How the gate actually works
The publish workflows declare `environment: <name>` on each store stage (e.g.
`android-play-production`). GitHub resolves that against **the repo where the run executes**.
If the environment there has a **required reviewer**, the job waits for approval before it
runs. If it has none (or doesn't exist), the job runs immediately. This script adds the
required-reviewer rule to each store environment.

---

## Prerequisites

- `gh` CLI installed and authenticated as an **admin** of the target repo: `gh auth login`
- Decide **who the reviewer(s)** should be — they need ≥ `write` access on the repo.

---

## Run it

### Recommended — gate production-facing stages only

```bash
bash scripts/configure-release-environments.sh \
  --repo openMF/kmp-project-template \
  --reviewer <login-or-@me-or-numeric-id> \
  --production-only \
  --wait-prod 30        # optional: 30-min cool-off on the top rung before deploy
```

Gates **11** environments (skips the internal/firebase/preview/prerelease test channels):

| Platform | Gated stages |
|---|---|
| Android | `android-play-closed`, `android-play-beta`, `android-play-production` |
| iOS | `ios-testflight-external`, `ios-app-store` |
| macOS | `mac-testflight-external`, `mac-app-store` |
| Desktop | `desktop-linux-deb-beta`, `desktop-linux-deb-stable` |
| Web | `web-gh-pages-staging`, `web-gh-pages-production` |

### Alternative — gate every stage (incl. internal)

Drop `--production-only` to also gate `*-internal`, `*-firebase`, `*-preview`,
`*-prerelease`. This makes **every** test build require an approval click too.

### Scope to specific platforms / artifacts

```bash
# only Android + iOS:
... --only android,ios --production-only

# different desktop artifact / web host (env names follow the slug):
... --desktop-target windows-msi-signed --web-host cloudflare-pages
```

### Preview first (no changes)

Add `--dry-run` to print exactly which environments would be configured without touching
anything.

---

## All flags

| Flag | Meaning |
|---|---|
| `--repo owner/repo` | **(required)** repo where releases are dispatched |
| `--reviewer <id\|@login\|me>` | **(required)** required reviewer; `me` = the authed user |
| `--production-only` | gate only external/production stages (recommended) |
| `--only <list>` | platform subset: `android,ios,mac,desktop,web` (default all) |
| `--desktop-target <slug>` | desktop env slug, default `linux-deb` (comma-list ok) |
| `--web-host <slug>` | web env slug, default `gh-pages` (comma-list ok) |
| `--wait-prod <minutes>` | wait timer on the top rung (production/app-store/stable) |
| `--dry-run` | preview only |

The script is **idempotent** — re-run it any time to change reviewers or scope.

---

## Verify it took

```bash
gh api repos/openMF/kmp-project-template/environments/android-play-production \
  -q '[.protection_rules[] | select(.type=="required_reviewers") | .reviewers[]] | length'
# → prints 1 (or however many reviewers you set)
```

Or just dispatch a release: a gated stage shows a yellow
**"<env> requires an approval to start deploying changes"** banner with a **Review** button,
and the run status sits at **Waiting**.

---

## Add more reviewers / change later

Re-run with a different `--reviewer`, or add reviewers in the GitHub UI:
**Repo → Settings → Environments → `<env>` → Required reviewers**. Production/stable
environments ideally have **2** reviewers — the script sets the one you pass; add a second
in the UI.

## Remove a gate (un-gate)

UI: **Settings → Environments → `<env>`** → uncheck *Required reviewers*. Or delete the
environment (it'll be auto-recreated un-gated on the next run that references it).

---

## FAQ

**Q: Do I need this for the GitHub Actions release to work?**
No. It's purely a human-approval safety layer. Skip it and releases run end-to-end.

**Q: I'm a maintainer, not an admin — can I run it?**
No. Environment protection requires Admin. Ask an org owner to either run the script or
grant you Admin on the repo.

**Q: Will gating slow down testing?**
Only if you gate the internal/firebase stages. `--production-only` leaves those ungated, so
day-to-day test builds are unaffected.

**Q: Does this change versioning or the promotion ladder?**
No. It only adds an approval pause before a stage deploys. Version computation and
internal→beta→production promotion are untouched.
