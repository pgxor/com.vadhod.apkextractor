# Submitting Vadhod APK Extractor to F-Droid

Working notes and the prepared metadata for getting this app into the official F-Droid repository.
Based on the [Submitting to F-Droid Quick Start Guide](https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/).

---

## 1. Eligibility checklist

| Requirement | Status |
| --- | --- |
| Public source code repository | ✅ <https://github.com/pgxor/com.vadhod.apkextractor> |
| FOSS license file in the repo | ✅ [`LICENSE`](../LICENSE) — GNU GPL v3 (`GPL-3.0-or-later`) |
| Only FOSS dependencies (no Firebase, no GMS) | ✅ see §2 |
| Author notified / does not oppose inclusion | ✅ we are the author |
| Fastlane metadata in the repo | ✅ see §3 |
| A git tag on each release commit | ✅ `v1.0` pushed |

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

Fork [`fdroid/fdroiddata`](https://gitlab.com/fdroid/fdroiddata), copy
[`docs/fdroid/com.vadhod.apkextractor.yml`](fdroid/com.vadhod.apkextractor.yml) from this repo to
`metadata/com.vadhod.apkextractor.yml` in the fork, and open a merge request. (The guide notes a merge
request is preferred over opening an RFP issue.)

The file is kept in this repo so it stays in sync with `versionCode`/`versionName` — update it in the
same commit as any future version bump. Current contents:

```yaml
Categories:
  - System
License: GPL-3.0-or-later
AuthorName: Parjanya Gala
AuthorEmail: parjanyagala@gmail.com
WebSite: https://pgxor.pages.dev/apk-extractor/
SourceCode: https://github.com/pgxor/com.vadhod.apkextractor
IssueTracker: https://github.com/pgxor/com.vadhod.apkextractor/issues
Changelog: https://github.com/pgxor/com.vadhod.apkextractor/blob/HEAD/CHANGELOG.md

AutoName: APK Extractor

RepoType: git
Repo: https://github.com/pgxor/com.vadhod.apkextractor.git
Binaries: 
  https://github.com/pgxor/com.vadhod.apkextractor/releases/download/v%v/vadhod-apk-extractor-v%v.apk

Builds:
  - versionName: '1.0'
    versionCode: 3
    commit: 8605b0254e06734199fdd92cb274dcf2e2c6659a
    subdir: app
    gradle:
      - yes
    prebuild:
      - sed -i '/foojay-resolver/d' ../settings.gradle.kts
      - echo "org.gradle.java.home=/usr/lib/jvm/java-21-openjdk-amd64" >> ../gradle.properties

AllowedAPKSigningKeys: 9a7ae254b76d1d77aa91a14b144bfb49ce7ae6734bd3ff64d63296a6c04d918d

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: '1.0'
CurrentVersionCode: 3
```

Both `prebuild` lines are **not optional** — see §6.1 and §6.2.

**The formatting is machine-generated, not stylistic.** F-Droid's `fdroid rewritemeta` CI job rewrites
the file and fails if the result differs from what is committed. Three things it insists on, all of them
non-obvious:

- `AutoName` (the launcher label read from the manifest) sits above `RepoType`, blank line either side.
- Inside a `Builds` entry, `gradle` comes **before** `prebuild` — the field order is fixed by
  `fdroidserver`, not by the file.
- `Binaries:` is long enough to wrap, so the tool emits the key with a **trailing space** and the URL
  indented on the next line. That trailing space is load-bearing; stripping it fails the job.

When in doubt, do not guess — read the diff the `checkupdates` / `fdroid rewritemeta` job prints and copy
it verbatim.

### 5.1 Review feedback applied (2026-08-06)

Reviewer `@seekme-seekyou` asked for four changes, all applied:

1. **Use the App Inclusion MR template** (`.gitlab/merge_request_templates/App inclusion.md`) — the MR
   description now follows it.
2. **Full commit hash, not the tag** — `commit:` is the 40-char SHA, not `v1.0`. Tags can move; hashes
   can't. `UpdateCheckMode: Tags` still drives auto-update.
3. **Reproducible builds** — `Binaries:` (note the `%v` placeholder = versionName) plus
   `AllowedAPKSigningKeys:` (our signing cert SHA-256, lowercase hex, no colons). F-Droid rebuilds from
   source and, if byte-identical, ships **our** signed APK, so users can move between the GitHub release
   and F-Droid without uninstalling. **Reproducibility is unverified** — we have no Linux/Docker
   environment to run `fdroid build`. If it doesn't reproduce we rebuild and re-publish the release;
   the fallback is dropping `Binaries` and letting F-Droid sign. Declining reproducible builds at
   inclusion time is the *irreversible* choice, which is why it was enabled.
4. **JDK 21** — see §6.1; the first attempt at this was wrong and was replaced in round 3.

**Release-asset naming is now load-bearing.** `Binaries` expands to
`.../releases/download/v<versionName>/vadhod-apk-extractor-v<versionName>.apk`. Every future release
must keep that tag and asset-name pattern or reproducible-build verification breaks.

### 5.2 Review feedback, round 3 (2026-08-10)

`@linsui` asked to fix the pipeline; `@seekme-seekyou` named three items. Reading the failed jobs
directly turned up a fourth, which was the one actually breaking the build.

| # | Item | Fix |
| --- | --- | --- |
| 1 | `AutoName` missing | `AutoName: APK Extractor` above `RepoType` |
| 2 | `prebuild` before `gradle` | swapped — `fdroidserver` fixes the field order |
| 3 | `Binaries` line format | wrapped form, trailing space after the key |
| 4 | **`fdroid build` failed on the scanner** | drop the foojay plugin — §6.1 |

Items 1–3 were taken from the diff printed by the `checkupdates` job (`fdroid rewritemeta` output),
so they are the tool's own formatting rather than a reading of the docs.

Before opening the MR, validate it locally with F-Droid's own tooling:

```bash
# in your fdroiddata fork
fdroid readmeta
fdroid lint com.vadhod.apkextractor
fdroid build -v -l com.vadhod.apkextractor    # full build in the buildserver VM
```

## 6. Known build-server risks

### 6.1 The foojay resolver fails the scanner (this is what broke the build)

`fdroid build` runs a source scanner before Gradle, and it treats the Gradle toolchain auto-provisioner
as a "usual suspect" — it downloads a JDK at build time, which is both network access and a
reproducibility hazard:

```
ERROR: Found usual suspect 'org.gradle.toolchains.foojay-resolver' at settings.gradle.kts
ERROR: Could not build app com.vadhod.apkextractor: Can't build due to 1 error while scanning
```

`settings.gradle.kts` carries `id("org.gradle.toolchains.foojay-resolver-convention")` purely because
the Android Studio new-project template puts it there. **Nothing in this project declares a Java
toolchain** — the only Java configuration anywhere is `sourceCompatibility`/`targetCompatibility =
JavaVersion.VERSION_11` in `app/build.gradle.kts`. The plugin is therefore never exercised, and removing
it cannot change build output:

```yaml
      - sed -i '/foojay-resolver/d' ../settings.gradle.kts
```

This is the standard fdroiddata idiom (see `metadata/es.pile.yml`, `metadata/com.geeksville.mesh.yml`).
It leaves an empty `plugins { }` block, which is valid Kotlin DSL.

**Worth doing upstream.** The plugin is dead weight here and should be dropped from
`settings.gradle.kts` in a future release, at which point this `prebuild` line can go. It was left in
place for v1.0 because `commit:` is pinned to the published tag and the release APK must stay
byte-comparable for reproducible-build verification.

### 6.2 JDK selection — the daemon-JVM criteria file is deleted for you

The first two attempts at this were both wrong, so the reasoning is worth recording.

`gradle/gradle-daemon-jvm.properties` pins the Gradle **Daemon JVM Criteria** to `toolchainVersion=21`
and `toolchainVendor=ADOPTIUM`, plus `toolchainUrl.*` foojay endpoints. The vendor pin works around a
local Windows problem (see `progress.md`) and has no business running on a Debian buildserver.

The recipe originally `sed`-ed that file to strip the vendor pin and URLs. **That was pointless** — the
scanner deletes the whole file on its own, before Gradle ever starts:

```
INFO: Removing gradle-daemon-jvm.properties at gradle/gradle-daemon-jvm.properties
INFO: Removing gradle-wrapper.jar at gradle/wrapper/gradle-wrapper.jar
```

With the criteria file gone, the daemon would simply run on whatever JDK the buildserver defaults to —
which is not what the reviewer asked for. So the JDK is now pinned directly, in a way that survives the
scanner:

```yaml
      - echo "org.gradle.java.home=/usr/lib/jvm/java-21-openjdk-amd64" >> ../gradle.properties
```

(`prebuild` runs in the `subdir`, i.e. `app/`, hence the `../` — confirmed against the Build Metadata
Reference. That JDK path is the one used by other fdroiddata recipes, e.g.
`metadata/com.itsfrz.tictactoe.yml`.)

The alternative is `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64` inside a custom `build:` block;
it was offered to the reviewer. The `gradle.properties` form was preferred because it keeps `gradle: yes`
handling output discovery.

### 6.3 Toolchain freshness

This project uses Gradle 9.6.1, AGP 9.3.x and `compileSdk 37`. F-Droid's buildserver has to support all
three. If its Gradle or SDK is older the build will fail; the fix is either to wait for the buildserver
to catch up or to add explicit `srclibs`/`sudo` setup lines to the recipe. GitHub Actions'
`ubuntu-latest` builds this fine (see the `Build` workflow), which is a reasonable proxy but not a
guarantee.

### 6.4 Not a problem: signing

Signing is not a concern: `app/build.gradle.kts` only wires a `signingConfig` when a
`keystore.properties` file is present, and that file is git-ignored. On a clean clone the release
build is unsigned, which is exactly what F-Droid needs before it signs with its own key.

> **Note:** the F-Droid build will be signed with F-Droid's key, so it will **not** install over the
> Play Store or GitHub builds. That is expected and applies to every app on F-Droid.

## 7. After the merge

Expect roughly **24–48 hours** from the fdroiddata merge until the app appears in the main repository.
Once it is live, update the download table in [`README.md`](../README.md) to link to
`https://f-droid.org/packages/com.vadhod.apkextractor/` and add the F-Droid badge.
