# Secrets Directory

This directory contains sensitive credentials and keys required for iOS and Android deployment. All files in this directory are gitignored and should **NEVER** be committed to version control.

## 📋 Required Files

### iOS Deployment

| File | Description | How to Obtain |
|------|-------------|---------------|
| `shared_keys.env` | Environment variables for iOS deployment | Replace placeholder values with your credentials |
| `.match_password` | Password for encrypting Match certificates | Generate with: `openssl rand -base64 32` |
| `AuthKey.p8` | App Store Connect API key | [App Store Connect](https://appstoreconnect.apple.com) → Users and Access → Keys |
| `match_ci_key` | SSH private key for Match repository | Generate with: `ssh-keygen -t ed25519 -f secrets/match_ci_key -N ""` |
| `match_ci_key.pub` | SSH public key for Match repository | Add as deploy key to your Match repository |
| `APNAuthKey.p8` | Apple Push Notification key (optional) | [Apple Developer Portal](https://developer.apple.com/account/resources/authkeys/list) → Create new key with APNs enabled |

### Android & Firebase Deployment

| File | Description | How to Obtain |
|------|-------------|---------------|
| `firebaseAppDistributionServiceCredentialsFile.json` | Firebase service account credentials | [Firebase Console](https://console.firebase.google.com) → Project Settings → Service Accounts → Generate new private key |
| `playStorePublishServiceCredentialsFile.json` | Google Play Console service account credentials | [Google Cloud Console](https://console.cloud.google.com) → IAM → Service Accounts → Create key |

## 🚀 Quick Setup

### Option 1: Run Setup Wizard (Recommended)

```bash
# Complete iOS setup (interactive)
bash scripts/setup_ios_complete.sh

# Optional: Setup APN for push notifications
bash scripts/setup_apn_key.sh
```

### Option 2: Manual Setup

1. **Update `shared_keys.env`**
   - Replace all `REPLACE_ME` values with your actual credentials
   - Update email addresses and contact information
   - See `shared_keys.env.template` for reference

2. **Generate Match Password**
   ```bash
   openssl rand -base64 32 > secrets/.match_password
   chmod 600 secrets/.match_password
   ```

3. **Get App Store Connect API Key**
   - Go to [App Store Connect](https://appstoreconnect.apple.com)
   - Navigate to Users and Access → Keys
   - Create new key with App Manager or Admin role
   - Download the `.p8` file (you can only do this once!)
   - Save as `secrets/AuthKey.p8`
   - Note the Key ID and Issuer ID for `shared_keys.env`

4. **Generate SSH Key for Match**
   ```bash
   ssh-keygen -t ed25519 -C "fastlane-match" -f secrets/match_ci_key -N ""
   chmod 600 secrets/match_ci_key
   chmod 644 secrets/match_ci_key.pub
   ```

5. **Add Deploy Key to Match Repository**
   - Create a private GitHub repository for certificates (e.g., `ios-certificates`)
   - Go to repository Settings → Deploy keys → Add deploy key
   - Paste content of `secrets/match_ci_key.pub`
   - ✅ Enable "Allow write access"

6. **Get Firebase Service Account (if needed)**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Select your project → Settings (gear icon) → Service Accounts
   - Click "Generate new private key"
   - Save as `secrets/firebaseAppDistributionServiceCredentialsFile.json`

7. **Get Play Store Service Account (if needed)**
   - Follow [Google Play Android Developer API setup](https://developers.google.com/android-publisher/getting_started)
   - Create service account in Google Cloud Console
   - Grant access in Play Console
   - Download JSON key and save as `secrets/playStorePublishServiceCredentialsFile.json`

## 🔐 Security Best Practices

### ✅ Do:
- Keep all files in this directory private
- Use strong passwords (minimum 16 characters)
- Store credentials in a password manager
- Use environment variables in CI/CD
- Rotate API keys periodically (every 6-12 months)
- Limit API key permissions (use App Manager instead of Admin when possible)

### ❌ Don't:
- Never commit secrets to git (all files are gitignored)
- Never share credentials via insecure channels (email, Slack, etc.)
- Never use production credentials in development
- Never store credentials in plaintext outside this directory

## 📚 Documentation

- [iOS Setup Guide](../docs/IOS_SETUP.md) - Complete setup instructions
- [iOS Deployment Guide](../docs/IOS_DEPLOYMENT.md) - Deployment workflows
- [Fastlane Configuration](../docs/FASTLANE_CONFIGURATION.md) - Configuration reference

## 🔍 Verify Your Setup

After configuring all files, verify your setup:

```bash
# Check iOS configuration
bash scripts/verify_apn_setup.sh  # If using APN

# Test deployment to Firebase (dry-run)
bash scripts/deploy_firebase.sh
```

## 📁 File Permissions

Ensure proper file permissions for security:

```bash
chmod 600 secrets/shared_keys.env
chmod 600 secrets/.match_password
chmod 600 secrets/AuthKey.p8
chmod 600 secrets/APNAuthKey.p8
chmod 600 secrets/match_ci_key
chmod 644 secrets/match_ci_key.pub
chmod 600 secrets/*.json
```

## 🆘 Need Help?

- Review setup documentation in `docs/IOS_SETUP.md`
- Check troubleshooting section in `docs/IOS_DEPLOYMENT.md`
- Run the interactive setup wizard: `bash scripts/setup_ios_complete.sh`

## 🔄 Template Files

This directory includes template files to help you get started:
- `shared_keys.env.template` - Reference template with all configuration options
- All sample files have `REPLACE_ME` placeholders

Keep the `.template` file for reference, but update the actual files with your credentials.
