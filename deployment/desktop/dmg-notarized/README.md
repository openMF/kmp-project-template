# deployment/desktop/dmg-notarized — Notarized macOS DMG

**Tier:** 2 (opt-in)
**Owning capability:** `macos-signing`
**Signing:** Developer ID Application cert + `xcrun notarytool` + `stapler`
**Gating:** `requires_confirm: true`

## What this deploys

Apple-notarized macOS DMG attached to a GitHub release. Notarization runs
through `xcrun notarytool submit --wait`, then `xcrun stapler staple` attaches
the ticket so the DMG verifies offline. Users get no Gatekeeper warning.

## Cost / time note

- Apple Developer Program: $99/yr.
- Notarization wait: 5-15 minutes (set `requires_confirm: true` to gate CI usage).

## Local deploy

```bash
export DEVELOPER_ID_NAME='Developer ID Application: Mifos Initiative (TEAMID)'
export ASC_API_KEY_PATH=secrets/AuthKey.p8
export ASC_KEY_ID=...
export ASC_ISSUER_ID=...
TAG=v2026.06.04 bash deployment/desktop/dmg-notarized/script.sh
```

## Secrets required

Run `/secrets pull --required-for macos-signing`. See `secrets-needs.yaml`.

## Dev fallback

`dev_fallback.signing_mode: ad-hoc` — local dev skips notarytool (use ad-hoc
codesign on trusted devices) per `config.yaml`.
