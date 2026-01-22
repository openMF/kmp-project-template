# GitHub Actions iOS Configuration Migration Guide

This guide explains the new iOS configuration system for GitHub Actions and how to migrate existing
workflows.

---

## Overview

We've centralized iOS configuration to eliminate duplication and simplify maintenance. Instead of
hardcoding values in workflow files, we now use a **single source of truth**:

- **Local Development**: Configuration in `fastlane-config/project_config.rb` +
  `secrets/shared_keys.env`
- **CI/CD**: Same configuration automatically extracted by workflows

### Benefits

✅ **Single Source of Truth** - Update config once, works everywhere
✅ **Less Duplication** - No need to sync values between local and CI
✅ **Easier Maintenance** - Change bundle ID? Just update `project_config.rb`
✅ **Consistent** - Same configuration locally and in CI
✅ **Type Safe** - Ruby validates configuration at extraction time

---

## What Changed

### Before (v1): Hardcoded Configuration

```yaml
# .github/workflows/multi-platform-build-and-publish.yml
with:
  app_identifier: 'org.mifos.kmp.template'         # ❌ Hardcoded
  git_url: 'git@github.com:openMF/ios-provisioning-profile.git'  # ❌ Hardcoded
  git_branch: 'master'                             # ❌ Hardcoded
  match_type: 'adhoc'                              # ❌ Hardcoded
  provisioning_profile_name: 'match AdHoc org.mifos.kmp.template'  # ❌ Hardcoded
  firebase_app_id: '1:728434912738:ios:1d81f8e53ca7a6f31a1dbb'    # ❌ Hardcoded
  # ... 35+ lines of configuration
```

**Problems:**

- Duplication between workflow and `project_config.rb`
- Easy to forget updating one when changing the other
- Difficult to maintain across multiple projects

### After (v2): Config-Aware

```yaml
# .github/workflows/multi-platform-build-and-publish.yml
with:
  ios_package_name: 'cmp-ios'
  # All iOS configuration read from fastlane-config/project_config.rb ✅
  distribute_ios_firebase: ${{ inputs.distribute_ios_firebase }}
  distribute_ios_testflight: ${{ inputs.distribute_ios_testflight }}
  distribute_ios_appstore: ${{ inputs.distribute_ios_appstore }}
  # ... ~10 lines total
```

**Benefits:**

- Configuration in `project_config.rb` (app-specific) and `secrets/shared_keys.env` (team-shared)
- Workflows automatically extract values using `fastlane-config/extract_config.rb`
- **70% reduction** in workflow configuration (35 lines → 10 lines)

---

## Required GitHub Secrets

Configure these secrets in your repository settings (Settings → Secrets and variables → Actions).

### iOS Deployment Secrets (Required)

