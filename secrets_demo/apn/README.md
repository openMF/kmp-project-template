# Apple Push Notifications (APN) Auth Key — Optional

Used for: enabling iOS push notifications via Firebase Cloud Messaging (FCM).
This is **optional** — skip it entirely if your app doesn't use push notifications.

## How to get it

1. Go to [Apple Developer — Keys](https://developer.apple.com/account/resources/authkeys/list)
2. Click **+** to create a new key
3. Name: `APNs Auth Key`
4. Check **Apple Push Notifications service (APNs)**
5. Click **Continue** → **Register**
6. **Download the `.p8` file immediately** (you can only download it once)
7. Note the **Key ID** (10 characters)
8. Your **Team ID** is the same as your App Store Connect team (`iosTeamId` in `libs.versions.toml`)

## Files

| File | Content |
|------|---------|
| `secrets/apn/APNAuthKey.p8` | The downloaded `.p8` file |

The Key ID and Team ID are set in `secrets/shared_keys.env`:
```bash
export APN_KEY_ID="YOUR_APN_KEY_ID"
export APN_KEY_PATH="./secrets/apn/APNAuthKey.p8"
export APN_TEAM_ID="$TEAM_ID"
```

## Commands

```bash
mkdir -p secrets/apn
cp ~/Downloads/AuthKey_*.p8 secrets/apn/APNAuthKey.p8
# Then update APN_KEY_ID in secrets/shared_keys.env
```

## Notes

- One APN key works for all your apps under the same Apple Developer account
- Keys don't expire but should be rotated periodically
- If you lose the `.p8` file you must revoke and regenerate
