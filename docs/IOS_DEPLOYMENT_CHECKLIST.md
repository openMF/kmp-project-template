# iOS Deployment Verification Checklist

This document provides a comprehensive checklist to verify that all iOS deployment components are properly configured and ready for automatic deployment.

## Quick Start

Run the automated verification script:

```bash
bash scripts/verify_ios_deployment.sh
```

This script will check all critical components and report pass/fail status.

---

## Manual Verification Checklist

### 1. Version Management ✓

**Critical Files:**
- `fastlane/FastFile` - Version sanitization logic
- `cmp-ios/iosApp/Info.plist` - Dynamic versioning variables
- `cmp-ios/iosApp.xcodeproj/project.pbxproj` - Xcode build settings

**Checks:**
- [ ] `get_version_from_gradle` helper function exists in FastFile
- [ ] Function transforms `2026.1.1-beta.0.13` → `2026.1.13` (Year.Month.CommitCount)
- [ ] Info.plist uses `$(MARKETING_VERSION)` for CFBundleShortVersionString
- [ ] Info.plist uses `$(CURRENT_PROJECT_VERSION)` for CFBundleVersion
- [ ] Xcode project has MARKETING_VERSION in both Debug and Release configs
- [ ] Xcode project has CURRENT_PROJECT_VERSION in both Debug and Release configs

**Test Command:**
```bash
bash scripts/check_ios_version.sh
```

---

### 2. Export Compliance ✓

**Critical Files:**
- `cmp-ios/iosApp/Info.plist` - Encryption compliance key

**Checks:**
- [ ] `ITSAppUsesNonExemptEncryption` key exists in Info.plist
- [ ] Value is set to `<false/>`
- [ ] TestFlight lane has `uses_non_exempt_encryption: false` parameter

**Verification:**
```bash
grep -A1 "ITSAppUsesNonExemptEncryption" cmp-ios/iosApp/Info.plist
```

---

### 3. FastFile Lanes ✓

**Critical Lanes:**
- `deploy_on_firebase` - Firebase App Distribution
- `beta` - TestFlight deployment
- `release` - App Store deployment

**Firebase Lane Checks:**
- [ ] Uses `get_version_from_gradle(sanitize_for_appstore: false)`
- [ ] Accepts full semantic version with pre-release identifiers

**Beta Lane Checks:**
- [ ] Uses `get_version_from_gradle(sanitize_for_appstore: true)`
- [ ] Increments version with sanitized App Store version
- [ ] Gets latest TestFlight build number
- [ ] Increments build number (latest + 1)
- [ ] Has `uses_non_exempt_encryption: false`

**Release Lane Checks:**
- [ ] Uses `get_version_from_gradle(sanitize_for_appstore: true)`
- [ ] Increments version with sanitized version
- [ ] Gets latest TestFlight build number
- [ ] Generates release notes from git commits
- [ ] Creates `fastlane/metadata/en-US/` directory
- [ ] Writes `release_notes.txt` before deliver action
- [ ] Has `copyright` parameter with dynamic year
- [ ] Has `skip_metadata: false` (to upload release notes)
- [ ] Has `skip_screenshots: true`
- [ ] Has `overwrite_screenshots: false`

---

### 4. Configuration Files ✓

**project_config.rb:**
- [ ] `IOS[:app_identifier]` set to `org.mifos.kmp.template`
- [ ] `IOS[:metadata_path]` set to `"./fastlane/metadata"`
- [ ] `IOS_SHARED[:testflight][:uses_non_exempt_encryption]` is `false`
- [ ] `IOS_SHARED[:appstore][:skip_app_version_update]` is `false`
- [ ] `IOS_SHARED[:appstore][:submit_for_review]` is `true`
- [ ] `IOS_SHARED[:appstore][:automatic_release]` is `true`

