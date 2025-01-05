# KMP Multi-Module Project Generator

A bash script to customize and rename Kotlin Multiplatform projects. This tool helps you quickly adapt the template project structure with your own package name and project settings.

## Features

- Renames all modules with your project name
- Updates package names throughout the codebase
- Handles Kotlin Multiplatform specific source sets
- Updates Gradle build files and settings
- Renames files and composable functions
- Updates run configurations
- Handles convention plugins
- Updates import statements
- Preserves project structure and functionality

## Prerequisites

- Bash version 4 or higher
- Unix-like environment (Linux/macOS)
- For macOS users: You may need to install a newer version of bash using Homebrew:
  ```bash
  brew install bash
  ```

## Usage

1. Copy the `customizer.sh` script to your project root directory
2. Make it executable:
   ```bash
   chmod +x customizer.sh
   ```
3. Run the script with your desired package name and project name:
   ```bash
   ./customizer.sh com.example.myapp MyProject
   ```

### Parameters

- `package_name`: Your desired package name (e.g., com.example.myapp)
- `project_name`: Your project name in PascalCase (e.g., MyProject)
- `application_name`: (Optional) Custom application class name

### Example

```bash
./customizer.sh com.company.awesomeapp AwesomeApp
```

This will:
1. Rename all modules from `mifos-*` to `awesomeapp-*`
2. Update package from `org.mifos` to `com.company.awesomeapp`
3. Rename files like `MifosApp.kt` to `AwesomeApp.kt`
4. Update all relevant configurations and references

## What Gets Updated

1. **Module Names**
    - `mifos-shared` → `yourproject-shared`
    - `mifos-android` → `yourproject-android`
    - `mifos-desktop` → `yourproject-desktop`
    - `mifos-web` → `yourproject-web`

2. **Package Names**
    - Updates all package declarations
    - Updates all import statements
    - Updates AndroidManifest.xml

3. **Build Files**
    - settings.gradle.kts
    - build.gradle.kts files
    - Convention plugins
    - Version catalog entries

4. **Run Configurations**
    - Updates configuration names
    - Updates module references
    - Renames configuration files

5. **Source Files**
    - Renames files with "Mifos" prefix
    - Updates composable function names
    - Updates class names and references

## Project Structure
```mermaid
graph TD
    A[Project Root] --> B[build-logic]
    A --> C[mifos-shared]
    A --> D[mifos-android]
    A --> E[mifos-desktop]
    A --> F[mifos-web]
    A --> G[.idea]
    
    B --> B1[convention plugins]
    B --> B2[build-logic.versions.toml]
    
    C --> C1[src/commonMain]
    C --> C2[src/commonTest]
    
    D --> D1[src/androidMain]
    D --> D2[src/androidTest]
    
    E --> E1[src/desktopMain]
    E --> E2[src/desktopTest]
    
    F --> F1[src/webMain]
    F --> F2[src/webTest]
    
    G --> G1[runConfigurations]
    G1 --> G2[mifos-android.run.xml]
    G1 --> G3[mifos-desktop.run.xml]
    
    classDef module fill:#e1f5fe,stroke:#FFFFFF
    classDef config fill:#fff3e0,stroke:#FFFFFF
    classDef source fill:#e8f5e9,stroke:#1b5e20
    
    class B,C,D,E,F module
    class B1,B2,G1,G2,G3 config
    class C1,C2,D1,D2,E1,E2,F1,F2 source
```

## File Modified By Script
```mermaid
graph LR
    A[Script Start] --> B[Update Package Names]
    B --> C[Rename Modules]
    C --> D[Update Build Files]
    D --> E[Update Source Files]
    E --> F[Update Configurations]
    
    subgraph "Package Names"
        B1[Kotlin Files]
        B2[Android Manifest]
        B3[Import Statements]
    end
    
    subgraph "Build Files"
        D1[settings.gradle.kts]
        D2[build.gradle.kts]
        D3[Convention Plugins]
        D4[Version Catalogs]
    end
    
    subgraph "Source Files"
        E1[Rename Mifos*.kt]
        E2[Update Composables]
        E3[Update References]
    end
    
    subgraph "Configurations"
        F1[Run Configs]
        F2[Workspace]
        F3[IDE Settings]
    end
    
    B --> B1 & B2 & B3
    D --> D1 & D2 & D3 & D4
    E --> E1 & E2 & E3
    F --> F1 & F2 & F3
    
    classDef step fill:#e1f5fe,stroke:#01579b
    classDef file fill:#e8f5e9,stroke:#1b5e20
    
    class A,B,C,D,E,F step
    class B1,B2,B3,D1,D2,D3,D4,E1,E2,E3,F1,F2,F3 file
```