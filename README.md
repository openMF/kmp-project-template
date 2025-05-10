<div align="center">

<img src="https://github.com/user-attachments/assets/ab2f5bf9-5b88-4fee-90e9-741e3b3f7a26" alt="Project Logo" width="150" style="margin-right: 20px;" />

<h1>KMP Multi-Module Project Generator</h1>

<p>🚀 The Ultimate Kotlin Multiplatform Project Generator with Production-Ready Setup</p>

![Kotlin](https://img.shields.io/badge/Kotlin-7f52ff?style=flat-square&logo=kotlin&logoColor=white)
![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin%20Multiplatform-4c8d3f?style=flat-square&logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Jetpack%20Compose%20Multiplatform-000000?style=flat-square&logo=android&logoColor=white)

![badge-android](http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat)
![badge-ios](http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat)
![badge-desktop](http://img.shields.io/badge/platform-desktop-DB413D.svg?style=flat)
![badge-js](http://img.shields.io/badge/platform-web-FDD835.svg?style=flat)

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![GitHub license](https://img.shields.io/github/license/Naereen/StrapDown.js.svg)](https://github.com/openMF/kmp-project-template/blob/development/LICENSE)
[![Pr Checks](https://github.com/openMF/kmp-project-template/actions/workflows/pr-check.yml/badge.svg)](https://github.com/openMF/kmp-project-template/actions/workflows/pr-check.yml)
[![Slack](https://img.shields.io/badge/Slack-4A154B?style=flat-square&logo=slack&logoColor=white)](https://join.slack.com/t/mifos/shared_invite/zt-2wvi9t82t-DuSBdqdQVOY9fsqsLjkKPA)

</div>

> [!Note]
>
> This branch is designed for partial customized projects. Running the `customizer.sh` script
> doesn't rename any application module, instead it'll change all `core` and `feature` module
> namespaces, packages, and other related configurations accordingly.
>
> For full customization, please use the `full-customizable` branch instead.

## 🌟 Key Features

- **Cross-Platform Support**: Android, iOS, Desktop, and Web applications from a single codebase
- **Multi-Module Architecture**: Clean, organized, and scalable project structure
- **Advanced Source Set Hierarchy**: Sophisticated code sharing structure with logical platform
  groupings
- **Pre-configured CI/CD**: GitHub Actions workflows for building, testing, and deployment
- **Code Quality Tools**: Static analysis and formatting tools pre-configured
- **Sync Capabilities**: Tools to stay in sync with upstream template changes
- **Secrets Management**: Secure handling of keystores and sensitive information

## 🚀 Getting Started

### Prerequisites

- Bash 4.0+
- Unix-like environment (macOS, Linux) or Git Bash on Windows
- Android Studio/IntelliJ IDEA
- Xcode (for iOS development)
- Node.js (for web development)

### Quick Start

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

The project follows a modular architecture:

- **Platform Modules**: `cmp-android`, `cmp-ios`, `cmp-desktop`, `cmp-web`, etc.
- **Core Modules**: Common, reusable components shared across all features
- **Feature Modules**: Self-contained feature implementations
- **Build Logic**: Custom Gradle plugins and build configuration

## 📚 Documentation

Our project includes comprehensive documentation to help you get started and understand the
architecture:

- [ ] [Setup Guide](docs/SETUP.md) - Detailed instructions for setting up your development
  environment
- [ ] [Architecture Overview](docs/ARCHITECTURE.md) - Explanation of the project's structure and
  design patterns
- [ ] [Code Style Guide](docs/STYLE_GUIDE.md) - Coding conventions and best practices
- [ ] [Source Set Hierarchy](docs/SOURCE_SET_HIERARCHY.md) - Guide to the Kotlin Multiplatform code
  sharing structure
- [ ] [Sync Script](docs/SYNC_SCRIPT.md) - Information about keeping in sync with upstream changes
- [ ] [Secrets Manager](docs/SECRETS_MANAGER.md) - Documentation for the keystore and secrets
  management system
- [ ] [Fastlane Configuration](docs/FASTLANE_CONFIGURATION.md) - Guide to automating deployments with fastlane

> Documentation is continuously improving. Check back for updates or contribute to enhancing our
> docs!

## 🤝 Contributing

We welcome contributions to improve the project template! Here's how you can help:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a pull request

Please follow our [Contributing Guidelines](CONTRIBUTING.md) for detailed information.

## 📫 Support

- Join
  our [Slack channel](https://join.slack.com/t/mifos/shared_invite/zt-2wvi9t82t-DuSBdqdQVOY9fsqsLjkKPA)
- Report issues on [GitHub](https://github.com/openMF/kmp-project-template/issues)
- Track progress on [Jira](https://mifosforge.jira.com/jira/software/c/projects/KMPPT/boards/63)

## 📄 License

This project is licensed under the [Mozilla Public License 2.0](LICENSE)