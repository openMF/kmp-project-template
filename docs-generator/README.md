# Documentation Generator

> **Note**: This is a documentation generation template for projects created from this KMP template. Follow the setup instructions below to enable documentation generation in your project.

## Quick Setup (For New Projects)

### 1. Add Dokka to Version Catalog

Edit `gradle/libs.versions.toml`:

**In `[versions]` section (after Static Analysis section), add:**
```toml
# Documentation
dokka = "2.0.0"
```

**In `[plugins]` section (before Room Plugin), add:**
```toml
# Documentation
dokka = { id = "org.jetbrains.dokka", version.ref = "dokka" }
```

### 2. Apply Dokka in Root Build

Edit `build.gradle.kts`:

**In plugins block, add:**
```kotlin
alias(libs.plugins.dokka) apply false
```

**At end of file, add:**
```kotlin
// Apply Dokka configuration for documentation generation
apply(from = "docs-generator/dokka-config/dokka.gradle.kts")
```

### 3. Customize for Your Project

Edit `docs-generator/dokka-config/dokka.gradle.kts` and replace:
- `YOUR_USERNAME/YOUR_REPO` with your actual GitHub repository

### 4. Generate Documentation

**Linux/macOS:**
```bash
./docs-generator/scripts/generate-docs.sh
```

**Windows:**
```cmd
docs-generator\scripts\generate-docs.bat
```

Documentation output: `build/docs-output/`

## What's Included

```
docs-generator/
├── dokka-config/           # Dokka configuration templates
├── scripts/                # Generation and publishing scripts
└── README.md              # This file

.github/workflows/
└── docs-generate.yaml      # CI/CD automation
```

## Optional: Automated Publishing

If you plan to publish to a separate documentation repository, add a custom workflow or script for that purpose. This template deploys via the `docs-website` project and GitHub Pages.

## Available Scripts

**Linux/macOS:**
```bash
# Generate documentation
./docs-generator/scripts/generate-docs.sh

# Generate with cleanup
./docs-generator/scripts/generate-docs.sh --clean

# Sync docs to website static folder
./docs-generator/scripts/sync-dokka-to-static-api.sh
```

**Windows:**
```cmd
# Generate documentation
docs-generator\scripts\generate-docs.bat

# Generate with cleanup
docs-generator\scripts\generate-docs.bat --clean

# Sync docs to website static folder
docs-generator\scripts\sync-dokka-to-static-api.bat
```

## If You Don't Need Documentation

Delete these:
- `docs-generator/` folder
- `.github/workflows/docs-generate.yaml`

## Documentation Best Practices

1. Add KDoc comments to all public APIs
2. Include usage examples in documentation
3. Link related APIs using `@see` tags
4. Test documentation locally before publishing

## Troubleshooting

**Docs not generating?**
- Ensure Dokka plugin is applied in `build.gradle.kts`
- Run with: `./gradlew dokkaHtmlMultiModule --stacktrace`

**Publishing fails?**
- Check `DOCS_DEPLOY_TOKEN` secret is set
- Verify token has `repo` permissions
- Confirm docs repository URL is correct

## Further Reading

- [Dokka Documentation](https://kotlinlang.org/docs/dokka-introduction.html)
- [KDoc Syntax](https://kotlinlang.org/docs/kotlin-doc.html)
- [GitHub Pages](https://docs.github.com/en/pages)

