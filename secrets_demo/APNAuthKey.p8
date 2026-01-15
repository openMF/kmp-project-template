-----BEGIN PRIVATE KEY-----
REPLACE_THIS_WITH_YOUR_ACTUAL_APPLE_PUSH_NOTIFICATION_APN_KEY_P8_FILE_CONTENT

OPTIONAL: Only needed if your app uses Firebase Cloud Messaging or push notifications

Create at: https://developer.apple.com/account/resources/authkeys/list
  1. Click + to create a new key
  2. Enable "Apple Push Notifications service (APNs)"
  3. Download the .p8 file (you can only do this once!)
  4. Note the Key ID

After creating this key:
  1. Upload to Firebase Console → Project Settings → Cloud Messaging → APNs certificates
  2. Update secrets/shared_keys.env with APN_KEY_ID

Keep this file secure and NEVER commit to git.
-----END PRIVATE KEY-----