**ios_config.rb:**
- [ ] Properly merges IOS and IOS_SHARED configurations
- [ ] BUILD_CONFIG includes all required keys
- [ ] TESTFLIGHT_CONFIG references proper hash
- [ ] APPSTORE_CONFIG references proper hash

---

### 5. Metadata Strategy ✓

**Critical Understanding:**
- `fastlane/metadata/` and `fastlane/screenshots/` are in `.gitignore`
- Metadata is generated dynamically at runtime
- CI/CD works because files are created during deployment

**Checks:**
- [ ] `fastlane/metadata` in `.gitignore`
- [ ] `fastlane/screenshots` in `.gitignore`
- [ ] FastFile creates metadata directory at runtime (line ~667-670)
- [ ] FastFile writes release_notes.txt from git commits
- [ ] `skip_metadata: false` to allow release notes upload

---

### 6. Deployment Scripts ✓

**Required Scripts:**
- `scripts/deploy_firebase.sh` - Firebase deployment
- `scripts/deploy_testflight.sh` - TestFlight deployment
- `scripts/deploy_appstore.sh` - App Store deployment
- `scripts/check_ios_version.sh` - Version verification
- `scripts/verify_ios_deployment.sh` - Deployment verification

**All Scripts Must:**
- [ ] Be executable (`chmod +x`)
- [ ] Check for macOS
- [ ] Validate prerequisites (Xcode, Ruby, Bundler)
- [ ] Validate required secret files
- [ ] Load configuration from `secrets/shared_keys.env`
- [ ] Provide clear error messages

---

### 7. Secret Files ✓

**Required Files in `secrets/`:**
- [ ] `shared_keys.env` - Shared iOS configuration
- [ ] `.match_password` - Match repository password
- [ ] `match_ci_key` - SSH key for Match repository
- [ ] `AuthKey.p8` - App Store Connect API key
- [ ] `firebaseAppDistributionServiceCredentialsFile.json` - Firebase credentials

**Environment Variables in shared_keys.env:**
- [ ] `TEAM_ID` - Apple Developer Team ID
- [ ] `APPSTORE_KEY_ID` - App Store Connect API Key ID
- [ ] `APPSTORE_ISSUER_ID` - App Store Connect API Issuer ID
- [ ] `MATCH_GIT_URL` - Match repository URL
- [ ] `MATCH_GIT_BRANCH` - Match repository branch

---

### 8. Documentation ✓

**Required Documentation:**
- [ ] `docs/IOS_DEPLOYMENT.md` - Deployment guide
- [ ] `docs/IOS_SETUP.md` - Initial setup guide
- [ ] `docs/IOS_DEPLOYMENT_CHECKLIST.md` - This checklist
- [ ] `docs/FASTLANE_CONFIGURATION.md` - Fastlane configuration

---

## Common Issues and Solutions

### Issue 1: Version Format Error (-19239)

**Error:**
```
CFBundleShortVersionString, "2026.1.1-beta.0.9", must be composed of
one to three period-separated integers.
```

**Solution:**
1. Verify `get_version_from_gradle` exists in FastFile
2. Ensure lane uses `sanitize_for_appstore: true`
3. Check Info.plist uses `$(MARKETING_VERSION)` variable

---

### Issue 2: Missing whatsNew Attribute

**Error:**
```
The provided entity is missing a required attribute - You must provide
a value for the attribute 'whatsNew' with this request
```

**Solution:**
1. Verify FastFile creates `release_notes_path` directory
2. Verify FastFile writes release notes before deliver
3. Ensure `skip_metadata: false` in deliver action
4. Check `metadata_path` is set correctly

---

### Issue 3: Cannot Submit for Review

**Error:**
```
Cannot submit for review - could not find an editable version
```

**Solution:**
1. Set `skip_app_version_update: false` in project_config.rb
2. Ensure `submit_for_review: true`
3. Verify version is being incremented

---

### Issue 4: Copyright Precheck Failure

**Error:**
```
Failed: Incorrect, or missing copyright date
```

