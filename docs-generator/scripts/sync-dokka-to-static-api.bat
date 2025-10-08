@echo off
setlocal enabledelayedexpansion

REM Sync all Dokka HTML outputs into docs-website/static/api for local viewing
REM This script is safe to run repeatedly. It will mirror outputs per-module.

REM Get script directory and project root
set SCRIPT_DIR=%~dp0
set REPO_ROOT_DIR=%SCRIPT_DIR%..\..
set DEST_DIR=%REPO_ROOT_DIR%\docs-website\static\api

echo Repository root: %REPO_ROOT_DIR%
echo Destination directory: %DEST_DIR%

REM Create destination directory
if not exist "%DEST_DIR%" mkdir "%DEST_DIR%"
echo. > "%DEST_DIR%\.gitkeep" 2>nul

echo Scanning for Dokka HTML outputs...

REM 1) Subproject Dokka outputs: */build/dokka/html (recursive search)
set SUBMODULE_COUNT=0
for /f "delims=" %%i in ('dir /s /b /ad "%REPO_ROOT_DIR%\*build\dokka\html" 2^>nul') do (
    if exist "%%i" (
        set /a SUBMODULE_COUNT+=1
        set SUBMODULE_DIR[!SUBMODULE_COUNT!]=%%i
        
        REM Extract module name by going up 3 levels from dokka/html/build/module
        set "temp_path=%%i"
        set "temp_path=!temp_path:\build\dokka\html=!"
        set "temp_path=!temp_path:%REPO_ROOT_DIR%\=!"
        for %%j in ("!temp_path!") do set MODULE_NAME[!SUBMODULE_COUNT!]=%%~nxj
    )
)

if %SUBMODULE_COUNT% gtr 0 (
    echo Found %SUBMODULE_COUNT% submodule Dokka directories
    for /l %%j in (1,1,%SUBMODULE_COUNT%) do (
        set "dokka_dir=!SUBMODULE_DIR[%%j]!"
        set "module_name=!MODULE_NAME[%%j]!"
        set "target=%DEST_DIR%\!module_name!"
        echo Syncing module '!module_name!' from !dokka_dir! -^> !target!
        
        REM Check if source directory has content
        set "has_content=0"
        for /f %%k in ('dir /b "!dokka_dir!" 2^>nul ^| find /c /v ""') do (
            if %%k gtr 0 set "has_content=1"
        )
        
        if !has_content! equ 1 (
            REM Remove existing content and copy new content
            if exist "!target!" rmdir /s /q "!target!"
            mkdir "!target!"
            xcopy /E /I /Y "!dokka_dir!\*" "!target!\" >nul 2>&1
            if !errorlevel! equ 0 (
                echo   Successfully synced to: !target!
            ) else (
                echo   Warning: Failed to sync !module_name!
            )
        ) else (
            echo   Skipping empty directory: !dokka_dir!
        )
    )
) else (
    echo No submodule Dokka directories found (*/build/dokka/html).
)

REM 2) Root multi-module Dokka output (per current Dokka config): build/docs/html
set ROOT_MULTI_DIR=%REPO_ROOT_DIR%\build\docs\html
if exist "%ROOT_MULTI_DIR%" (
    echo Syncing root multi-module docs from %ROOT_MULTI_DIR% -^> %DEST_DIR%\root
    
    REM Check if source directory has content
    set "has_content=0"
    for /f %%k in ('dir /b "%ROOT_MULTI_DIR%" 2^>nul ^| find /c /v ""') do (
        if %%k gtr 0 set "has_content=1"
    )
    
    if !has_content! equ 1 (
        REM Remove existing content and copy new content
        if exist "%DEST_DIR%\root" rmdir /s /q "%DEST_DIR%\root"
        mkdir "%DEST_DIR%\root"
        xcopy /E /I /Y "%ROOT_MULTI_DIR%\*" "%DEST_DIR%\root\" >nul 2>&1
        if !errorlevel! equ 0 (
            echo   Successfully synced root docs
        ) else (
            echo   Warning: Failed to sync root docs
        )
    ) else (
        echo   Skipping empty root docs directory: %ROOT_MULTI_DIR%
    )
) else (
    echo Root multi-module docs directory not found at %ROOT_MULTI_DIR% (skipping).
)

