# Fastlane Configuration Checklist

Use this checklist when setting up fastlane for a new project.

## ✅ Step 1: Update project_config.rb

Open `fastlane-config/project_config.rb` and update:

### Core Information
- [ ] `PROJECT_NAME` - Your project name
- [ ] `ORGANIZATION_NAME` - Your organization name

### Android Configuration
- [ ] `package_name` - Android package identifier (e.g., com.yourcompany.app)
- [ ] `keystore.file` - Keystore filename
- [ ] `keystore.password` - Keystore password
- [ ] `keystore.key_alias` - Key alias
- [ ] `keystore.key_password` - Key password
- [ ] `firebase.prod_app_id` - Firebase Android production app ID
- [ ] `firebase.demo_app_id` - Firebase Android demo app ID (if applicable)
- [ ] `firebase.groups` - Firebase tester groups
- [ ] Update APK/AAB paths if module names changed

### iOS Configuration
- [ ] `app_identifier` - iOS bundle identifier (e.g., com.yourcompany.app)
- [ ] `team_id` - Apple Developer Team ID
- [ ] `app_store_connect.key_id` - App Store Connect API Key ID
- [ ] `app_store_connect.issuer_id` - App Store Connect Issuer ID
- [ ] `code_signing.match_git_url` - Repository URL for match certificates
- [ ] `code_signing.match_git_branch` - Branch name for match certificates
- [ ] `code_signing.provisioning_profiles.adhoc` - AdHoc profile name
- [ ] `code_signing.provisioning_profiles.appstore` - AppStore profile name
- [ ] `firebase.app_id` - Firebase iOS app ID
- [ ] `firebase.groups` - Firebase tester groups
- [ ] `version_number` - Initial version number
- [ ] Update project/workspace paths if changed

## ✅ Step 2: Place Required Secret Files

Create a `secrets/` directory in project root and place:

### Required for Android
- [ ] `playStorePublishServiceCredentialsFile.json` - Google Play Store API credentials
- [ ] `firebaseAppDistributionServiceCredentialsFile.json` - Firebase App Distribution credentials

### Required for iOS
- [ ] `Auth_key.p8` - App Store Connect API authentication key
- [ ] `match_ci_key` - SSH private key for match repository access
- [ ] `firebaseAppDistributionServiceCredentialsFile.json` - Firebase App Distribution credentials

### Required for Code Signing
- [ ] Place your keystore file in `keystores/` directory with the name specified in config

## ✅ Step 3: Verify Configuration

Run the following to ensure everything is configured correctly:

```bash
# Check if all files are in place
ls -la secrets/
ls -la keystores/

# Validate fastlane configuration
cd fastlane
bundle install
bundle exec fastlane android assembleDebugApks  # Test Android build
bundle exec fastlane ios build_ios              # Test iOS build (macOS only)
```

## ✅ Step 4: Update .gitignore

Ensure the following are in your `.gitignore`:

```gitignore
# Secrets
secrets/
*.p8
*.json
*.keystore
*.jks

# Fastlane
fastlane/report.xml
fastlane/Preview.html
fastlane/screenshots
fastlane/test_output
fastlane/.env
```

## ✅ Step 5: Set Up CI/CD (Optional)

If using GitHub Actions, CircleCI, or similar:

- [ ] Add secrets as environment variables in CI/CD platform
- [ ] Update workflow files to use new package names
- [ ] Configure fastlane match for iOS code signing
- [ ] Test deployment lanes in CI environment

## 🔐 Security Checklist

- [ ] Never commit secret files to version control
- [ ] Use different Firebase projects for prod/demo/dev
- [ ] Rotate API keys periodically
- [ ] Use different keystores for debug and release builds
- [ ] Enable two-factor authentication on all developer accounts
- [ ] Restrict Firebase App Distribution to specific tester groups
- [ ] Use match for iOS certificate management (recommended)

## 📚 Quick Commands Reference

### Android Lanes
```bash
# Build debug APKs
bundle exec fastlane android assembleDebugApks

# Build release APKs (requires keystore)
bundle exec fastlane android assembleReleaseApks

# Deploy to Firebase App Distribution
bundle exec fastlane android deployReleaseApkOnFirebase
bundle exec fastlane android deployDemoApkOnFirebase

# Deploy to Play Store (internal track)
bundle exec fastlane android deployInternal

# Promote builds
bundle exec fastlane android promoteToBeta
bundle exec fastlane android promote_to_production
```

### iOS Lanes (macOS only)
```bash
# Build iOS app (no signing)
bundle exec fastlane ios build_ios

# Build signed iOS app
bundle exec fastlane ios build_signed_ios

# Deploy to Firebase App Distribution
bundle exec fastlane ios deploy_on_firebase

# Upload to TestFlight
bundle exec fastlane ios beta

# Submit to App Store
bundle exec fastlane ios release
```

## ❓ Common Issues

### Issue: "Keystore file not found"
**Solution:** Ensure keystore is in `keystores/` directory and path is correct in `project_config.rb`

### Issue: "Invalid Play Store credentials"
**Solution:** Check that `playStorePublishServiceCredentialsFile.json` is valid and has correct permissions

### Issue: "iOS provisioning profile not found"
**Solution:** Run `bundle exec fastlane match` to fetch/create certificates

### Issue: "Firebase upload fails"
**Solution:** Verify Firebase App IDs and ensure service account has "Firebase App Distribution Admin" role

## 🎯 Pro Tips

1. **Test locally first** - Always test fastlane lanes locally before running in CI/CD
2. **Use match for iOS** - Simplifies certificate management across team
3. **Version control** - Keep `project_config.rb` in git, but not secrets
4. **Environment-specific configs** - Use environment variables for different environments
5. **Automate versioning** - Let fastlane handle version codes/numbers automatically
6. **Release notes** - Use conventional commits for automatic release note generation

## 📞 Need Help?

- Read the [README.md](./README.md) for detailed documentation
- Check [fastlane documentation](https://docs.fastlane.tools/)
- Review the FastFile for available lanes and options
- Run `bundle exec fastlane action_name` for help on specific actions
