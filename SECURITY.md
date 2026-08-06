# Security Policy

## Supported versions

| Version | Supported |
| --- | --- |
| 1.0 (latest release) | ✅ |
| Older / pre-release builds | ❌ |

Only the most recent release receives security fixes.

## Reporting a vulnerability

**Please do not open a public GitHub issue for a security problem.**

Report it privately to **Project_AI@a5econsulting.net**, or use GitHub's
[private vulnerability reporting](https://github.com/pga5e/com.vadhod.apkextractor/security/advisories/new)
on this repository.

Please include:

- A description of the issue and its impact.
- Steps to reproduce, or a proof of concept.
- Affected app version, device model and Android version.

You can expect an acknowledgement within **7 days** and, where a fix is warranted, a patched release as
soon as practical. Please give us a reasonable window to ship a fix before disclosing publicly. We are
happy to credit you in the release notes unless you prefer otherwise.

## Scope

The app is offline by design, which rules out entire classes of issue. Things that **are** in scope:

- Anything that causes the app to gain network access, or that would let data leave the device.
- Path traversal or unintended writes through the Storage Access Framework sink
  (`data/extract/SafApkSink.kt`, `core/util/Filenames.kt`).
- Zip handling flaws in the split bundler or APK inspector (`data/extract/SplitBundler.kt`,
  `data/inspect/ApkInspector.kt`) — for example zip-slip while listing or bundling entries.
- Incorrect reporting of a signing certificate in the detail screen.
- Exported components, `FileProvider` misconfiguration, or intent handling that leaks file access to
  other apps.
- Privilege escalation, or any path that reaches a hidden/non-public API.

Out of scope:

- The fact that `QUERY_ALL_PACKAGES` lets the app enumerate installed apps — that is the app's
  documented purpose and the data never leaves the device.
- Issues that require a rooted device, a modified OS, or physical access with an unlocked screen.
- Findings against the Google Play build's signing key (Play re-signs uploads with its own key).

## Verifying a download

Every GitHub release lists the SHA-256 of the published APK. Verify before sideloading:

```bash
sha256sum vadhod-apk-extractor-v1.0.apk
```

You can also check the APK's signing certificate:

```bash
apksigner verify --print-certs vadhod-apk-extractor-v1.0.apk
```

The GitHub release APKs are signed with the certificate `CN=Vadhod, OU=Apps, O=Vadhod, L=Mumbai,
ST=MH, C=IN`, SHA-256 fingerprint:

```
9a:7a:e2:54:b7:6d:1d:77:aa:91:a1:4b:14:4b:fb:49:ce:7a:e6:73:4b:d3:ff:64:d6:32:96:a6:c0:4d:91:8d
```

Play Store and (in future) F-Droid builds are signed with **different** keys — that is expected.
