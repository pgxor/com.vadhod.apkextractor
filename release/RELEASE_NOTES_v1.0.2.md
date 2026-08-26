Bug-fix release addressing **every finding** from the F-Droid inclusion review
([fdroiddata!44998](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/44998)). Thanks to
[@MiggiV2](https://gitlab.com/MiggiV2) for testing 1.0.1 on real hardware and finding all four, and
to [@andrewpozdnakov7](https://gitlab.com/andrewpozdnakov7) for the source review.

No new permissions. Still no `INTERNET` permission, no tracking, no ads, no root.

## Fixed

- **The APK contents list was unreadable in dark theme.** Every screen sits in a `Scaffold` with a
  transparent container so the pastel gradient shows through, but Material 3 resolves that to no
  content colour and falls back to its hard-coded `Color.Black` default. The *Contents* header and
  all entry names on the app detail screen painted pure black on the dark background, at roughly
  1.3:1 contrast. Fixed once in `GradientBackground`, which now publishes `onSurface` as the content
  colour — closing the whole class of bug for every screen rather than patching two call sites.
- **Split bundles were saved as `.apks.apk`.** The Storage Access Framework reconciles the requested
  MIME type against the file name's extension and renames the file when they disagree: `apks` maps
  to no registered type, while the APK MIME type maps to `apk`. Bundles now request
  `application/octet-stream`, the only type that agrees with an unregistered extension, so the
  `.apks` name is written as intended. **The archive contents were always correct — only the name was
  wrong**, so existing files can simply be renamed to drop the trailing `.apk`.
- **The Settings screen could not be scrolled**, so the About card — with the app name and the
  GPL-3.0-or-later notice — was clipped below the screen edge and unreachable on shorter screens,
  and worse at large font scales.
- **Status- and navigation-bar icons ignored the in-app theme.** `enableEdgeToEdge` detects dark mode
  from the *system* night setting, so choosing Light on a system-dark device (or Dark on a
  system-light one) left the system-bar icons the same colour as the app background, and changing the
  theme in Settings never updated them. Bar appearance now follows the resolved app theme.

## Added

- **An About dialog.** Tapping *About* in Settings opens a compact modal with the app icon, version
  and build number, a short description, the developer and contact address, the licence, third-party
  credits and the copyright line. The contact address opens your email app via `ACTION_SENDTO` with
  the version pre-filled in the subject — a hand-off to another app, not a network call.
- **The Nunito OFL notice now ships with the app.** SIL OFL 1.1 requires the copyright notice and
  licence text to travel with the font, and the app embeds Nunito, so the APK is itself a
  distribution of it. The full licence is bundled at `assets/licenses/OFL-1.1-Nunito.txt` and
  indexed by [`THIRD_PARTY_LICENSES.md`](https://github.com/pgxor/com.vadhod.apkextractor/blob/v1.0.2/THIRD_PARTY_LICENSES.md).

## Verification

```
sha256sum vadhod-apk-extractor-v1.0.2.apk
```

```
5cd3211e0c0ba57a329bd89cfbfc5b1351eeda7300cd22b59661b131647ddf55  vadhod-apk-extractor-v1.0.2.apk
f26f1ad4fc9a3656e097e7bffa25830d62add66fac106e251df38c3e0b889286  vadhod-apk-extractor-v1.0.2-source.zip
```

The APK is signed with certificate `CN=Vadhod, OU=Apps, O=Vadhod, L=Mumbai, ST=MH, C=IN`, SHA-256
fingerprint:

```
9a7ae254b76d1d77aa91a14b144bfb49ce7ae6734bd3ff64d63296a6c04d918d
```

Same key as 1.0 and 1.0.1, so this installs as a normal update over the GitHub build. The APK
Signing Block contains only the v2 signature and padding — no `Dependency metadata` block — and
carries no `META-INF/version-control-info.textproto`, so it stays reproducible from the tagged
commit.

Tested on a moto g45 5G (Android 15): dark-theme contents readable, split export writes a valid
`.apks` containing `base.apk` plus three config splits with all CRCs matching, Settings scrolls, and
the About dialog opens.

> [!IMPORTANT]
> This still will **not** install over the Google Play build. Google re-signs Play uploads with its
> own app-signing key, so the two are not interchangeable — uninstall first if you are switching
> sources.

## Files

| File | Description |
| --- | --- |
| `vadhod-apk-extractor-v1.0.2.apk` | Signed release APK (Android 10+, ~3 MB) |
| `vadhod-apk-extractor-v1.0.2-source.zip` | Source at tag `v1.0.2` |
| `SHA256SUMS.txt` | Checksums for both files |
