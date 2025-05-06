# Project Sync Tools

This repository contains tools to synchronize project directories and files from an upstream repository while preserving local customizations. These tools help maintain consistency with an upstream template repository while allowing for project-specific modifications.

## Table of Contents

- [Overview](#overview)
- [Sync Directories Script](#sync-directories-script)
    - [Features](#features)
    - [Usage](#usage)
    - [Options](#options)
    - [Exclusion System](#exclusion-system)
    - [Examples](#examples)
- [GitHub Workflow](#github-workflow)
    - [Workflow Features](#workflow-features)
    - [Configuration](#configuration)
    - [Scheduling](#scheduling)
    - [Manual Trigger](#manual-trigger)
- [Technical Details](#technical-details)
    - [How Exclusions Work](#how-exclusions-work)
    - [Branching Strategy](#branching-strategy)

## Overview

When working with a multi-module project based on a template, it's often necessary to stay in sync with upstream changes while maintaining project-specific customizations. These tools automate this process by:

1. Fetching the latest changes from the upstream repository
2. Applying these changes to your project
3. Preserving specific files and directories that should not be overwritten
4. Creating a separate branch with these changes for review
5. Optionally creating a pull request (in the GitHub workflow)

## Sync Directories Script

The `sync-dirs.sh` script is a Bash utility that synchronizes directories and files from an upstream repository while preserving specified exclusions.

### Features

- Syncs multiple directories and files with a single command
- Preserves specified files and directories from being overwritten
- Supports exclusions at both directory and root levels
- Creates a dedicated branch for synced changes
- Interactive confirmation for pushing changes
- Dry-run option to preview changes without applying them

### Usage

```bash
./sync-dirs.sh [options]
```

### Options

| Option        | Description                                   |
|---------------|-----------------------------------------------|
| `-h, --help`  | Display help information and exit             |
| `--dry-run`   | Show what would be done without making changes|
| `-f, --force` | Skip confirmation prompts and proceed automatically |

### Exclusion System

The script supports two types of exclusions:

1. **Directory-level exclusions**: Files and directories within specific project directories
2. **Root-level exclusions**: Files in the root of the project

Exclusions are defined in the `EXCLUSIONS` associative array:

```bash
declare -A EXCLUSIONS=(
    ["cmp-android"]="src/main/res:dir dependencies:dir src/main/ic_launcher-playstore.png:file google-services.json:file"
    ["cmp-web"]="src/jsMain/resources:dir src/wasmJsMain/resources:dir"
    ["cmp-desktop"]="icons:dir"
    ["cmp-ios"]="iosApp/Assets.xcassets:dir"
    ["root"]="secrets.env:file"
)
```

Format for exclusions:
- Directory exclusions: `path/to/exclude:dir`
- File exclusions: `path/to/exclude:file`
- Root-level files: Add to the "root" key

### Examples

**Basic usage:**
```bash
./sync-dirs.sh
```

**Dry run to preview changes:**
```bash
./sync-dirs.sh --dry-run
```

**Non-interactive execution:**
```bash
./sync-dirs.sh --force
```

**Display help:**
```bash
./sync-dirs.sh --help
```

## GitHub Workflow

The repository includes a GitHub Actions workflow (`sync-cmp-directories.yml`) that automates the synchronization process in CI/CD environments.

### Workflow Features

- Automatically syncs directories and files from the upstream repository
- Preserves excluded files and directories
- Creates a pull request with the changes
- Can be scheduled or manually triggered
- Detailed PR description with all synchronized items

### Configuration

The workflow is configured to:

1. Check out your repository
2. Set up Git configuration
3. Add and fetch from the upstream repository
4. Create temporary branches for the sync process
5. Sync directories and files while preserving exclusions
6. Create a pull request with the changes

### Scheduling

By default, the workflow is scheduled to run weekly:

```yaml
schedule:
  - cron: '0 0 * * 1'  # Runs at 00:00 on Monday
```

### Manual Trigger

The workflow can also be triggered manually with optional parameters:

```yaml
workflow_dispatch:
  inputs:
    upstream:
      description: 'Upstream repository to sync directories from'
      default: 'https://github.com/openMF/kmp-project-template.git'
      required: true
      type: string
```

## Technical Details

### How Exclusions Work

Both tools use a similar approach to handle exclusions:

1. **Preservation Phase**: Before syncing, excluded files and directories are copied to a temporary location
2. **Sync Phase**: Changes from the upstream repository are applied
3. **Restoration Phase**: Excluded files and directories are restored from the temporary location

For root-level exclusions, the process is similar but adapted to handle files in the project root.

### Branching Strategy

The sync process uses a dedicated branching strategy:

1. A new branch is created based on your main development branch (default: `dev`)
2. A temporary branch is created from the upstream repository
3. Changes are synchronized from the temporary branch to the sync branch
4. The temporary branch is deleted after the sync
5. The sync branch can be pushed for review and merged

This approach keeps the sync changes isolated and provides an opportunity for review before merging.