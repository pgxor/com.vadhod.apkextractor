**Packaging-only release for F-Droid inclusion. No functional changes** — no behaviour, UI or
permission differences from 1.0. If you are happy on 1.0, there is nothing here you need.

## What changed

Three things were stripped out of the APK, all of them build-system defaults rather than app code:

- **The dependency-metadata signing block.** The Android Gradle Plugin embeds a Play Console dependency
  manifest into the APK Signing Block, compressed and **encrypted with a Google key** so nobody else can
  read it. F-Droid's scanner rejects it as opaque, non-free and non-reproducible. Now disabled with
  `dependenciesInfo { includeInApk = false }`.
- **The git-commit stamp.** AGP wrote the building commit into
  `META-INF/version-control-info.textproto`, which tied the binary to *which commit* produced it. Now
  disabled with `vcsInfo { include = false }`.
- **An unused Gradle toolchain plugin** (`foojay-resolver-convention`), which downloads a JDK at build
  time. Nothing in this project declares a Java toolchain, so it was dead weight.

The About screen now reads **GPL-3.0-or-later**, matching `LICENSE`.

## Verification

```
sha256sum vadhod-apk-extractor-v1.0.1.apk
```

```
9799c19722588e0043aad1195e7df67fec52f6b74aa48d409d19da9658a5b4c7  vadhod-apk-extractor-v1.0.1.apk
015c532dede837cc6538d9efff1298ec3b549376cda81b226c865891d9dbe4ee  vadhod-apk-extractor-v1.0.1-source.zip
```

The APK is signed with certificate `CN=Vadhod, OU=Apps, O=Vadhod, L=Mumbai, ST=MH, C=IN`, SHA-256
fingerprint:

```
9a7ae254b76d1d77aa91a14b144bfb49ce7ae6734bd3ff64d63296a6c04d918d
```

Same key as 1.0, so this installs as a normal update over the GitHub build.

> [!IMPORTANT]
> This still will **not** install over the Google Play build. Google re-signs Play uploads with its own
> app-signing key, so the two are not interchangeable — uninstall first if you are switching sources.

## Files

| File | Description |
| --- | --- |
| `vadhod-apk-extractor-v1.0.1.apk` | Signed release APK (Android 10+, ~3 MB) |
| `vadhod-apk-extractor-v1.0.1-source.zip` | Source at tag `v1.0.1` |
| `SHA256SUMS.txt` | Checksums for both files |
