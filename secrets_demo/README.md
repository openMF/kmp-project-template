# secrets_demo/ — OSS-safe schema-as-code

This tree is the **schema-as-code** mirror of `secrets/`. Every credential file
here is a PLACEHOLDER carrying a machine-detectable magic marker:

- **Text files:** first line is `# CLAUDE-PLACEHOLDER — do not commit this to secrets/`
- **JSON files:** `"_comment": "CLAUDE-PLACEHOLDER…"` key present

`secrets/` is gitignored. `secrets_demo/` is committed and shows exactly which
files to populate and where to get them.

## Quick start — fork setup

```bash
# 1. Copy placeholder structure
cp -r secrets_demo/* secrets/   # creates the directory structure
# WARNING: this copies placeholder files, not real secrets — you must fill them in

# 2. Fill in real values for the platforms you want to deploy to
#    Each subdirectory has a README.md explaining exactly how to get the credentials

# 3. Sync to GitHub Actions
bash scripts/sync-secrets-to-github.sh --dry-run   # preview what will be set
bash scripts/sync-secrets-to-github.sh              # push to your fork's GHA secrets

# 4. Trigger a workflow
gh workflow run .github/workflows/multi-platform-build-and-publish.yml
```

## Layout

### iOS / App Store

| secrets_demo/ path | secrets/ target | Credential | Per-secret guide |
|---|---|---|---|
| `appstore/AuthKey.p8` | `secrets/appstore/AuthKey.p8` | App Store Connect API key (.p8) | [appstore/README.md](appstore/README.md) |
| `appstore/key_id` | `secrets/appstore/key_id` | ASC Key ID (10 chars) | [appstore/README.md](appstore/README.md) |
| `appstore/issuer_id` | `secrets/appstore/issuer_id` | ASC Issuer ID (UUID) | [appstore/README.md](appstore/README.md) |
| `match/match_ci_key` | `secrets/match/match_ci_key` | SSH private key for Match repo | [match/README.md](match/README.md) |
| `match/match_ci_key.pub` | `secrets/match/match_ci_key.pub` | SSH public key (add to cert repo) | [match/README.md](match/README.md) |
| `match/.match_password` | `secrets/match/.match_password` | Match encryption password | [match/README.md](match/README.md) |
| `shared_keys.env.template` | `secrets/shared_keys.env` | iOS env vars (team ID, contacts) | Inline docs in template |
| `APNAuthKey.p8` | `secrets/apn/APNAuthKey.p8` | APN push key (optional) | [Apple Developer](https://developer.apple.com/account/resources/authkeys/list) |

### Android / Play Store

| secrets_demo/ path | secrets/ target | Credential | Per-secret guide |
|---|---|---|---|
| `keystores/release.properties` | `secrets/keystores/release.properties` | Keystore passwords/alias | [keystores/README.md](keystores/README.md) |
| *(generate)* | `secrets/keystores/release.jks` | Release keystore binary | [keystores/README.md](keystores/README.md) |
| `play/service-account.json` | `secrets/play/service-account.json` | Play Store service account JSON | [play/README.md](play/README.md) |

### Firebase (Android + iOS)

| secrets_demo/ path | secrets/ target | Credential | Per-secret guide |
|---|---|---|---|
| `firebase/service-account.json` | `secrets/firebase/service-account.json` | Firebase service account JSON | [firebase/README.md](firebase/README.md) |
| `firebase/android_app_id` | `secrets/firebase/android_app_id` | Firebase Android prod App ID | [firebase/README.md](firebase/README.md) |
| `firebase/android_demo_app_id` | `secrets/firebase/android_demo_app_id` | Firebase Android demo App ID | [firebase/README.md](firebase/README.md) |
| `firebase/ios_app_id` | `secrets/firebase/ios_app_id` | Firebase iOS App ID | [firebase/README.md](firebase/README.md) |

### Web hosting

| secrets_demo/ path | secrets/ target | Guide |
|---|---|---|
| `cloudflare/api_token` | `secrets/cloudflare/api_token` | [cloudflare/README.md](cloudflare/README.md) |
| `cloudflare/account_id` | `secrets/cloudflare/account_id` | [cloudflare/README.md](cloudflare/README.md) |
| `netlify/auth_token` | `secrets/netlify/auth_token` | [netlify/README.md](netlify/README.md) |
| `netlify/site_id` | `secrets/netlify/site_id` | [netlify/README.md](netlify/README.md) |
| `vercel/token` | `secrets/vercel/token` | [vercel/README.md](vercel/README.md) |
| `vercel/org_id` | `secrets/vercel/org_id` | [vercel/README.md](vercel/README.md) |
| `vercel/project_id` | `secrets/vercel/project_id` | [vercel/README.md](vercel/README.md) |

## Three deployment modes

### Mode 1 — Local / manual

Populate `secrets/` from the table above, then:

```bash
# Android
bundle exec fastlane --fastlane-dir deployment android deployReleaseApkOnFirebase

# iOS
bundle exec fastlane --fastlane-dir deployment ios beta
```

### Mode 2 — GitHub Actions

Run `scripts/sync-secrets-to-github.sh` once to push secrets to your fork's
GHA repository secrets. Workflows read them automatically via `secrets.*`.

```bash
bash scripts/sync-secrets-to-github.sh
```

### Mode 3 — Framework /release command

If you're using the Claude Product Cycle framework with a vault:

```bash
/secrets pull          # materializes vault → secrets/ canonical paths
/release ios testflight
```

## Authority

- RULE-SECRETS-VAULT-001 (SV2 schema-as-code parity)
- AC78 (≥14 OSS-safe files + README)
- AC83 (key-equivalence enforced)
- AC89 (magic markers on every placeholder)
