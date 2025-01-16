<div align="center">

<img src="https://github.com/user-attachments/assets/ab2f5bf9-5b88-4fee-90e9-741e3b3f7a26" alt="Project Logo" width="150" style="margin-right: 20px;" />

<h1>KMP Multi-Module Project Generator</h1>

<p> 🚀 The Ultimate Kotlin Multiplatform Project Generator with Production-Ready Setup</p>

![Kotlin](https://img.shields.io/badge/Kotlin-7f52ff?style=flat-square&logo=kotlin&logoColor=white)
![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin%20Multiplatform-4c8d3f?style=flat-square&logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Jetpack%20Compose%20Multiplatform-000000?style=flat-square&logo=android&logoColor=white)

![badge-android](http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat)
![badge-ios](http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat)
![badge-desktop](http://img.shields.io/badge/platform-desktop-DB413D.svg?style=flat)
![badge-js](http://img.shields.io/badge/platform-web-FDD835.svg?style=flat)

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![GitHub license](https://img.shields.io/github/license/Naereen/StrapDown.js.svg)](https://github.com/openMF/kmp-project-template/blob/development/LICENSE)
[![GitHub release](https://img.shields.io/github/release/Naereen/StrapDown.js.svg)](https://github.com/openMF/kmp-project-template/releases/)
[![GitHub issues](https://img.shields.io/github/issues/Naereen/StrapDown.js.svg)](https://github.com/openMF/kmp-project-template/issues/)

[![Pr Checks](https://github.com/openMF/kmp-project-template/actions/workflows/pr-check.yml/badge.svg)](https://github.com/openMF/kmp-project-template/actions/workflows/pr-check.yml)
[![Slack](https://img.shields.io/badge/Slack-4A154B?style=flat-square&logo=slack&logoColor=white)](https://join.slack.com/t/mifos/shared_invite/zt-2wvi9t82t-DuSBdqdQVOY9fsqsLjkKPA)
[![Jira](https://img.shields.io/badge/jira-%230A0FFF.svg?style=flat-square&logo=jira&logoColor=white)](https://mifosforge.jira.com/jira/software/c/projects/MM/issues/?filter=allissues&jql=project%20%3D%20%22MM%22%20ORDER%20BY%20created%20DESC)

</div>


> \[!Note]
>
> This branch is designed for partial customized projects. Running the `customizer.sh` script doesn't rename any application module, instead it'll change all `core` and `feature` module namespaces, packages, and other related configurations accordingly.
> and change your android and desktop application id and namespace in `libs.versions.toml` file.
> 
> For full customization, please use the `full-customizable` branch instead.

## 🌟 Key Features

### 📱 Cross-Platform Support
- **Android**: Native Android app with Jetpack Compose
- **iOS**: Native iOS app with SwiftUI integration
- **Desktop**: JVM-based desktop application
- **Web**: Kotlin/JS with Compose Web
- **Shared Logic**: Common business logic across all platforms

### 🏗️ Architecture & Structure
- **Multi-Module Design**: Organized, scalable architecture
- **Clean Architecture**: Separation of concerns with domain, data, and presentation layers
- **Feature-First Modularization**: Independent feature modules for better maintainability
- **Shared UI Components**: Reusable Compose Multiplatform components
- **Platform-Specific Optimizations**: Native capabilities while maximizing code sharing

### 🛠️ Development Tools
- **Gradle Kotlin DSL**: Modern build configuration
- **Version Catalogs**: Centralized dependency management
- **Type-Safe Accessors**: Improved build script maintainability
- **Custom Gradle Plugins**: Streamlined build process
- **Run Configurations**: Pre-configured for all platforms

### 🔍 Code Quality
- **Static Analysis**: Detekt for code quality checks
- **Code Formatting**: Spotless integration
- **Git Hooks**: Automated pre-commit checks
- **Style Guide Enforcement**: Consistent coding standards
- **Automated Testing**: Unit and integration test setup

### 📦 CI/CD Integration
- **GitHub Actions**: Automated build and test workflows
- **PR Checks**: Automated pull request validation
- **Fastlane Integration**: Automated deployment for Android and iOS
- **Dynamic Versioning**: Automated version management
- **Release Notes Generation**: Automated changelog creation

### 🎨 UI/UX Components
- **Design System**: Consistent styling across platforms
- **Theme Support**: Light/dark mode compatibility
- **UI Components**: Pre-built, customizable widgets
- **Resources Management**: Efficient asset handling
- **Accessibility**: Built-in accessibility support

### 💾 Data Management
- **DataStore Integration**: Efficient local storage
- **Network Layer**: API client setup
- **Caching Strategy**: Optimized data caching
- **Analytics Integration**: Ready-to-use analytics setup
- **Error Handling**: Comprehensive error management

### 🔄 Sync Capabilities
- **Automated Directory Sync**: Keep your CMP directories in sync with upstream
- **GitHub Actions Integration**: Automated weekly sync workflow
- **Manual Sync Script**: Easy-to-use bash script for manual syncing
- **Selective Sync**: Only sync specific CMP directories:
    - cmp-android
    - cmp-desktop
    - cmp-ios
    - cmp-web
- **Pull Request Generation**: Automated PRs for sync changes
- **Change Validation**: Ensures only necessary updates are committed

### Using the Sync Script

#### Manual Sync
```bash
# Run the sync script
./sync-cmp-dirs.sh
```

#### Automated GitHub Workflow
The repository includes a GitHub workflow (`sync-cmp-dirs.yml`) that:
- Runs automatically every Monday at midnight UTC
- Can be triggered manually from GitHub Actions UI
- Creates pull requests for review when changes are detected
- Includes detailed change logs in PR description

## 🚀 Quick Start

### Prerequisites
- Bash 4.0+
- Unix-like environment (macOS, Linux) or Git Bash on Windows
- Android Studio/IntelliJ IDEA
- Xcode (for iOS development)
- Node.js (for web development)

### Installation

1. **Clone the Repository**
```bash
git clone https://github.com/openMF/kmp-project-template.git
cd kmp-project-template
```

2. **Run the Customizer**
```bash
./customizer.sh org.example.myapp MyKMPProject
```

3. **Build and Run**
```bash
./gradlew build
```

## 📁 Project Structure

### Core Modules
```
core/
├── analytics/    # Analytics and tracking
├── common/       # Shared utilities and extensions
├── data/         # Data layer implementation
├── datastore/    # Local storage management
├── domain/       # Business logic and use cases
├── model/        # Data models and entities
├── network/      # Network communication
├── ui/           # Shared UI components
└── designsystem/ # Design system components
```

### Feature Modules
```
feature/
├── home/        # Home screen features
├── profile/     # User profile management
└── settings/    # App settings
```

### Platform-Specific
```
cmp-android/     # Android app
cmp-ios/         # iOS app
cmp-desktop/     # Desktop app
cmp-web/         # Web app
cmp-shared/      # Shared code
```

## 🔄 Module Dependencies

```mermaid
graph TD
    A[Project Root] --> B[buildLogic]
    A --> C[core]
    A --> D[feature]
    A --> E[Platform Modules]
    
    C --> C1[common]
    C --> C2[model]
    C --> C3[data]
    C --> C4[network]
    C --> C5[domain]
    C --> C6[ui]
    C --> C7[designsystem]
    C --> C8[datastore]
    
    D --> D1[home]
    D --> D2[profile]
    D --> D3[settings]
    
    E --> E1[cmp-android]
    E --> E2[cmp-ios]
    E --> E3[cmp-desktop]
    E --> E4[cmp-web]
    E --> E5[cmp-shared]
```
## 🔄 Staying in Sync with Upstream

### Manual Sync
1. Use the provided `sync-cmp-dirs.sh` script to sync specific CMP directories
2. Review changes before committing
3. Push changes to your repository

### Automated Sync
1. The GitHub workflow automatically syncs directories weekly
2. Review and merge the generated pull requests
3. Manual sync can be triggered from GitHub Actions

## 🤝 Contributing

We welcome contributions! Here's how you can help:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a pull request

## 📚 Documentation
> Documentation is a work in progress.

- [Setup Guide](docs/SETUP.md)
- [Architecture Overview](docs/ARCHITECTURE.md)
- [Contributing Guidelines](CONTRIBUTING.md)
- [Code Style Guide](docs/STYLE_GUIDE.md)

## 📫 Support

- Join our [Slack channel](https://join.slack.com/t/mifos/shared_invite/zt-2wvi9t82t-DuSBdqdQVOY9fsqsLjkKPA)
- Report issues on [GitHub](https://github.com/openMF/kmp-project-template/issues)
- Track progress on [Jira](https://mifosforge.jira.com/jira/software/c/projects/MM/issues/?filter=allissues&jql=project%20%3D%20%22MM%22%20ORDER%20BY%20created%20DESC)

## 📄 License

This project is licensed under the [Mozilla Public License 2.0](LICENSE).