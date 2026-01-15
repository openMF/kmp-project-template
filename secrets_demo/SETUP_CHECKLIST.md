# Secrets Setup Checklist

Use this checklist to track your progress in setting up all required credentials.

## iOS Credentials

### 1. App Store Connect API Key
- [ ] Create API key in [App Store Connect](https://appstoreconnect.apple.com) → Users and Access → Keys
- [ ] Download `.p8` file (you can only do this once!)
- [ ] Replace content in `secrets/AuthKey.p8`
- [ ] Note the Key ID: `____________________`
- [ ] Note the Issuer ID: `____________________`
- [ ] Update `APPSTORE_KEY_ID` in `secrets/shared_keys.env`
- [ ] Update `APPSTORE_ISSUER_ID` in `secrets/shared_keys.env`

### 2. Apple Developer Team
- [ ] Get Team ID from [Apple Developer](https://developer.apple.com/account) → Membership
- [ ] Team ID: `____________________`
- [ ] Update `TEAM_ID` in `secrets/shared_keys.env`

### 3. Match Repository (Code Signing)
- [ ] Create private GitHub repository (e.g., `ios-certificates`)
- [ ] Repository URL: `____________________`
- [ ] Generate SSH key: `ssh-keygen -t ed25519 -f secrets/match_ci_key -N ""`
- [ ] Add `secrets/match_ci_key.pub` as deploy key to repository (with write access)
- [ ] Update `MATCH_GIT_URL` in `secrets/shared_keys.env`
- [ ] Generate Match password: `openssl rand -base64 32 > secrets/.match_password`

### 4. Contact Information
- [ ] Update `TESTFLIGHT_CONTACT_EMAIL` in `secrets/shared_keys.env`
- [ ] Update `TESTFLIGHT_FIRST_NAME` in `secrets/shared_keys.env`
- [ ] Update `TESTFLIGHT_LAST_NAME` in `secrets/shared_keys.env`
- [ ] Update `TESTFLIGHT_PHONE` in `secrets/shared_keys.env`
- [ ] Update `APPSTORE_REVIEW_EMAIL` in `secrets/shared_keys.env`
- [ ] Update `APPSTORE_REVIEW_FIRST_NAME` in `secrets/shared_keys.env`
- [ ] Update `APPSTORE_REVIEW_LAST_NAME` in `secrets/shared_keys.env`
- [ ] Update `APPSTORE_REVIEW_PHONE` in `secrets/shared_keys.env`

### 5. Optional: Apple Push Notifications (APN)
- [ ] Create APN key in [Apple Developer](https://developer.apple.com/account/resources/authkeys/list)
- [ ] Download APN `.p8` file
- [ ] Replace content in `secrets/APNAuthKey.p8`
- [ ] Note APN Key ID: `____________________`
- [ ] Uncomment and update APN config in `secrets/shared_keys.env`

## Firebase Credentials

### 6. Firebase App Distribution
- [ ] Go to [Firebase Console](https://console.firebase.google.com) → Project Settings → Service Accounts
- [ ] Click "Generate new private key"
- [ ] Replace content in `secrets/firebaseAppDistributionServiceCredentialsFile.json`

## Android Credentials (if deploying to Play Store)

### 7. Google Play Console Service Account
- [ ] Follow [setup guide](https://developers.google.com/android-publisher/getting_started)
- [ ] Create service account in Google Cloud Console
- [ ] Grant access in Play Console
- [ ] Download JSON key
- [ ] Replace content in `secrets/playStorePublishServiceCredentialsFile.json`

## Verification

### 8. Test Your Setup
- [ ] Run: `bash scripts/verify_apn_setup.sh` (if using APN)
- [ ] Run: `bash scripts/deploy_firebase.sh` (dry-run to check configuration)
- [ ] Verify all files have correct permissions:
  ```bash
  ls -la secrets/
  # Sensitive files should be -rw------- (600)
  # Public keys and docs should be -rw-r--r-- (644)
  ```

## Quick Reference

**Files to Update:**
1. `secrets/shared_keys.env` - Update ALL "REPLACE_ME" values
2. `secrets/.match_password` - Generate with: `openssl rand -base64 32`
3. `secrets/AuthKey.p8` - Download from App Store Connect
4. `secrets/match_ci_key` - Generate with: `ssh-keygen -t ed25519 -f secrets/match_ci_key -N ""`
5. `secrets/match_ci_key.pub` - Auto-generated with private key
6. `secrets/APNAuthKey.p8` - Download from Apple Developer (optional)
7. `secrets/firebaseAppDistributionServiceCredentialsFile.json` - Download from Firebase
8. `secrets/playStorePublishServiceCredentialsFile.json` - Download from Google Cloud

**Or use the automated wizard:**
```bash
bash scripts/setup_ios_complete.sh
```

---

✅ **Setup Complete!** Once all checkboxes are marked, you're ready to deploy.

See `secrets/README.md` for detailed instructions.
