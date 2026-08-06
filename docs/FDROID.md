# Submitting Vadhod APK Extractor to F-Droid

Working notes and the prepared metadata for getting this app into the official F-Droid repository.
Based on the [Submitting to F-Droid Quick Start Guide](https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/).

---

## 1. Eligibility checklist

| Requirement | Status |
| --- | --- |
| Public source code repository | ⬜ **Repo must be made public** — currently private |
| FOSS license file in the repo | ✅ [`LICENSE`](../LICENSE) — GNU GPL v3 (`GPL-3.0-or-later`) |
| Only FOSS dependencies (no Firebase, no GMS) | ✅ see §2 |
| Author notified / does not oppose inclusion | ✅ we are the author |
| Fastlane metadata in the repo | ✅ see §3 |
| A git tag on each release commit | ⬜ tag `v1.0` must be pushed |

## 2. Dependency audit

Every dependency is FOSS and published to Maven Central or Google's Maven repo. There is no Firebase,
no Google Mobile Services, no Play Services, no analytics SDK, and no closed-source blob anywhere in
the tree.

| Dependency | License |
| --- | --- |
| AndroidX (core-ktx, activity-compose, lifecycle, datastore, documentfile, core-splashscreen) | Apache-2.0 |
| Jetpack Compose (BOM, ui, material3, material-icons-extended) | Apache-2.0 |
| Kotlin stdlib + kotlinx-coroutines | Apache-2.0 |
| Coil (image loading) | Apache-2.0 |
| JUnit, Espresso (test only) | EPL-1.0 / Apache-2.0 |

Coil is used **only** for loading app icons from the local `PackageManager` via a custom
`AppIconFetcher` — no network fetcher is ever configured, and the app has no `INTERNET` permission for
one to use.

There are **no anti-features** to declare: no ads, no tracking, no non-free dependencies, no non-free
assets, no non-free network services, no upstream non-free build artifacts.

## 3. Fastlane metadata (already in this repo)

```
fastlane/metadata/android/en-US/
├── title.txt
├── short_description.txt          (67 chars — limit is 80)
├── full_description.txt
├── changelogs/
│   └── 3.txt                      (filename = versionCode — limit is 500 chars)
└── images/
    ├── icon.png                   (512×512)
    └── phoneScreenshots/
        ├── 1.png … 5.png          (1080×2341)
```

Add a `changelogs/<versionCode>.txt` for **every** future release, named after the `versionCode` in
`app/build.gradle.kts`.

## 4. Tag the release

F-Droid's `UpdateCheckMode: Tags` watches for git tags, so every release commit needs one:

```bash
git tag -a v1.0 -m "Vadhod APK Extractor 1.0"
git push origin v1.0
```

The tag name must stay consistent (`v<versionName>`) across releases.

## 5. Prepared fdroiddata metadata

Fork [`fdroid/fdroiddata`](https://gitlab.com/fdroid/fdroiddata), create
`metadata/com.vadhod.apkextractor.yml` with the content below, and open a merge request. (The guide
notes a merge request is preferred over opening an RFP issue.)

```yaml
Categories:
  - System
License: GPL-3.0-or-later
AuthorName: Parjanya Gala
AuthorEmail: Project_AI@a5econsulting.net
SourceCode: https://github.com/pga5e/com.vadhod.apkextractor
IssueTracker: https://github.com/pga5e/com.vadhod.apkextractor/issues
Changelog: https://github.com/pga5e/com.vadhod.apkextractor/blob/HEAD/CHANGELOG.md

AutoName: Vadhod APK Extractor

RepoType: git
Repo: https://github.com/pga5e/com.vadhod.apkextractor.git

Builds:
  - versionName: '1.0'
    versionCode: 3
    commit: v1.0
    subdir: app
    gradle:
      - yes

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: '1.0'
CurrentVersionCode: 3
```

Before opening the MR, validate it locally with F-Droid's own tooling:

```bash
# in your fdroiddata fork
fdroid readmeta
fdroid lint com.vadhod.apkextractor
fdroid build -v -l com.vadhod.apkextractor    # full build in the buildserver VM
```

## 6. Known build-server risks

Two things are worth checking with `fdroid build` before submitting, because they are the most likely
causes of a rejected build:

1. **Toolchain freshness.** This project uses Gradle 9.6.1, AGP 9.3.x and `compileSdk 37`. F-Droid's
   buildserver has to support all three. If its Gradle or SDK is older, the build will fail and the
   fix is either to wait for the buildserver to catch up or to add explicit `srclibs`/`sudo` setup
   lines to the build recipe.

2. **`gradle/gradle-daemon-jvm.properties` auto-provisioning.** That file carries `toolchainUrl.*`
   entries pointing at the foojay API, so Gradle may try to **download** a JDK. F-Droid builds are
   network-restricted and reproducibility-sensitive, so this will likely need neutralising in the
   build recipe:

   ```yaml
     - versionName: '1.0'
       versionCode: 3
       commit: v1.0
       subdir: app
       prebuild:
         - sed -i '/^toolchainUrl/d' ../gradle/gradle-daemon-jvm.properties
       gradle:
         - yes
   ```

Signing is not a concern: `app/build.gradle.kts` only wires a `signingConfig` when a
`keystore.properties` file is present, and that file is git-ignored. On a clean clone the release
build is unsigned, which is exactly what F-Droid needs before it signs with its own key.

> **Note:** the F-Droid build will be signed with F-Droid's key, so it will **not** install over the
> Play Store or GitHub builds. That is expected and applies to every app on F-Droid.

## 7. After the merge

Expect roughly **24–48 hours** from the fdroiddata merge until the app appears in the main repository.
Once it is live, update the download table in [`README.md`](../README.md) to link to
`https://f-droid.org/packages/com.vadhod.apkextractor/` and add the F-Droid badge.
