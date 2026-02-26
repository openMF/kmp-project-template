# Android String Resource Translator (`translate.py`)

A production-ready Python script for translating Android string resources (`strings.xml` and `arrays.xml`) using the Google Gemini API.

## Features

- **Format Preservation**: Ensures comments, spacing (blank lines), and structure match the source file exactly.
- **Placeholder & Markup Safety**: Freezes placeholders (e.g., `%s`, `%1$d`) and markup tags (e.g., `<b>`, `<xliff:g>`) before translating to guarantee they are preserved and kept in the correct order.
- **Source Attribute Propagation**: Copies attributes like `formatted`, `product`, and `tools:*` to the translated strings.
- **Robust Error Handling**: Includes batch translation with individual string fallback on failure, and automatic retry mechanisms for rate limits (429) or model overloads (503).
- **Change Detection**: Tracks source strings through a simple hash-based snapshot mechanism (`.translation_snapshots/`). Only new strings and strings whose source text modified are re-translated, saving time and tokens.
- **Advanced Resource Support**: Translates single `<string>`, ordered `<string-array>`, and `<plurals>` resources out-of-the-box.
- **Character Compatibility**: Manages HTML entity conversions and robust Android special character escaping.
- **AAPT2 Compatibility**: Implements proper `xliff` namespace handling to prevent build errors.

## Requirements

1. Python 3.8+
2. Required packages:
   ```bash
   pip install google-genai lxml
   ```
3. A Google Gemini API Key

## Usage

Set your Google Gemini API key as an environment variable:
```bash
export GEMINI_API_KEY=your_api_key_here
```
*(You can customize the environment variable name via the `--api-key-env` flag).*

### Applying Translations

Run the script in `apply` mode to fetch missing strings and write translated files directly to their respective `values-{locale}` folders.

```bash
# Basic usage
python translate.py --mode apply --locales es,de,fr

# Using a specific model and fine-tuning batch parameters
python translate.py \
    --mode apply \
    --repo-root . \
    --locales ar \
    --model gemma-3-27b-it \
    --batch-size 15 \
    --request-delay 4.0
```

### Checking for Missing Translations

Run the script in `check` mode inside CI/CD workflows to simply verify whether all strings are translated without making any actual API calls or file modifications.

```bash
python translate.py --mode check --locales es,de,fr
```
*In `check` mode, the script exits with code `2` if translations are missing.*

## Available Command-Line Arguments

- `--mode` (Required): `apply` (to translate and write xml) or `check` (to only check for missing keys).
- `--locales`: A comma-separated list of target Android language/region codes (e.g. `es,fr,de,ar`). Default is `es,de`.
- `--repo-root`: The path to the root of the Android project (where to search for `src/*/res/values/strings.xml` or Compose Multiplatform equivalent). Default is `.`.
- `--model`: The Gemini API model to use. Default is `gemini-2.0-flash`.
- `--batch-size`: Number of strings to send in a single Gemini API request. Default is `20` (capped at `15` for Gemma models).
- `--request-delay`: Delay in seconds between API requests to prevent immediate rate-limiting. Default is `2.0` (forced to `4.0` for Gemma models).
- `--api-key-env`: Name of the environment variable used to retrieve the API key. Default is `GEMINI_API_KEY`.
- `--no-validate`: Disable automatic malformed XML checks after writing translations.
- `--verbose` / `-v`: Enable debug-level logging.

## Under The Hood

### 1. Snapshot Tracking
When you successfully translate strings, the script saves a JSON file in `.translation_snapshots/` within the source module. Subsequent runs will compare current source text against these hashes, allowing `translate.py` to seamlessly fix previously translated strings if you tweak the original English wording.

### 2. Orphaned Translations cleanup
In `apply` mode, if a developer deletes a string or an array item from the english source, the script reliably detects and strips the orphaned translation from all localized strings files to avoid accumulation of unused strings.

### 3. Rate Limit Handling
If the Google Gemini backend responds with `429 Rate limited` or `503 Service Unavailable`, `translate.py` will automatically backoff and retry according to `--max-retries` and the wait times embedded in API responses.
