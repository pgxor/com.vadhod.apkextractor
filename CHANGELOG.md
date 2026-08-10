# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] — 2026-08-10

Packaging-only release for F-Droid inclusion (`versionCode` 4). **No functional changes** — no
behaviour, UI or permission differences from 1.0.

### Changed

- **Removed the dependency-metadata signing block** (`dependenciesInfo { includeInApk = false }`).
  AGP embeds a Play Console dependency manifest into the APK Signing Block, compressed and encrypted
  with a Google key. F-Droid's scanner rejects it as an opaque, non-reproducible, non-free blob.
- **Removed the git-commit stamp** (`vcsInfo { include = false }`). AGP wrote the building commit into
  `META-INF/version-control-info.textproto`, which made the APK depend on *which commit* built it and
  broke reproducible-build verification when the release tag sat on a later commit than the build.
- **Dropped the unused `foojay-resolver-convention` Gradle plugin** from `settings.gradle.kts`. Nothing
  in this project declares a Java toolchain, and F-Droid blocks it because it downloads a JDK at build
  time.
- The About screen now reads **GPL-3.0-or-later**, matching `LICENSE`.

### Notes

- Signed with the same key as 1.0, so it installs as a normal update over the GitHub build.
- Still **not** interchangeable with the Google Play build, which carries Google's app-signing key.

## [1.0] — 2026-07-27

First public release. Published to Google Play on 27 July 2026 (`versionCode` 3).

### Added

- **App browsing** — every installed app, split into **System** and **User** tabs, with icons loaded
  entirely offline.
- **Search and sort** — filter by name, sort by name, size, install date or last update; the chosen
  sort order is persisted.
- **Extraction** — extract a single app, or enter selection mode and **batch-extract** many at once,
  with live per-item and overall progress and partial-success reporting.
- **Split-APK support** — simple apps export as a single `.apk`; split apps are bundled into one
  reinstallable `.apks` archive (base + splits). The default format is configurable.
- **Storage Access Framework** — pick any destination folder once; the grant is persisted. No broad
  storage permissions are requested.
- **App detail screen** — version, size, min/target SDK, install and update dates, the signing
  certificate SHA-256, and a full listing of the APK's zip entries.
- **Sharing** — share an extracted APK via `FileProvider`, or export any app's icon as a PNG.
- **Design system** — soft pastel palette, gradients, Nunito typography, light and dark themes,
  WCAG AA contrast, font-scale and reduce-motion support.
- **Settings** — theme mode, default export format, saved export folder, about/license.

### Security & privacy

- **No `INTERNET` permission.** A guardrail unit test fails the build if `INTERNET` or
  `ACCESS_NETWORK_STATE` ever appears in the manifest.
- No analytics, telemetry, crash reporting, ads, accounts or third-party trackers.
- Non-root: public `PackageManager` and SAF only — no `su`, no Shizuku, no hidden-API reflection.
- The only sensitive permission is `QUERY_ALL_PACKAGES`, required to enumerate installed apps.

[1.0]: https://github.com/pgxor/com.vadhod.apkextractor/releases/tag/v1.0
