fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android deployReleaseApkOnFirebase

```sh
[bundle exec] fastlane android deployReleaseApkOnFirebase
```

Publish Release Artifacts to Firebase App Distribution

### android deployDemoApkOnFirebase

```sh
[bundle exec] fastlane android deployDemoApkOnFirebase
```

Publish Demo Artifacts to Firebase App Distribution

### android promoteToBeta

```sh
[bundle exec] fastlane android promoteToBeta
```

Promote internal track to beta on Google Play

### android deployInternal

```sh
[bundle exec] fastlane android deployInternal
```

Deploy AAB to Google Play Store internal track

### android promote_to_production

```sh
[bundle exec] fastlane android promote_to_production
```

Promote beta track to production on Google Play

### android syncListing

```sh
[bundle exec] fastlane android syncListing
```

Sync Play Store store listing — title, description, screenshots, feature graphic. No APK/AAB uploaded.

### android upload_android_screenshots

```sh
[bundle exec] fastlane android upload_android_screenshots
```

Upload Android screenshots + feature graphic to Google Play Store (all active locales)

### android sync_play_listing

```sh
[bundle exec] fastlane android sync_play_listing
```

Sync full Play Store listing (text + screenshots + feature graphic)

### android android_screenshots

```sh
[bundle exec] fastlane android android_screenshots
```

Alias for upload_android_screenshots

----


## Mac

### mac desktop_testflight

```sh
[bundle exec] fastlane mac desktop_testflight
```

Upload macOS desktop build to TestFlight (Mac App Store track)

### mac desktop_release

```sh
[bundle exec] fastlane mac desktop_release
```

Promote macOS desktop build to Mac App Store production

----


## iOS

### ios release

```sh
[bundle exec] fastlane ios release
```

Upload iOS application to App Store

### ios deploy_on_firebase

```sh
[bundle exec] fastlane ios deploy_on_firebase
```

Upload iOS application to Firebase App Distribution

### ios frame_ios_screenshots

```sh
[bundle exec] fastlane ios frame_ios_screenshots
```

Frame iOS screenshots with device bezels via frameit

### ios upload_ios_screenshots

```sh
[bundle exec] fastlane ios upload_ios_screenshots
```

Upload framed iOS screenshots to App Store Connect

### ios ios_screenshots

```sh
[bundle exec] fastlane ios ios_screenshots
```

Frame + upload iOS screenshots (generation is handled by /release)

### ios beta

```sh
[bundle exec] fastlane ios beta
```

Upload beta build to TestFlight

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
