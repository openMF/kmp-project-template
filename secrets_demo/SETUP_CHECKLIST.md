# Secrets Setup Checklist

Use this checklist when setting up a new fork. Complete the platforms you intend to deploy to.

## Quick start

```bash
# 1. Copy the full structure into secrets/
cp -r secrets_demo/* secrets/

# 2. Work through the sections below, replacing placeholder files with real ones

# 3. Sync everything to GitHub Actions
bash scripts/sync-secrets-to-github.sh --dry-run   # preview first
bash scripts/sync-secrets-to-github.sh              # push all secrets
```

---

## iOS — required for TestFlight / App Store

### 1. App Store Connect API Key

See [secrets_demo/appstore/README.md](appstore/README.md) for detailed steps.

- [ ] Create API key at [App Store Connect → Keys](https://appstoreconnect.apple.com/access/api)
- [ ] Place `.p8` file at `secrets/apple/appstore/AuthKey.p8`
- [ ] Write Key ID (10 chars) to `secrets/apple/appstore/key_id`
- [ ] Write Issuer ID (UUID) to `secrets/apple/appstore/issuer_id`

### 2. Apple Developer Team ID

- [ ] Find Team ID at [Apple Developer → Membership](https://developer.apple.com/account)
- [ ] Set `apple.team.id` in `gradle/fork.properties` (copy from `gradle/fork.properties.template`)

### 3. Fastlane Match (Code Signing)

See [secrets_demo/match/README.md](match/README.md) for detailed steps.

- [ ] Create private GitHub repo for certificates (e.g. `your-org/ios-certificates`)
- [ ] Generate SSH key: `ssh-keygen -t ed25519 -f secrets/apple/match/match_ci_key -N ""`
- [ ] Add `secrets/apple/match/match_ci_key.pub` as deploy key to the cert repo (write access)
- [ ] Generate password: `openssl rand -base64 32 | tr -d '\n' > secrets/apple/match/.match_password`
- [ ] Set `apple.match.git.url=git@github.com:your-org/ios-certificates.git` in `gradle/fork.properties`
- [ ] Run `bundle exec fastlane match init` once (first fork only)

### 4. Contact information (for TestFlight and App Store review)

Edit `gradle/fork.properties` (copy from `gradle/fork.properties.template`) and fill in:

- [ ] `org.email` / `org.first.name` / `org.last.name` / `org.phone`
- [ ] `apple.tf.groups` (comma-separated TestFlight tester group names)
- [ ] `org.marketing.url` / `org.privacy.url` / `org.support.url`

### 5. APN Push Notifications (optional)

See [secrets_demo/apn/README.md](apn/README.md).

- [ ] Create APN key at [Apple Developer → Keys](https://developer.apple.com/account/resources/authkeys/list)
- [ ] Place `.p8` at `secrets/ios/apn/APNAuthKey.p8`
- [ ] Write Key ID to `secrets/ios/apn/key_id`
- [ ] Write Team ID to `secrets/ios/apn/team_id`

---

## Android — required for Play Store

### 6. Release Keystore

See [secrets_demo/keystores/README.md](keystores/README.md) for detailed steps.

- [ ] Generate keystore: `keytool -genkey -keystore secrets/android/keystores/release.jks -alias release -keyalg RSA -keysize 4096 -validity 10000`
- [ ] Create `secrets/android/keystores/release.properties` with `storePassword`, `keyAlias`, `keyPassword`
- [ ] **Back up the keystore** — you cannot recover it if lost

### 7. Google Play Service Account

See [secrets_demo/play/README.md](play/README.md) for detailed steps.

- [ ] Create service account in Google Cloud Console
- [ ] Grant service account access in Play Console (Release Manager role)
- [ ] Place JSON key at `secrets/android/play/service-account.json`

---

## Firebase — required for Firebase App Distribution (iOS + Android)

See [secrets_demo/firebase/README.md](firebase/README.md) for detailed steps.

- [ ] Download service account JSON from Firebase Console → Project Settings → Service Accounts
- [ ] Place at `secrets/android/firebase/service-account.json`
- [ ] Write Android prod App ID to `secrets/android/firebase/android_app_id`
- [ ] Write Android demo App ID to `secrets/android/firebase/android_demo_app_id`
- [ ] Write iOS App ID to `secrets/ios/firebase/ios_app_id`
- [ ] Set `apple.tf.groups` in `gradle/fork.properties` (Firebase tester group names)

---

## Web hosting — required for web deployment

### Cloudflare Pages
- [ ] [cloudflare/README.md](cloudflare/README.md) → `secrets/web/cloudflare/api_token`, `secrets/web/cloudflare/account_id`

### Netlify
- [ ] [netlify/README.md](netlify/README.md) → `secrets/web/netlify/auth_token`, `secrets/web/netlify/site_id`

### Vercel
- [ ] [vercel/README.md](vercel/README.md) → `secrets/web/vercel/token`, `secrets/web/vercel/org_id`, `secrets/web/vercel/project_id`

---

## Final step: sync to GitHub Actions

```bash
# Verify secrets/ looks right
ls -la secrets/
ls -la secrets/apple/appstore/ secrets/apple/match/ secrets/android/firebase/ secrets/android/play/ secrets/android/keystores/

# Dry run to preview what will be set
bash scripts/sync-secrets-to-github.sh --dry-run

# Sync all
bash scripts/sync-secrets-to-github.sh

# Or sync by category
bash scripts/sync-secrets-to-github.sh --only ios
bash scripts/sync-secrets-to-github.sh --only android
bash scripts/sync-secrets-to-github.sh --only firebase
bash scripts/sync-secrets-to-github.sh --only web
```

Once synced, trigger a workflow:

```bash
gh workflow run .github/workflows/multi-platform-build-and-publish.yml
```
