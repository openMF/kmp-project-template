<!-- secrets_demo/README.md -->
# secrets_demo/ — OSS-safe schema-as-code

This tree is the **schema-as-code** mirror of `secrets/`. Every file here is a
PLACEHOLDER carrying a machine-detectable magic marker:

- **Text files:** first line is `# CLAUDE-PLACEHOLDER — do not commit this to secrets/`
- **Binary files:** first 16 bytes are the literal sequence `CLAUDE-PLHLD-v1\0`

`core/scripts/secrets-demo-schema-validate.sh` (framework) enforces that this
tree has exactly the same key set as `secrets/` (when populated) and that every
file carries its marker. Run:

```bash
bash core/scripts/secrets-demo-schema-validate.sh --root .
bash core/scripts/secrets-demo-schema-validate.sh --root . --update-hashes  # regenerate .placeholders-manifest.yaml
```

## Layout

| secrets_demo/ path | Kind | Owning alias | materialize.at peer |
|---|---|---|---|
| `AuthKey.p8` | text | appstore_private_key_p8 | secrets/appstore/AuthKey.p8 |
| `APNAuthKey.p8` | text | (optional) APN push key | secrets/APNAuthKey.p8 |
| `.match_password` | text | match_password | secrets/match/.match_password |
| `match_ci_key` | text | match_git_ssh_private_key | secrets/match/match_ci_key |
| `match_ci_key.pub` | text | (public-key sibling) | secrets/match/match_ci_key.pub |
| `firebaseAppDistributionServiceCredentialsFile.json` | text | firebase_service_account_json | secrets/firebase/service-account.json |
| `playStorePublishServiceCredentialsFile.json` | text | play_publisher_service_account_json | secrets/play/service-account.json |
| `shared_keys.env` | text | (multi-key iOS env shell) | secrets/shared_keys.env |
| `shared_keys.env.template` | text | (template reference) | secrets/shared_keys.env.template |
| `cloudflare/api_token` | text | cloudflare_pages_api_token | secrets/cloudflare/api_token |
| `cloudflare/account_id` | text | cloudflare_account_id | secrets/cloudflare/account_id |
| `netlify/auth_token` | text | netlify_auth_token | secrets/netlify/auth_token |
| `netlify/site_id` | text | netlify_site_id | secrets/netlify/site_id |
| `vercel/token` | text | vercel_token | secrets/vercel/token |
| `vercel/org_id` | text | vercel_org_id | secrets/vercel/org_id |
| `vercel/project_id` | text | vercel_project_id | secrets/vercel/project_id |

Documentation files (`README.md`, `SETUP_CHECKLIST.md`) and the
`.placeholders-manifest.yaml` companion are excluded from validator key-set
parity (they have no `secrets/` peer).

## Layered detection (T12 / AC106)

The validator's `--update-hashes` flag writes `.placeholders-manifest.yaml`
with per-file SHA256 + kind + magic. `manual-preflight.md` (later sub-plan)
checks BOTH magic-marker AND SHA256-match before flagging a file as a
placeholder, eliminating false-positives.

## Authority

- RULE-SECRETS-VAULT-001 (SV2 schema-as-code parity)
- AC78 (≥14 OSS-safe files + README)
- AC83 (key-equivalence enforced)
- AC89 (magic markers on every placeholder)
- AC106 (layered SHA256 detection)