REM 3) Legacy/alternative root output (some tasks produce build/dokka/html)
set LEGACY_ROOT_DIR=%REPO_ROOT_DIR%\build\dokka\html
if exist "%LEGACY_ROOT_DIR%" (
    echo Syncing legacy root docs from %LEGACY_ROOT_DIR% -^> %DEST_DIR%\root
    
    REM Check if source directory has content
    set "has_content=0"
    for /f %%k in ('dir /b "%LEGACY_ROOT_DIR%" 2^>nul ^| find /c /v ""') do (
        if %%k gtr 0 set "has_content=1"
    )
    
    if !has_content! equ 1 (
        REM Remove existing content and copy new content
        if exist "%DEST_DIR%\root" rmdir /s /q "%DEST_DIR%\root"
        mkdir "%DEST_DIR%\root"
        xcopy /E /I /Y "%LEGACY_ROOT_DIR%\*" "%DEST_DIR%\root\" >nul 2>&1
        if !errorlevel! equ 0 (
            echo   Successfully synced legacy root docs
        ) else (
            echo   Warning: Failed to sync legacy root docs
        )
    ) else (
        echo   Skipping empty legacy root docs directory: %LEGACY_ROOT_DIR%
    )
)

REM Prevent GitHub Pages from treating underscores as special, useful if served elsewhere
echo. > "%DEST_DIR%\.nojekyll" 2>nul

echo Done. Local API docs are available under: %DEST_DIR%

REM Generate an index.html that lists all modules (and root) for convenient navigation
set INDEX_FILE=%DEST_DIR%\index.html
(
  echo ^<!DOCTYPE html^>
  echo ^<html lang="en"^>
  echo ^<head^>
  echo   ^<meta charset="UTF-8" /^>
  echo   ^<meta name="viewport" content="width=device-width, initial-scale=1" /^>
  echo   ^<title^>API Reference^</title^>
  echo   ^<style^>body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Ubuntu,Cantarell,^"Noto Sans^",^"Helvetica Neue^",Arial,^"Apple Color Emoji^",^"Segoe UI Emoji^";max-width:960px;margin:32px auto;padding:0 16px;line-height:1.6}h1{margin-bottom:0.25rem}ul{padding-left:1.25rem}code{background:#f6f7f9;padding:2px 6px;border-radius:4px}^</style^>
  echo ^</head^>
  echo ^<body^>
  echo   ^<h1^>API Reference^</h1^>
  echo   ^<p^>Choose a module below to view its Dokka-generated documentation:^</p^>
  echo   ^<ul^>
) > "%INDEX_FILE%"

REM List root multi-module first if present
if exist "%DEST_DIR%\root\index.html" (
  echo     ^<li^>^<a href="./root/index.html"^>^<strong^>All Modules^</strong^> (multi-module index)^</a^>^</li^>>>"%INDEX_FILE%"
)

REM Enumerate module directories
for /f "delims=" %%d in ('dir /b /ad "%DEST_DIR%" 2^>nul') do (
  if /I not "%%d"=="root" (
    if not "%%d"==".git" (
      echo     ^<li^>^<a href="./%%d/index.html"^>%%d^</a^>^</li^>>>"%INDEX_FILE%"
    )
  )
)

(
  echo   ^</ul^>
  echo   ^<p^>^<small^>This page is auto-generated by ^<code^>sync-dokka-to-static-api.bat^</code^>.^</small^>^</p^>
  echo ^</body^>
  echo ^</html^>
)>>"%INDEX_FILE%"
echo Wrote index: %INDEX_FILE%