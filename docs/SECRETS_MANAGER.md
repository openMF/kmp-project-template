# Secrets Manager

A comprehensive bash script for managing Android keystores, automating the signing configuration,
and handling GitHub secrets.

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Key Features](#key-features)
- [Usage](#usage)
    - [Generating Keystores](#generating-keystores)
    - [Viewing Secrets](#viewing-secrets)
    - [Adding Secrets to GitHub](#adding-secrets-to-github)
    - [Listing GitHub Secrets](#listing-github-secrets)
    - [Deleting GitHub Secrets](#deleting-github-secrets)
    - [Deleting All GitHub Secrets](#deleting-all-github-secrets)
- [Configuration](#configuration)
    - [Environment File (secrets.env)](#environment-file-secretsenv)
    - [Files Updated by Script](#files-updated-by-script)
- [Directory Structure](#directory-structure)
- [Troubleshooting](#troubleshooting)
- [FAQ](#faq)

## Overview

The Android Keystore Manager automates the process of creating, storing, and configuring Android app
signing keys. It can:

- Generate debug (ORIGINAL) and release (UPLOAD) keystores with custom information
- Securely store keystore configurations and passwords
- Update fastlane and Gradle build configurations automatically
- Manage GitHub secrets for CI/CD pipelines
- Provide a clear view of all managed secrets

This tool is especially useful for development teams that need to maintain consistent signing
configurations across multiple environments and CI/CD pipelines.

## Installation

1. Make it executable:
   ```bash
   chmod +x keystore-manager.sh
   ```

2. Ensure you have the required dependencies:
    - Java Development Kit (JDK) for keytool
    - GitHub CLI (gh) for GitHub secrets management

## Key Features

- **Dual Keystore Management**: Creates and maintains both debug (ORIGINAL) and release (UPLOAD)
  keystores
- **Automated Configuration**: Updates Gradle and fastlane configurations automatically
- **GitHub Secrets Integration**: Easily manage secrets for CI/CD pipelines
- **Secret Visualization**: View all secrets in a nicely formatted table
- **Secure Storage**: Keystores are stored in a separate 'keystores' directory
- **Flexible Certificate Information**: Support for custom company and organization details

## Usage

### Generating Keystores

```bash
./keystore-manager.sh generate
```

This command:

1. Creates the `keystores` directory if it doesn't exist
2. Generates both ORIGINAL (debug) and UPLOAD (release) keystores
3. Updates `secrets.env` with base64-encoded keystores
4. Updates fastlane config in `fastlane-config/android_config.rb`
5. Updates Gradle config in `cmp-android/build.gradle.kts`

### Viewing Secrets

```bash
./keystore-manager.sh view
```

Displays all secrets from `secrets.env` in a nicely formatted table. For multiline values (like
base64-encoded keystores), it shows `[MULTILINE VALUE]` instead of the actual content.

### Adding Secrets to GitHub

```bash
./keystore-manager.sh add --repo=username/repo
```

Adds all secrets from `secrets.env` to the specified GitHub repository (excludes certificate
information and local configuration values).

You can target a specific GitHub environment:

```bash
./keystore-manager.sh add --repo=username/repo --env=production
```

### Listing GitHub Secrets

```bash
./keystore-manager.sh list --repo=username/repo
```

Lists all secrets currently stored in the specified GitHub repository.

### Deleting GitHub Secrets

```bash
./keystore-manager.sh delete --repo=username/repo --name=SECRET_NAME
```

Deletes a specific secret from the GitHub repository.

### Deleting All GitHub Secrets

```bash
./keystore-manager.sh delete-all --repo=username/repo
```

Deletes all secrets defined in `secrets.env` from the GitHub repository (excluding certificate
information and local configuration values).

To also delete excluded secrets:

```bash
./keystore-manager.sh delete-all --repo=username/repo --include-excluded
```

## Configuration

### Environment File (secrets.env)

The `secrets.env` file contains all the keystore and secret information. Example structure:

```
# GitHub Secrets Environment File
# Format: KEY=VALUE (use quotes for values with spaces)
# Use <<EOF and EOF for multiline values

# ORIGINAL Keystore credentials (Debug/Development)
ORIGINAL_KEYSTORE_FILE_PASSWORD=original_keystore_password
ORIGINAL_KEYSTORE_ALIAS=original_key
ORIGINAL_KEYSTORE_ALIAS_PASSWORD=original_key_password

# UPLOAD Keystore credentials (Release/Production)
UPLOAD_KEYSTORE_FILE_PASSWORD=upload_keystore_password
UPLOAD_KEYSTORE_ALIAS=upload_key
UPLOAD_KEYSTORE_ALIAS_PASSWORD=upload_key_password

# Local keystore generation settings (not sent to GitHub)
ORIGINAL_KEYSTORE_NAME=original_keystore.keystore
UPLOAD_KEYSTORE_NAME=upload_keystore.keystore
VALIDITY=25
KEYALG=RSA
KEYSIZE=2048
OVERWRITE=false

# Certificate information (Distinguished Name)
# IMPORTANT: Use quotes for values with spaces
COMPANY_NAME="Devikon Inc."
DEPARTMENT="Mobile Development"
ORGANIZATION="Devikon Inc."
CITY="Kolkata"
STATE="West Bengal"
COUNTRY=IN

# Base64 encoded keystores (added by the script)
ORIGINAL_KEYSTORE_FILE<<EOF
MIICXAIBAAKBgQDCFENGw33yGihy92pDjZQhl0C36rPJj+...
EOF

UPLOAD_KEYSTORE_FILE<<EOF
MIICXQIBAAKBgQDASAEDHGOPsVNlHCYi6ofmoEOdG+7xDRa...
EOF

# Other secrets
API_KEY=your_api_key_here
```

### Files Updated by Script

The script automatically updates several configuration files:

1. **fastlane-config/android_config.rb**:
   ```ruby
   module FastlaneConfig
     module AndroidConfig
       STORE_CONFIG = {
         default_store_file: "upload.keystore",
         default_store_password: "upload_keystore_password",
         default_key_alias: "upload_key",
         default_key_password: "upload_key_password"
       }
       # ... other configurations ...
     end
   end
   ```

2. **cmp-android/build.gradle.kts**:
   ```kotlin
   android {
     signingConfigs {
       create("release") {
         storeFile = file(System.getenv("KEYSTORE_PATH") ?: "../keystores/upload.keystore")
         storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "upload_keystore_password"
         keyAlias = System.getenv("KEYSTORE_ALIAS") ?: "upload_key"
         keyPassword = System.getenv("KEYSTORE_ALIAS_PASSWORD") ?: "upload_key_password"
         enableV1Signing = true
         enableV2Signing = true
       }
     }
   }
   ```

## Directory Structure

After running the script, you should have the following directory structure:

```
project/
├── keystore-manager.sh        # The main script
├── secrets.env                # Environment file with secrets
├── keystores/                 # Directory containing keystore files
│   ├── original.keystore      # Debug/development keystore
│   └── upload.keystore        # Release/production keystore
├── fastlane-config/           # Fastlane configuration directory
│   └── android_config.rb      # Fastlane configuration for Android
└── cmp-android/              
    └── build.gradle.kts       # Gradle build file with signing config
```

## Troubleshooting

### Common Issues

1. **Error: keytool command not found**
    - Make sure you have JDK installed and keytool is in your PATH
    - Install JDK if needed: `sudo apt install default-jdk`

2. **GitHub CLI (gh) is not installed**
    - Install GitHub
      CLI: [https://cli.github.com/manual/installation](https://cli.github.com/manual/installation)
    - Log in with: `gh auth login`

3. **Error with multiline values**
    - Ensure all multiline blocks in `secrets.env` are properly closed with `EOF`
    - Check for proper formatting: `KEY<<EOF` and `EOF` should be on separate lines

4. **Configuration files not updated**
    - Check if the files exist at the expected paths
    - Verify file permissions: `chmod +rw path/to/file`

### Verifying Keystores

To verify that a keystore was generated correctly:

```bash
keytool -list -v -keystore keystores/original.keystore -storepass your_password
```

## FAQ

**Q: What's the difference between ORIGINAL and UPLOAD keystores?**  
A: ORIGINAL (debug) keystores are for development/testing, while UPLOAD (release) keystores are for
production builds that get published to the Play Store.

**Q: Will my certificate information be uploaded to GitHub?**  
A: No. Certificate information (COMPANY_NAME, DEPARTMENT, etc.) is excluded from GitHub secrets by
default.

**Q: How do I change the keystore passwords?**  
A: Edit the values in `secrets.env` and run `./keystore-manager.sh generate` with the OVERWRITE
option set to true.

**Q: Can I use this script in CI/CD pipelines?**  
A: Yes, the script is designed to work in both interactive and automated environments. For CI/CD,
you would typically use the `add` command to upload secrets.

**Q: How are my keystore files secured?**  
A: Keystore files are stored in the `keystores` directory and never directly uploaded to GitHub.
Only base64-encoded versions are added as GitHub secrets.

**Q: Do I need to manually update Gradle and fastlane configurations?**  
A: No, the script automatically updates both configurations when you run the `generate` command.