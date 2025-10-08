@echo off
setlocal enabledelayedexpansion

REM ################################################################################
REM Documentation Generation Script for Windows
REM
REM This script generates Dokka documentation for the KMP project.
REM
REM Usage:
REM   docs-generator\scripts\generate-docs.bat [options]
REM
REM Options:
REM   --clean        Clean previous documentation before generating
REM   --no-color     Disable colored output
REM   --help         Show this help message
REM
REM Environment Variables:
REM   OUTPUT_DIR     Directory for generated documentation (default: build/docs-output)
REM
REM Examples:
REM   docs-generator\scripts\generate-docs.bat
REM   docs-generator\scripts\generate-docs.bat --clean
REM   set OUTPUT_DIR=temp\docs && docs-generator\scripts\generate-docs.bat
REM
REM ################################################################################

REM Default options
set CLEAN=false
set NO_COLOR=false
if "%OUTPUT_DIR%"=="" set OUTPUT_DIR=build\docs-output

REM Get script directory and project root
set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..\..

REM Colors for output (Windows 10+ supports ANSI colors)
if "%NO_COLOR%"=="false" (
    set RED=[91m
    set GREEN=[92m
    set YELLOW=[93m
    set BLUE=[94m
    set NC=[0m
) else (
    set RED=
    set GREEN=
    set YELLOW=
    set BLUE=
    set NC=
)

REM ################################################################################
REM Functions
REM ################################################################################

:print_header
echo %BLUE%================================================%NC%
echo %BLUE%%~1%NC%
echo %BLUE%================================================%NC%
goto :eof

:print_success
echo %GREEN%✓ %~1%NC%
goto :eof

:print_error
echo %RED%✗ %~1%NC%
goto :eof

:print_warning
echo %YELLOW%⚠ %~1%NC%
goto :eof

:print_info
echo %BLUE%ℹ %~1%NC%
goto :eof

:show_help
findstr /B /C:"REM #" "%~f0" | findstr /V "REM #" | findstr /V "REM ################################################################################"
exit /b 0

:check_requirements
call :print_header "Checking Requirements"

REM Check if we're in the project root
if not exist "%PROJECT_ROOT%\settings.gradle.kts" (
    call :print_error "Not in a Gradle project root directory"
    exit /b 1
)
call :print_success "Found Gradle project"

REM Check for gradlew.bat
if not exist "%PROJECT_ROOT%\gradlew.bat" (
    call :print_error "gradlew.bat not found in project root"
    exit /b 1
)
call :print_success "Found gradlew.bat"

echo.
goto :eof

:clean_previous_docs
if "%CLEAN%"=="true" (
    call :print_header "Cleaning Previous Documentation"
    
    if exist "%PROJECT_ROOT%\%OUTPUT_DIR%" rmdir /s /q "%PROJECT_ROOT%\%OUTPUT_DIR%"
    if exist "%PROJECT_ROOT%\build\dokka" rmdir /s /q "%PROJECT_ROOT%\build\dokka"
    
    call :print_success "Cleaned previous documentation"
    echo.
)
goto :eof

:generate_dokka
call :print_header "Generating Dokka Documentation"

cd /d "%PROJECT_ROOT%"

call gradlew.bat dokkaGenerate -x :cmp-web:dokkaGenerate --no-configuration-cache --no-daemon --stacktrace
if errorlevel 1 (
    call :print_error "Failed to generate Dokka documentation"
    exit /b 1
)
call :print_success "Dokka documentation generated successfully"
echo.
goto :eof

:organize_documentation
call :print_header "Organizing Documentation"

if not exist "%PROJECT_ROOT%\%OUTPUT_DIR%" mkdir "%PROJECT_ROOT%\%OUTPUT_DIR%"

REM Copy Dokka documentation
if exist "%PROJECT_ROOT%\build\dokka" (
    call :print_info "Copying Dokka documentation..."
    xcopy /E /I /Y "%PROJECT_ROOT%\build\dokka\*" "%PROJECT_ROOT%\%OUTPUT_DIR%\"
    call :print_success "Dokka documentation copied"
)

REM Create .nojekyll for GitHub Pages
echo. > "%PROJECT_ROOT%\%OUTPUT_DIR%\.nojekyll"

echo.
goto :eof

:print_summary
call :print_header "Documentation Generation Complete"

echo %GREEN%Documentation generated successfully!%NC%
echo.
echo Output directory: %BLUE%%OUTPUT_DIR%%NC%
echo.
echo To view the documentation:
echo   1. Open %BLUE%%OUTPUT_DIR%\index.html%NC% in your browser
echo   2. Or run a local server:
echo      %YELLOW%cd %OUTPUT_DIR% ^&^& python -m http.server 8000%NC%
echo      Then open: %BLUE%http://localhost:8000/index.html%NC%
echo.

REM Calculate total size
for /f "tokens=3" %%a in ('dir /s /-c "%PROJECT_ROOT%\%OUTPUT_DIR%" ^| find "File(s)"') do set TOTAL_SIZE=%%a
if defined TOTAL_SIZE (
    echo Total documentation size: %BLUE%%TOTAL_SIZE% bytes%NC%
)

echo.
goto :eof

REM ################################################################################
REM Parse arguments
REM ################################################################################

:parse_args
if "%~1"=="" goto :main_execution

if "%~1"=="--clean" (
    set CLEAN=true
    shift
    goto :parse_args
)

if "%~1"=="--no-color" (
    set NO_COLOR=true
    shift
    goto :parse_args
)

if "%~1"=="--help" (
    call :show_help
    exit /b 0
)

if "%~1"=="-h" (
    call :show_help
    exit /b 0
)

call :print_error "Unknown option: %~1"
echo Use --help to see available options
exit /b 1

REM ################################################################################
REM Main execution
REM ################################################################################

:main_execution
call :print_header "KMP Project Documentation Generator"
echo.

call :check_requirements
call :clean_previous_docs
call :generate_dokka
call :organize_documentation
call :print_summary

exit /b 0
