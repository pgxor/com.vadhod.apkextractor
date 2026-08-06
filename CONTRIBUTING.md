# Contributing to Vadhod APK Extractor

Thanks for your interest. This project has an unusually strict set of constraints — they are the
product, not bureaucracy. Please read this page before opening a pull request.

## The non-negotiables

These are enforced in review, and some are enforced by tests. A PR that breaks one will not be merged.

1. **No `INTERNET` permission, ever.** No networking code, telemetry, analytics, crash reporting, ads,
   or SDKs that phone home. `app/src/test/java/com/vadhod/apkextractor/NoInternetPermissionTest.kt`
   fails the build if `INTERNET` or `ACCESS_NETWORK_STATE` appears in the manifest — do not disable it.
2. **Pure non-root.** Public `PackageManager` + Storage Access Framework only. No `su`, no Shizuku, no
   reflection into hidden APIs.
3. **Least privilege.** Storage exclusively via SAF — no `READ/WRITE_EXTERNAL_STORAGE`, no
   `MANAGE_EXTERNAL_STORAGE`. `QUERY_ALL_PACKAGES` is the only sensitive permission. Adding any
   permission requires a written justification in the PR.
4. **Dependencies are pinned.** Every version lives in `gradle/libs.versions.toml` — never inline a
   version in `build.gradle.kts`. Never downgrade a pinned version. No `-alpha`/`-beta`/`-rc`/snapshot
   releases without an explicit recorded decision. New dependencies must be added to
   `libraries-used.md` with a justification, and must be justified against using a platform API
   instead.
5. **Design system only.** Colors, gradients, shapes, type and spacing come from `core/design`. No
   hard-coded hex, no inline `Brush`, no ad-hoc `dp` corner radii in feature code. If you need a styled
   element, add or reuse a component in `core/design/components` rather than re-implementing it inline.
   WCAG AA (≥ 4.5:1) contrast is mandatory for every text/background pair.

The full list lives in [`rules.md`](rules.md); the architectural blueprint is in
[`architecture.md`](architecture.md).

## Getting set up

```bash
git clone https://github.com/pga5e/com.vadhod.apkextractor.git
cd com.vadhod.apkextractor
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

You need JDK 21 (Temurin is what the daemon criteria pins) and the Android SDK with API 37. No
`keystore.properties` is needed — without it the release build is simply left unsigned.

Development is verified on-device; there is no emulator flow. `./gradlew installDebug` pushes to a
connected device.

## Architecture expectations

- Layered `core/` → `data/` → `feature/`. No Android framework types in `core/model`.
- MVVM with unidirectional data flow: UI is a pure function of immutable `StateFlow` state, events
  flow up. No business logic in composables.
- All file and `PackageManager` I/O runs on `Dispatchers.IO`, is cancellable, and reports progress.
- Errors are **typed and surfaced** (`AppResult`/`AppError`, `ExtractResult.Failure`) — never thrown
  up to crash the app. Batch operations must allow partial success.
- User-facing strings belong in `strings.xml`.

## Pull requests

- Work in small, verifiable steps. Every commit should leave `./gradlew assembleDebug` green.
- Run `./gradlew testDebugUnitTest` before pushing.
- Describe *what* changed and *why*. If you touched anything structural, update `architecture.md`.
- Append a dated entry to `progress.md` (newest on top) describing the change and how you verified it.
- Do not commit `local.properties`, `keystore.properties`, keystores, or any secret.

## Reporting bugs

Open an issue with your device model, Android version, app version, and exact steps to reproduce. For
extraction failures, include the package name of the app you were extracting and whether it is a split
app. **Never paste anything you consider private** — remember the app has no network access, so there
are no logs on our side to correlate with.

## Security

Please do not open a public issue for security problems. See [`SECURITY.md`](SECURITY.md).

## License

By contributing you agree that your contributions are licensed under the
[GNU General Public License v3.0 or later](LICENSE), the same license as the project.