**Solution:**
1. Add `copyright` parameter to deliver action
2. Use format: `"#{Time.now.year} #{FastlaneConfig::ProjectConfig::ORGANIZATION_NAME}"`
3. Ensure current year is used dynamically

---

### Issue 5: Encryption Compliance Questions

**Problem:** Manual questions during upload

**Solution:**
1. Add `ITSAppUsesNonExemptEncryption` key to Info.plist
2. Set value to `<false/>`
3. Add `uses_non_exempt_encryption: false` to pilot action

---

## Testing the Configuration

### Test 1: Version Check

```bash
bash scripts/check_ios_version.sh
```

**Expected Output:**
```
Full Version from Gradle: 2026.1.1-beta.0.13
App Store Compatible Version: 2026.1.13
Verification: PASSED ✓
```

---

### Test 2: Deployment Verification

```bash
bash scripts/verify_ios_deployment.sh
```

**Expected Output:**
```
✅ All Critical Checks Passed!
Pass Rate: 100.0%
iOS deployment configuration is ready for automatic deployment!
```

---

### Test 3: Version Progression

```bash
# Check current version
bash scripts/check_ios_version.sh

# Make a commit
git add .
git commit -m "test: version progression"

# Check new version (commit count should increment)
bash scripts/check_ios_version.sh
```

**Expected:** Commit count increments: `2026.1.13` → `2026.1.14`

---

## CI/CD Integration

### GitHub Actions Required Secrets

Configure these secrets in your GitHub repository:

- `SHARED_KEYS_ENV` - Content of `secrets/shared_keys.env`
- `MATCH_PASSWORD` - Match repository password
- `MATCH_SSH_KEY` - SSH private key for Match repository
- `APPSTORE_API_KEY` - App Store Connect API key (.p8 file content)

### Example Workflow

```yaml
name: iOS App Store Deployment

on:
  push:
    tags:
      - 'v*'

jobs:
  deploy:
    runs-on: macos-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Setup Ruby
        uses: ruby/setup-ruby@v1
        with:
          ruby-version: '3.0'
          bundler-cache: true

      - name: Setup secrets
        run: |
          mkdir -p secrets
          echo "${{ secrets.SHARED_KEYS_ENV }}" > secrets/shared_keys.env
          echo "${{ secrets.MATCH_PASSWORD }}" > secrets/.match_password
          echo "${{ secrets.MATCH_SSH_KEY }}" > secrets/match_ci_key
          echo "${{ secrets.APPSTORE_API_KEY }}" > secrets/AuthKey.p8
          chmod 600 secrets/match_ci_key

      - name: Deploy to App Store
        run: bash scripts/deploy_appstore.sh
```

---

## Summary

### Pre-Deployment Checklist

- [ ] All secret files exist in `secrets/` directory
- [ ] Info.plist uses dynamic versioning variables
- [ ] Xcode project has MARKETING_VERSION and CURRENT_PROJECT_VERSION
- [ ] FastFile has version sanitization logic
- [ ] All deployment scripts are executable
- [ ] Version check script passes
- [ ] Verification script passes
- [ ] Bundle dependencies installed: `bundle install`

### Ready for Deployment When:

1. ✅ Version check script passes
2. ✅ Verification script shows 100% pass rate
3. ✅ All secret files are in place
4. ✅ App Store Connect API credentials configured
5. ✅ Match repository accessible

---

## Additional Resources

- [iOS Deployment Guide](./IOS_DEPLOYMENT.md)
- [iOS Setup Guide](./IOS_SETUP.md)
- [Fastlane Configuration](./FASTLANE_CONFIGURATION.md)
- [Fastlane Documentation](https://docs.fastlane.tools/)
- [App Store Connect API](https://developer.apple.com/documentation/appstoreconnectapi)
- [App Store Review Guidelines](https://developer.apple.com/app-store/review/guidelines/)

---

**Last Updated:** 2026-01-22
**Status:** Ready for Deployment ✅
