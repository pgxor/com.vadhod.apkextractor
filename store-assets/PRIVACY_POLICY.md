# Privacy Policy — Vadhod APK Extractor

_Last updated: 27 July 2026_

Vadhod APK Extractor ("the app") is designed to be private by default. This policy explains what the
app does and does not do with your information.

## The short version
**The app collects nothing, sends nothing, and has no internet access.** Everything happens locally on
your device.

## No data collection
The app does **not** collect, store, transmit, or share any personal or usage data. There are:
- **No accounts, no sign-in.**
- **No analytics, no telemetry, no crash reporting.**
- **No advertising and no third-party SDKs** that collect data.

## No network access
The app declares **no `INTERNET` permission** in its manifest and contains no networking code.
Nothing you do in the app ever leaves your device.

## Permissions we use
- **Query all packages** (`QUERY_ALL_PACKAGES`): required so the app can list the applications
  installed on your device — the core function of an APK extractor. This information is only shown to
  you, on-device, and is never recorded or sent anywhere.
- **Storage Access Framework**: when you choose to export an APK, the system file picker lets you pick
  a destination folder. The app only writes the file(s) you explicitly export. It uses no broad
  storage permissions (`READ/WRITE_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE`).

## Files you export
APK and icon files you extract are written only to the folder you select via the system file picker.
You control those files entirely; the app does not track or transmit them.

## Children's privacy
The app collects no data from anyone, including children.

## Changes to this policy
If this policy changes, the updated version will be published at this page with a new "Last updated"
date.

## Contact
Questions? Contact: **Project_AI@a5econsulting.net**
