# Secrets Management - Complete Guide

**For:** Setting up deployment credentials
**Time:** 30-45 minutes
**Last Updated:** 2026-02-13

---

## 📖 Overview

This guide covers complete secrets management for all platforms.

**What you'll learn:**
- Complete secrets inventory (30+ secrets)
- File-to-secret mapping
- Generating secrets with `keystore-manager.sh`
- Adding secrets to GitHub Actions
- Rotating secrets
- Security best practices

---

## 📋 Table of Contents

1. [Quick Start: Automatic Sync](#quick-start-automatic-sync)
2. [Secrets Inventory](#secrets-inventory)
3. [File Structure](#file-structure)
4. [Generating Secrets](#generating-secrets)
5. [Adding to GitHub Actions](#adding-to-github-actions)
6. [Secret Mapping Reference](#secret-mapping-reference)
7. [Rotating Secrets](#rotating-secrets)
8. [Security Best Practices](#security-best-practices)
9. [Troubleshooting](#troubleshooting)

---

## Quick Start: Automatic Sync

**Template Projects (Recommended):**

```bash
# 1. Run project setup (generates Android keystores + secrets.env)
./setup-project.sh

# 2. Run iOS setup (if needed - auto-syncs iOS secrets to secrets.env)
./scripts/setup_ios_complete.sh

# Done! secrets.env is automatically synchronized with all platform secrets
```

**Manual Sync:**

```bash
# Re-scan secrets/ directory and update secrets.env
./keystore-manager.sh sync
```

**What sync does:**
1. Reads `secrets/shared_keys.env` and extracts iOS string secrets
2. Scans `secrets/` directory and encodes files to base64
3. Updates `secrets.env` with all secrets (preserves existing data)
4. Adds Desktop signing placeholders (optional, empty by default)
5. Validates result (format, required secrets, base64 encoding)

**Features:**
- Automatic backup before sync (`secrets.env.backup`)
- Preserves existing secrets if source files are missing
- Idempotent: safe to run multiple times
- Validates format and base64 encoding after sync

**When to use sync:**
- After iOS setup completes (automatic)
- After adding new files to `secrets/` directory
- After updating `shared_keys.env`
- To refresh/validate secrets.env

---

## Secrets Inventory

### Complete List (30+ Secrets)

#### Android Secrets (8)

| Secret Name | Description | Required For |
|------------|-------------|--------------|
| `ORIGINAL_KEYSTORE_FILE` | Original keystore (base64) | Play Store signing |
| `ORIGINAL_KEYSTORE_FILE_PASSWORD` | Keystore password | Play Store signing |
| `ORIGINAL_KEYSTORE_ALIAS` | Key alias | Play Store signing |
| `ORIGINAL_KEYSTORE_ALIAS_PASSWORD` | Key alias password | Play Store signing |
| `UPLOAD_KEYSTORE_FILE` | Upload keystore (base64) | Play Console upload |
| `GOOGLESERVICES` | google-services.json (base64) | Firebase integration |
| `FIREBASECREDS` | Firebase credentials (base64) | Firebase App Distribution |
| `PLAYSTORECREDS` | Play Store credentials (base64) | Play Console deployment |

#### iOS Secrets (6)

| Secret Name | Description | Required For |
|------------|-------------|--------------|
| `APPSTORE_KEY_ID` | App Store Connect API Key ID | TestFlight, App Store |
| `APPSTORE_ISSUER_ID` | API Issuer ID | TestFlight, App Store |
| `APPSTORE_AUTH_KEY` | Auth key .p8 file (base64) | TestFlight, App Store |
| `MATCH_PASSWORD` | Fastlane Match passphrase | Certificate decryption |
| `MATCH_SSH_PRIVATE_KEY` | SSH key for Match repo (base64) | Certificate repository access |
| `FIREBASECREDS` | Firebase credentials (base64) | Firebase App Distribution |

#### macOS Secrets (7)

| Secret Name | Description | Required For |
|------------|-------------|--------------|
| `APPSTORE_KEY_ID` | Same as iOS | TestFlight, App Store |
| `APPSTORE_ISSUER_ID` | Same as iOS | TestFlight, App Store |
| `APPSTORE_AUTH_KEY` | Same as iOS | TestFlight, App Store |
| `MACOS_SIGNING_KEY` | Signing certificate (base64) | macOS app signing |
| `MACOS_SIGNING_PASSWORD` | Certificate password | macOS app signing |
| `MACOS_INSTALLER_CERTIFICATE` | Installer certificate (base64) | macOS installer signing |
| `MACOS_PROVISIONING_PROFILE_BASE64` | Provisioning profile (base64) | macOS provisioning |

#### Desktop Secrets (9)

| Secret Name | Description | Required For |
|------------|-------------|--------------|
| `WINDOWS_SIGNING_KEY` | Windows certificate (base64) | Windows signing |
| `WINDOWS_SIGNING_PASSWORD` | Certificate password | Windows signing |
| `WINDOWS_SIGNING_CERTIFICATE` | Certificate alias | Windows signing |
| `MACOS_SIGNING_KEY` | macOS certificate (base64) | Desktop macOS signing |
| `MACOS_SIGNING_PASSWORD` | Certificate password | Desktop macOS signing |
| `MACOS_SIGNING_CERTIFICATE` | Certificate alias | Desktop macOS signing |
| `LINUX_SIGNING_KEY` | Linux key (optional) | Linux signing (optional) |
| `LINUX_SIGNING_PASSWORD` | Key password | Linux signing (optional) |
| `LINUX_SIGNING_CERTIFICATE` | Certificate alias | Linux signing (optional) |

#### Shared Secrets (1)

| Secret Name | Description | Required For |
|------------|-------------|--------------|
| `GITHUB_TOKEN` | GitHub authentication | GitHub Pages, Releases |

**Total: 31 secrets** (not all required for every deployment)

---

## File Structure

### Local Secrets Directory

**Structure:**

```
secrets/
├── firebaseAppDistributionServiceCredentialsFile.json  # Firebase creds
├── google-services.json                               # Android Firebase config
├── playStorePublishServiceCredentialsFile.json        # Play Store creds
├── AuthKey.p8                                         # App Store Connect key
├── match_ci_key                                       # Match SSH private key
├── match_ci_key.pub                                   # Match SSH public key
├── shared_keys.env                                    # iOS configuration
├── macos_signing.p12                                  # macOS certificate
├── macos_installer.p12                                # macOS installer cert
├── windows_signing.pfx                                # Windows certificate
└── linux_signing.key                                  # Linux key (optional)
```

**⚠️ NEVER COMMIT `secrets/` DIRECTORY** - Always in `.gitignore`

---

### Keystores Directory

**Structure:**

```
keystores/
├── original-release-key.jks      # Original Android keystore
├── upload-keystore.jks           # Upload Android keystore
├── original-release-key.jks.b64  # Base64 encoded original
└── upload-keystore.jks.b64       # Base64 encoded upload
```

**⚠️ NEVER COMMIT `keystores/` DIRECTORY** - Always in `.gitignore`

---

## Generating Secrets

### Master Script: keystore-manager.sh

**Location:** `./keystore-manager.sh`

**Purpose:** Generate, encode, and manage all secrets

---

### Step 1: Generate Android Keystores

```bash
./keystore-manager.sh generate
```

**Prompts:**
- Company/Organization details (CN, OU, O, L, S, C)
- Keystore passwords (or auto-generate)

**Output:**
```
✅ Generated original-release-key.jks
✅ Generated upload-keystore.jks
✅ Extracted certificates and keys
✅ Saved configuration to secrets.env
```

**Files created:**
- `keystores/original-release-key.jks`
- `keystores/upload-keystore.jks`
- `secrets.env` (configuration)

---

### Step 2: Setup Firebase

```bash
./firebase-setup.sh
```

**Requires:**
- Firebase CLI installed: `brew install firebase-cli`
- Logged in: `firebase login`

**Prompts:**
- Firebase project name

**Output:**
- `cmp-android/google-services.json` (4 variants)
- `cmp-ios/GoogleService-Info.plist`
- `secrets/firebaseAppDistributionServiceCredentialsFile.json` (if configured)

**Manual alternative:**
1. Go to Firebase Console
2. Create project
3. Register Android app → Download `google-services.json`
4. Register iOS app → Download `GoogleService-Info.plist`
5. Project Settings → Service Accounts → Generate new private key
6. Save as `secrets/firebaseAppDistributionServiceCredentialsFile.json`

---

### Step 3: Setup Google Play Store

**1. Create Service Account**

```bash
# Go to Google Cloud Console
# → IAM & Admin → Service Accounts → Create Service Account
# Name: "GitHub Actions Play Store"
# Role: "Service Account User"
```

**2. Generate Key**

```bash
# Service Accounts → Your account → Keys → Add Key → JSON
# Download JSON file
# Save as: secrets/playStorePublishServiceCredentialsFile.json
```

**3. Grant Play Console Access**

```bash
# Go to Play Console
# → Settings → API access
# → Link service account created above
# → Grant permissions: "Release manager" or "Admin"
```

---

### Step 4: Setup iOS (App Store Connect)

```bash
./scripts/setup_ios_complete.sh
```

**Prompts:**
- Apple Developer Team ID
- Bundle Identifier
- App Store Connect API Key ID
- App Store Connect Issuer ID
- Path to .p8 auth key file
- Match repository URL
- Match branch (default: `master`)
- Match passphrase

**Output:**
- `secrets/AuthKey.p8` - App Store Connect API key
- `secrets/match_ci_key` - SSH private key
- `secrets/match_ci_key.pub` - SSH public key
- `secrets/shared_keys.env` - Configuration

**Manual setup:**

**1. Generate App Store Connect API Key**

```bash
# Go to App Store Connect
# → Users and Access → Keys → App Store Connect API
# → Generate API Key
# - Name: "GitHub Actions"
# - Access: "Admin" or "App Manager"
# → Download .p8 file
# Save as: secrets/AuthKey.p8
# Note: Key ID and Issuer ID
```

**2. Generate SSH Key for Match**

```bash
ssh-keygen -t ed25519 -C "github-actions-match" -f secrets/match_ci_key -N ""

# Output:
# - secrets/match_ci_key (private key)
# - secrets/match_ci_key.pub (public key)
```

**3. Add SSH Key to Match Repository**

```bash
# View public key
cat secrets/match_ci_key.pub

# Go to Match repository (GitHub)
# → Settings → Deploy keys → Add deploy key
# - Title: "GitHub Actions CI"
# - Key: [paste public key]
# - ✅ Allow write access
```

**4. Create shared_keys.env**

```bash
cat > secrets/shared_keys.env <<EOF
export APPSTORE_KEY_ID="ABC123XYZ"
export APPSTORE_ISSUER_ID="12345678-1234-1234-1234-123456789012"
export MATCH_GIT_URL="git@github.com:your-org/certificates.git"
export MATCH_GIT_BRANCH="master"
export MATCH_PASSWORD="your-secure-passphrase"
export MATCH_TYPE="appstore"
EOF
```

---

### Step 5: Setup macOS Certificates

**1. Export Certificates from Xcode**

```bash
# Open Xcode → Preferences → Accounts
# → Your Apple ID → Manage Certificates
# → Select "Developer ID Application" certificate
# → Right-click → Export
# → Save as: macos_signing.p12
# → Set password

# Repeat for "Developer ID Installer" certificate
# → Save as: macos_installer.p12
```

**2. Convert to Base64**

```bash
base64 -i macos_signing.p12 -o macos_signing.p12.b64
base64 -i macos_installer.p12 -o macos_installer.p64.b64
```

---

### Step 6: Setup Desktop Signing (Optional)

**Windows:**
```bash
# Obtain code signing certificate from CA
# Export as .pfx file
# Save as: secrets/windows_signing.pfx

# Convert to base64
base64 -i secrets/windows_signing.pfx -o secrets/windows_signing.pfx.b64
```

**Linux:**
```bash
# Generate GPG key (if needed)
gpg --gen-key

# Export private key
gpg --export-secret-keys -a > secrets/linux_signing.key

# Convert to base64
base64 -i secrets/linux_signing.key -o secrets/linux_signing.key.b64
```

---

## Adding to GitHub Actions

### Option 1: Automated (keystore-manager.sh)

```bash
./keystore-manager.sh add
```

**Prerequisites:**
- GitHub CLI installed: `brew install gh`
- Authenticated: `gh auth login`

**What it does:**
1. Encodes all files in `secrets/` and `keystores/` to base64
2. Adds each secret to GitHub repository using `gh secret set`
3. Confirms success for each secret

**Output:**
```
✅ Added KEYSTORE_FILE
✅ Added GOOGLESERVICES
✅ Added FIREBASECREDS
✅ Added PLAYSTORECREDS
... (30+ secrets)
```

---

### Option 2: Manual (GitHub UI)

**1. Encode Secrets**

```bash
./keystore-manager.sh encode-secrets
```

**Output:** Base64 strings for each file

**2. Add via GitHub UI**

```bash
# Go to repository on GitHub
# → Settings → Secrets and variables → Actions
# → New repository secret

# For each secret:
# - Name: [SECRET_NAME]
# - Value: [base64 string from step 1]
# → Add secret
```

---

### Option 3: Manual (GitHub CLI)

```bash
# Individual secrets
gh secret set KEYSTORE_FILE < keystores/original-release-key.jks.b64
gh secret set GOOGLESERVICES < secrets/google-services.json.b64
gh secret set FIREBASECREDS < secrets/firebaseAppDistributionServiceCredentialsFile.json.b64

# String secrets
echo "password123" | gh secret set KEYSTORE_PASSWORD
echo "alias_password" | gh secret set KEYSTORE_ALIAS_PASSWORD
```

---

### Verify Secrets Added

```bash
# List all secrets
./keystore-manager.sh list

# Or use gh directly
gh secret list
```

**Expected output:**
```
APPSTORE_AUTH_KEY
APPSTORE_ISSUER_ID
APPSTORE_KEY_ID
FIREBASECREDS
GOOGLESERVICES
KEYSTORE_ALIAS
KEYSTORE_ALIAS_PASSWORD
KEYSTORE_FILE
KEYSTORE_PASSWORD
MACOS_INSTALLER_CERTIFICATE
... (30+ total)
```

---

## Secret Mapping Reference

### File → GitHub Secret Mapping

| Local File | GitHub Secret Name | Encoding |
|-----------|-------------------|----------|
| `keystores/original-release-key.jks` | `ORIGINAL_KEYSTORE_FILE` or `KEYSTORE_FILE` | Base64 |
| `secrets/google-services.json` | `GOOGLESERVICES` | Base64 |
| `secrets/firebaseAppDistributionServiceCredentialsFile.json` | `FIREBASECREDS` | Base64 |
| `secrets/playStorePublishServiceCredentialsFile.json` | `PLAYSTORECREDS` | Base64 |
| `secrets/AuthKey.p8` | `APPSTORE_AUTH_KEY` | Base64 |
| `secrets/match_ci_key` | `MATCH_SSH_PRIVATE_KEY` | Base64 |
| `secrets/macos_signing.p12` | `MACOS_SIGNING_KEY` | Base64 |
| `secrets/macos_installer.p12` | `MACOS_INSTALLER_CERTIFICATE` | Base64 |
| `secrets/windows_signing.pfx` | `WINDOWS_SIGNING_KEY` | Base64 |

### String Secrets (Not Files)

| Value | GitHub Secret Name | Source |
|-------|-------------------|--------|
| Keystore password | `ORIGINAL_KEYSTORE_FILE_PASSWORD` | From `secrets.env` |
| Keystore alias | `ORIGINAL_KEYSTORE_ALIAS` | From `secrets.env` |
| Alias password | `ORIGINAL_KEYSTORE_ALIAS_PASSWORD` | From `secrets.env` |
| App Store Key ID | `APPSTORE_KEY_ID` | From App Store Connect |
| Issuer ID | `APPSTORE_ISSUER_ID` | From App Store Connect |
| Match password | `MATCH_PASSWORD` | From `secrets/shared_keys.env` |
| macOS signing password | `MACOS_SIGNING_PASSWORD` | From certificate export |

---

## Rotating Secrets

### When to Rotate Secrets

**Mandatory rotation:**
- Secret compromised or exposed
- Team member with access leaves
- Certificate expired
- Regulatory requirement

**Recommended rotation:**
- Every 90 days (best practice)
- After major security incident
- When changing service accounts

---

### Rotating Android Keystore

**⚠️ WARNING:** Cannot rotate original keystore for published app

**For new apps only:**

```bash
# 1. Generate new keystore
./keystore-manager.sh generate

# 2. Re-encode
./keystore-manager.sh encode-secrets

# 3. Update GitHub secrets
./keystore-manager.sh add
```

**For published apps:** Can only rotate upload keystore (if using App Signing by Google Play)

---

### Rotating Firebase Credentials

```bash
# 1. Generate new service account key
# Firebase Console → Project Settings → Service Accounts
# → Generate new private key

# 2. Save as secrets/firebaseAppDistributionServiceCredentialsFile.json

# 3. Encode
base64 -i secrets/firebaseAppDistributionServiceCredentialsFile.json -o firebasecreds.b64

# 4. Update secret
gh secret set FIREBASECREDS < firebasecreds.b64

# 5. Delete old key from Firebase Console
```

---

### Rotating iOS Certificates

**Option 1: Force New (Deletes Existing)**

```bash
# Re-run Match with force
bundle exec fastlane match adhoc --force
bundle exec fastlane match appstore --force

# Update secrets (if SSH key changed)
./keystore-manager.sh add
```

**Option 2: Renew Expired**

```bash
# Match auto-renews expired certificates
bundle exec fastlane match adhoc
bundle exec fastlane match appstore
```

---

### Rotating App Store Connect API Key

```bash
# 1. Generate new key in App Store Connect
# → Users and Access → Keys → Generate new key

# 2. Download new .p8 file
# Save as: secrets/AuthKey.p8

# 3. Update secrets
base64 -i secrets/AuthKey.p8 -o authkey.b64
gh secret set APPSTORE_AUTH_KEY < authkey.b64

# 4. Update Key ID and Issuer ID if changed
echo "NEW_KEY_ID" | gh secret set APPSTORE_KEY_ID
echo "NEW_ISSUER_ID" | gh secret set APPSTORE_ISSUER_ID

# 5. Revoke old key in App Store Connect
```

---

## Security Best Practices

### 1. Never Commit Secrets

**Protected by `.gitignore`:**
```gitignore
secrets/
keystores/
*.jks
*.p12
*.pfx
*.p8
*.key
*.pem
*.env
google-services.json
GoogleService-Info.plist
```

**Verify before commit:**
```bash
# Check what will be committed
git status
git diff --cached

# Search for potential secrets
git diff --cached | grep -i "password\|api_key\|secret\|token"
```

---

### 2. Use Strong Passwords

**Generate secure passwords:**

```bash
# For keystore passwords
openssl rand -base64 32

# Or use keystore-manager.sh auto-generation
./keystore-manager.sh generate
# → Enter blank when prompted to auto-generate
```

**Minimum requirements:**
- 20+ characters
- Mix of uppercase, lowercase, numbers, symbols
- Not dictionary words
- Unique per secret

---

### 3. Limit Secret Access

**GitHub repository settings:**
```bash
# Settings → Environments → production
# → Environment secrets (more restricted than repository secrets)
# → Required reviewers (for production deployments)
```

**Team access:**
- Only grant secret access to necessary team members
- Use environment protection rules
- Audit secret access regularly

---

### 4. Monitor Secret Usage

```bash
# View GitHub Actions runs
gh run list

# Check for failed secrets
gh run view <run-id> --log-failed

# Review audit log
# GitHub → Settings → Security → Audit log
```

---

### 5. Use Least Privilege

**Service accounts:**
- Grant minimum required permissions
- Use role-based access (not admin)
- Separate accounts per purpose

**Examples:**
- Play Store: "Release manager" (not "Admin")
- Firebase: "Firebase Admin SDK Service Agent" (not "Owner")
- App Store: "App Manager" (not "Admin")

---

### 6. Encrypt Secrets at Rest

**Local encryption:**

```bash
# Encrypt secrets directory
tar -czf secrets.tar.gz secrets/
openssl enc -aes-256-cbc -salt -in secrets.tar.gz -out secrets.tar.gz.enc
rm secrets.tar.gz

# Decrypt when needed
openssl enc -d -aes-256-cbc -in secrets.tar.gz.enc -out secrets.tar.gz
tar -xzf secrets.tar.gz
```

---

### 7. Regular Audits

**Monthly checklist:**
- [ ] Review team members with secret access
- [ ] Check for expired certificates
- [ ] Verify all secrets still in use
- [ ] Remove unused secrets
- [ ] Update passwords rotated >90 days ago

**Audit command:**
```bash
./keystore-manager.sh view

# Check certificate expiration
keytool -list -v -keystore keystores/original-release-key.jks | grep "Valid"
security find-certificate -a -p -Z | openssl x509 -noout -enddate
```

---

## Troubleshooting

### Issue 1: Secret Not Found in GitHub Actions

**Symptoms:**
```
Error: Secret KEYSTORE_FILE not found
```

**Solutions:**

```bash
# 1. Verify secret exists
gh secret list | grep KEYSTORE_FILE

# 2. Check secret name (case-sensitive)
# Workflow uses: KEYSTORE_FILE
# Secret name must match exactly

# 3. Re-add secret
./keystore-manager.sh add

# 4. Check secret is repository-level (not environment)
# Settings → Secrets and variables → Actions → Repository secrets
```

---

### Issue 2: Base64 Decoding Fails

**Symptoms:**
```
Error: invalid base64 data
```

**Solutions:**

```bash
# 1. Re-encode properly
base64 -i input.file -o output.b64

# 2. Check for newlines (remove with -w 0)
base64 -w 0 -i input.file > output.b64

# 3. Verify encoding
base64 -d -i output.b64 -o test.file
diff input.file test.file  # Should be identical
```

---

### Issue 3: Keystore Password Wrong

**Symptoms:**
```
Keystore was tampered with, or password was incorrect
```

**Solutions:**

```bash
# 1. View password from secrets.env
cat secrets.env | grep KEYSTORE.*PASSWORD

# 2. Test locally
keytool -list -v -keystore keystores/original-release-key.jks

# 3. Re-generate keystore if password lost
# ⚠️ Only for new apps!
./keystore-manager.sh generate
```

---

### Issue 4: Match SSH Key Permission Denied

**Symptoms:**
```
Permission denied (publickey)
```

**Solutions:**

```bash
# 1. Verify SSH key is added to Match repository
# Match repo → Settings → Deploy keys
# → Check key exists with write access

# 2. Test SSH connection
ssh -T git@github.com -i secrets/match_ci_key

# 3. Re-generate key
ssh-keygen -t ed25519 -C "github-actions" -f secrets/match_ci_key -N ""
cat secrets/match_ci_key.pub
# → Add to Match repo deploy keys

# 4. Re-encode and update secret
base64 -i secrets/match_ci_key -o match_ci_key.b64
gh secret set MATCH_SSH_PRIVATE_KEY < match_ci_key.b64
```

---

## Additional Resources

- **keystore-manager.sh Help:** `./keystore-manager.sh --help`
- **GitHub Secrets Docs:** https://docs.github.com/en/actions/security-guides/encrypted-secrets
- **Firebase Service Accounts:** https://firebase.google.com/docs/admin/setup#initialize-sdk
- **App Store Connect API:** https://developer.apple.com/documentation/appstoreconnectapi
- **Fastlane Match:** https://docs.fastlane.tools/actions/match/

---

**Last Updated:** 2026-02-13
**Maintainer:** See CLAUDE.md