| Secret Name             | Description                                   | How to Obtain                                                                    |
|-------------------------|-----------------------------------------------|----------------------------------------------------------------------------------|
| `TEAM_ID`               | Apple Developer Team ID (10 characters)       | [Apple Developer Account](https://developer.apple.com/account) → Membership      |
| `APPSTORE_KEY_ID`       | App Store Connect API Key ID                  | [App Store Connect](https://appstoreconnect.apple.com) → Users and Access → Keys |
| `APPSTORE_ISSUER_ID`    | App Store Connect API Issuer ID (UUID)        | Same location as Key ID                                                          |
| `APPSTORE_AUTH_KEY`     | Base64-encoded AuthKey.p8 file                | Download .p8 from App Store Connect (one-time only!)                             |
| `MATCH_PASSWORD`        | Fastlane Match repository encryption password | Generate: `openssl rand -base64 32`                                              |
| `MATCH_SSH_PRIVATE_KEY` | Base64-encoded SSH private key for Match repo | Generate: `ssh-keygen -t ed25519`                                                |

### Firebase Distribution (Optional)

| Secret Name     | Description                                  |
|-----------------|----------------------------------------------|
| `FIREBASECREDS` | Base64-encoded Firebase service account JSON |

### TestFlight/App Store Contact Info (Required for Deployment)

| Secret Name                  | Example Value    |
|------------------------------|------------------|
| `TESTFLIGHT_CONTACT_EMAIL`   | team@example.com |
| `TESTFLIGHT_FIRST_NAME`      | Your             |
| `TESTFLIGHT_LAST_NAME`       | Name             |
| `TESTFLIGHT_PHONE`           | +1234567890      |
| `APPSTORE_REVIEW_EMAIL`      | team@example.com |
| `APPSTORE_REVIEW_FIRST_NAME` | Your             |
| `APPSTORE_REVIEW_LAST_NAME`  | Name             |
| `APPSTORE_REVIEW_PHONE`      | +1234567890      |

### Android/Desktop Secrets (Unchanged)

Existing Android and desktop secrets remain the same:

- `ORIGINAL_KEYSTORE_FILE`, `UPLOAD_KEYSTORE_FILE` (Android)
- `GOOGLESERVICES`, `PLAYSTORECREDS`, `FIREBASECREDS` (Android/iOS)
- `NOTARIZATION_*` (macOS notarization)
- `*_SIGNING_*` (Windows/macOS/Linux signing)

---

## How to Encode Files for Secrets

### macOS

```bash
# Encode AuthKey.p8
base64 -i secrets/AuthKey.p8 | pbcopy

# Encode SSH key
base64 -i secrets/match_ci_key | pbcopy

# Then paste into GitHub Secrets UI
```

### Linux

```bash
# Encode AuthKey.p8
base64 -w 0 secrets/AuthKey.p8 | xclip -selection clipboard

# Encode SSH key
base64 -w 0 secrets/match_ci_key | xclip -selection clipboard

# Then paste into GitHub Secrets UI
```

### Windows (PowerShell)

```powershell
# Encode AuthKey.p8
[Convert]::ToBase64String([IO.File]::ReadAllBytes("secrets\AuthKey.p8")) | Set-Clipboard

# Then paste into GitHub Secrets UI
```

---

## How to Update Your Project

### Step 1: Verify Configuration Files

Ensure your `project_config.rb` has the new structure:

```bash
# Check for IOS and IOS_SHARED sections
grep -A 5 "IOS_SHARED" fastlane-config/project_config.rb
```

**Expected:** You should see `IOS_SHARED` with `ENV['TEAM_ID']`, `ENV['APPSTORE_KEY_ID']`, etc.

If missing, your project needs the iOS configuration update. See [iOS Setup Guide](IOS_SETUP.md).

### Step 2: Test Config Extraction

Run the extraction script locally:

```bash
ruby fastlane-config/extract_config.rb
```

**Expected Output:**

```json
{
  "app_identifier": "org.mifos.kmp.template",
  "firebase_app_id": "1:728434912738:ios:1d81f8e53ca7a6f31a1dbb",
  "team_id": "L432S2FZP5",
  "match_git_url": "git@github.com:openMF/ios-provisioning-profile.git",
  ...
}
```

If you see errors, fix your `project_config.rb` configuration.

### Step 3: Configure GitHub Secrets

Add all required secrets listed above to your repository:

1. Go to repository Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Add each secret from the table above

**Tip:** Use the encoding commands to prepare base64 values.

### Step 4: Update Workflow File

Your workflow file (`.github/workflows/multi-platform-build-and-publish.yml`) should already have
the updated structure with comments like:

```yaml
# iOS configuration now read from fastlane-config/project_config.rb
```

If not, remove hardcoded iOS configuration values and add the comment.

### Step 5: Test Workflow

Trigger a manual workflow run:

1. Go to Actions tab
2. Select "Multi-Platform Build and Publish"
3. Click "Run workflow"
4. Select options and run

**Monitor:** Check workflow logs for config extraction step. It should output extracted JSON.

---

## Configuration Mapping

Here's how workflow values map to `project_config.rb`:

| Workflow Value              | Configuration Location                                      |
|-----------------------------|-------------------------------------------------------------|
| `app_identifier`            | `IOS[:app_identifier]`                                      |
| `firebase_app_id`           | `IOS[:firebase][:app_id]`                                   |
| `firebase_groups`           | `IOS[:firebase][:groups]`                                   |
| `team_id`                   | `IOS_SHARED[:team_id]`                                      |
| `git_url`                   | `IOS_SHARED[:code_signing][:match_git_url]`                 |
| `git_branch`                | `IOS_SHARED[:code_signing][:match_git_branch]`              |
| `match_type`                | `IOS_SHARED[:code_signing][:match_type]`                    |
| `provisioning_profile_name` | `IOS_SHARED[:code_signing][:provisioning_profiles][:adhoc]` |

**Dynamic Values:** Provisioning profile names are computed automatically:

```ruby
provisioning_profiles : {
  adhoc: "match AdHoc #{IOS[:app_identifier]}",
  appstore: "match AppStore #{IOS[:app_identifier]}"
}
```

When you change `app_identifier`, provisioning profiles update automatically! ✨

---

## Troubleshooting

### Error: "Cannot find IOS_SHARED"

**Cause:** Your `project_config.rb` doesn't have the new configuration structure.

**Solution:** Ensure you have both `IOS` and `IOS_SHARED` sections in
`fastlane-config/project_config.rb`.

### Error: "uninitialized constant FastlaneConfig"

**Cause:** Ruby can't load the project_config module.

**Solution:**

```bash
# Verify file exists
ls -la fastlane-config/project_config.rb

# Check Ruby syntax
ruby -c fastlane-config/project_config.rb

# Test loading
ruby -r './fastlane-config/project_config' -e 'puts "OK"'
```

### Workflow fails: "Invalid app_identifier"

**Cause:** Config extraction failed or returned invalid value.

**Solution:**

1. Check workflow logs for extraction step output
2. Verify `project_config.rb` has valid `app_identifier`
3. Test locally: `ruby fastlane-config/extract_config.rb`

### Provisioning Profile Not Found

**Cause:** Profile name doesn't match Match repository.

**Solution:**

1. Verify profile naming: `match AdHoc <bundle_id>`
2. Check Match repository has profiles for your bundle ID
3. Run: `bundle exec fastlane ios sync_certificates` locally

### GitHub Secrets Not Working

**Cause:** Secrets not configured or incorrectly encoded.

**Solution:**

1. Verify secrets exist in repository settings
2. Re-encode files using commands above
3. Ensure no extra whitespace in secret values
4. Test with simple workflow first

---

## Migration Checklist

Use this checklist when migrating a project:

- [ ] ✅ **Verify** `project_config.rb` has `IOS` and `IOS_SHARED` sections
- [ ] ✅ **Test** config extraction: `ruby fastlane-config/extract_config.rb`
- [ ] ✅ **Configure** all required GitHub Secrets
- [ ] ✅ **Encode** binary files (AuthKey.p8, SSH keys) to base64
- [ ] ✅ **Update** workflow file (remove hardcoded iOS values)
- [ ] ✅ **Test** workflow run (Firebase distribution)
- [ ] ✅ **Verify** logs show extracted configuration
- [ ] ✅ **Test** local deployment: `bash scripts/deploy_firebase.sh`
- [ ] ✅ **Document** any project-specific configuration
- [ ] ✅ **Update** team documentation with new process

---

## Local vs CI Configuration

### Local Development

```bash
# Configuration files
fastlane-config/project_config.rb    # App config
secrets/shared_keys.env               # Team secrets

# Deployment
source secrets/shared_keys.env
export MATCH_PASSWORD=$(cat secrets/.match_password)
bundle exec fastlane ios deploy_on_firebase
```

### CI/CD (GitHub Actions)

```yaml
# Configuration extraction
- run: ruby fastlane-config/extract_config.rb > config.json

# Secrets from GitHub Secrets
- env:
    TEAM_ID: ${{ secrets.TEAM_ID }}
    APPSTORE_KEY_ID: ${{ secrets.APPSTORE_KEY_ID }}
    # ... etc

# Deployment
- run: bundle exec fastlane ios deploy_on_firebase
```

**Result:** Same Fastlane configuration, different secret sources!

---

## Best Practices

### 1. Keep Secrets Secret

❌ **Never** commit secrets to git:

- `secrets/shared_keys.env`
- `secrets/.match_password`
- `secrets/AuthKey.p8`
- `secrets/match_ci_key`

✅ **Always** use:

- `.gitignore` for local files
- GitHub Secrets for CI/CD

### 2. Use ENV Variables

✅ **Good:** `ENV['TEAM_ID'] || "fallback"`
❌ **Bad:** Hardcoded values

This allows overriding in CI without changing code.

### 3. Validate Configuration

```bash
# Before deployment
ruby -c fastlane-config/project_config.rb
ruby fastlane-config/extract_config.rb

# Expect valid JSON output
```

### 4. Document Project-Specific Config

Each project may have unique requirements. Document:

- Custom fastlane lanes
- Special provisioning profiles
- Deployment schedules
- Testing requirements

### 5. Keep Documentation Updated

When configuration changes:

- Update `project_config.rb`
- Test locally
- Update team documentation
- Notify team members

---

## Additional Resources

- [iOS Setup Guide](IOS_SETUP.md) - Complete iOS configuration setup
- [iOS Deployment Guide](IOS_DEPLOYMENT.md) - Deployment workflows and processes
- [Fastlane Documentation](https://docs.fastlane.tools/) - Official Fastlane docs
- [App Store Connect API](https://developer.apple.com/documentation/appstoreconnectapi) - Apple's
  API documentation

---

## Support

If you encounter issues:

1. **Check workflow logs** for error messages
2. **Test locally** with `ruby fastlane-config/extract_config.rb`
3. **Verify secrets** are correctly configured
4. **Review** this migration guide
5. **Open an issue** with detailed error logs

---

**Last Updated:** January 2026
**Version:** 2.0.0
