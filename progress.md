# progress.md

> Append-only log of what actually happened, newest at top. One entry per working session or
> meaningful change. Record: date, what changed, verification result (build/tests), and decisions.
> This is how a future session knows the true state without re-deriving it.

---

## 2026-08-06 — Public-repo prep: README, GPL-3.0 LICENSE, F-Droid metadata, GitHub release

App is **live on Google Play** (`versionCode` 3, `versionName` 1.0, published 27 Jul 2026). This session
prepared the repo to go public and to be submitted to F-Droid. **T-065 is now essentially done.**

**Added:**

1. **`LICENSE`** — canonical GNU GPL v3 text (35,149 bytes) downloaded from `gnu.org/licenses/gpl-3.0.txt`.
   Matches `architecture.md` Q16 and the existing in-app About string. README declares SPDX
   **`GPL-3.0-or-later`** (the standard GPL boilerplate "or any later version"); change to `GPL-3.0-only`
   if strict-v3 is wanted — it appears in `README.md`, `CONTRIBUTING.md` and `docs/FDROID.md`.

2. **`README.md`** (renamed from `README.MD` — needed `git rm --cached` because `core.ignorecase=true`
   makes plain `git mv` a no-op). Full public README: icon, badges, Play badge, 5 screenshots, features,
   download table with the **signing-key mismatch warning** (Play re-signs, so the GitHub APK won't
   install over a Play install), privacy/permissions section, build instructions, architecture map.

3. **Assets are now tracked.** `DATA/` is git-ignored, so the screenshots and Play badge that lived there
   could never render on GitHub. Screenshots resized 1290×2796 → **1080×2341** and palette-quantised
   (1.2 MB → ~170–570 KB each) into `fastlane/.../phoneScreenshots/`; badge → `docs/assets/`.

4. **F-Droid readiness** — `fastlane/metadata/android/en-US/` (`title`, `short_description` 67 chars,
   `full_description`, `changelogs/3.txt` trimmed under the 500-char cap, `images/icon.png` 512×512,
   `phoneScreenshots/1-5.png`), plus **`docs/FDROID.md`**: eligibility checklist, FOSS dependency audit
   (no Firebase/GMS; Coil is local-icon-only), the ready-to-paste `metadata/com.vadhod.apkextractor.yml`
   for a fdroiddata MR, and two flagged buildserver risks — (a) Gradle 9.6.1/AGP 9.3/`compileSdk 37`
   freshness, (b) `gradle/gradle-daemon-jvm.properties` `toolchainUrl.*` entries can make Gradle try to
   **download** a JDK, which needs a `prebuild` `sed` line to strip.

5. **Repo polish** — `CHANGELOG.md` (Keep a Changelog, 1.0 entry), `CONTRIBUTING.md` (the non-negotiables
   restated), `SECURITY.md` (private reporting, scope, release-verification fingerprint),
   `.github/workflows/build.yml` (test + assembleDebug + assembleRelease on ubuntu/JDK 21),
   `.github/ISSUE_TEMPLATE/` (bug, feature, config).

**Verification:** `./gradlew assembleRelease` **BUILD SUCCESSFUL** — `app-release.apk`, 3,144,057 bytes,
SHA-256 `71887d4321023bd5c6ac1d1b3ffb8e42a3c44ef3e5a655abc9dd5364790bda7d`. `apksigner verify` confirms
`CN=Vadhod, OU=Apps, O=Vadhod, L=Mumbai, ST=MH, C=IN`, cert SHA-256 `9a7ae254b76d…c04d918d`.

**⚠ Local toolchain regression (not a repo bug):** the `toolchainVendor=ADOPTIUM` pin from 27 Jul is
**no longer sufficient**. The VS Code Red Hat extension's bundled JRE (`21.0.11`) is *itself* an Adoptium
build, so it satisfies the vendor pin while still having no `jlink`, and `JdkImageTransform` fails again.
Working invocation until this is properly fixed:

```bash
JDK='C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot'
./gradlew --stop
JAVA_HOME="$JDK" ./gradlew assembleRelease --no-configuration-cache \
  -Dorg.gradle.java.installations.auto-detect=false \
  "-Dorg.gradle.java.installations.paths=$JDK"
```

A durable fix (pin the exact JDK path, or exclude the `.vscode` install from detection) is still **TODO**.

**CI bug found and fixed:** the first Actions runs failed outright — `gradlew` was committed with mode
`100644` (Windows has no executable bit), so `./gradlew` on `ubuntu-latest` died with *Permission denied*
before Gradle started. Fixed with `git update-index --chmod=+x gradlew` (commit `8605b02`). Run
`31075844407` is **green** (7m27s: `testDebugUnitTest` + `assembleDebug` + `assembleRelease`), so the
runner does have `compileSdk 37` and the toolchain resolves correctly on Linux.

**Released:** tag `v1.0` pushed and GitHub release **published** —
<https://github.com/pgxor/com.vadhod.apkextractor/releases/tag/v1.0>. Assets:
`vadhod-apk-extractor-v1.0.apk` (3,144,057 B, sha256 `71887d43…0bda7d`),
`vadhod-apk-extractor-v1.0-source.zip` (3,373,895 B, sha256 `bb95aedf…b5cbae`, `git archive` of the tag),
and `SHA256SUMS.txt`, plus GitHub's auto-generated source archives. Release artifacts are staged in
`release/`, which is now git-ignored.

**Pre-public security check:** `git ls-files` and a full-history `--diff-filter=A` scan confirm
`keystore.properties`, `*.jks` and `local.properties` have **never** been committed. Safe to make public.

**Repo is public** (flipped by the user). GitHub detects GPL-3.0 from `LICENSE`. About/description,
Play Store homepage link and 14 topics set via `gh repo edit`. Both shields.io badges resolve live
(`release: v1.0`, `build: passing`).

**GitHub account renamed `pga5e` → `pgxor`.** GitHub redirects the old paths, but 22 references across
`README.md`, `SECURITY.md`, `CHANGELOG.md`, `CONTRIBUTING.md`, `.github/ISSUE_TEMPLATE/config.yml`,
`docs/FDROID.md`, `docs/fdroid/*.yml` and the release notes were updated to the canonical URL, plus
`git remote set-url` and the published v1.0 release body (`gh release edit`). Canonical URLs matter for
the F-Droid metadata, which shouldn't depend on a redirect.

**Contact email** changed to `parjanyagala@gmail.com` across `SECURITY.md`, both privacy-policy files,
`PLAY_STORE.md` and the F-Droid metadata. ⚠ `store-assets/privacy_policy.html` is the file **hosted at
the URL the Play listing points to** — the live page still shows the old address until it is re-uploaded.

**F-Droid MR submitted:** <https://gitlab.com/fdroid/fdroiddata/-/merge_requests/44998> — "New app:
Vadhod APK Extractor", one new file (`metadata/com.vadhod.apkextractor.yml`), opened from the fork
`pgxor/fdroiddata`, branch `com.vadhod.apkextractor`, targeting `fdroid/fdroiddata:master`. Submitted
via the GitLab API (no clone — fdroiddata is huge). Two API gotchas for next time: `glab api` needs an
explicit `-H "Content-Type: application/json"` with `--input` (otherwise HTTP 415), and the fork import
takes ~5 minutes before branches can be created.

Metadata decisions: `AutoName` removed (F-Droid generates it — the docs say maintainers shouldn't set
it); `prebuild: rm -f ../gradle/gradle-daemon-jvm.properties` included **by default**, because the
`toolchainVendor=ADOPTIUM` pin matches nothing on F-Droid's Debian-OpenJDK buildserver and would push
Gradle into downloading a JDK from the foojay URLs in that same file. Confirmed against the Build
Metadata Reference that `prebuild` runs in `subdir` (`app/`), so the `../` is correct.

**Fork pipelines showed red — diagnosed and cleaned up.** `pgxor/fdroiddata` pipelines
(`2736227287`, `2736235797`, `2736237582`) all failed with **0 jobs** and no YAML errors; the first of
those ran on the fork's untouched `master` *before* our file existed, which proves it wasn't caused by
the metadata — fdroiddata's `app_verification_rules` filter every job out in a fork context.

Empirical check that settled it: **all 12 recently-merged "New app:" MRs have `head_pipeline: none`** —
no pipeline runs for them at all. Ours only got one because a fresh GitLab fork has CI/CD enabled by
default. Fix: set `jobs_enabled=false` / `builds_access_level=disabled` on the fork and delete the four
empty pipelines, so the MR matches every merged precedent. MR now reports `head_pipeline: none`,
`detailed_merge_status: mergeable`, and `squash=true` is set (the branch has 2 commits — create, then
the pgxor URL fix). ⚠ Ordering gotcha: disabling builds first makes the pipelines API return 404 — must
delete pipelines *before* disabling. Nothing was suppressed: those pipelines contained zero jobs and
zero verification; F-Droid runs its own build after review.

**Still open:** (a) SPDX is declared `GPL-3.0-or-later` in `README.md`, `CONTRIBUTING.md` and
`docs/FDROID.md` — switch to `GPL-3.0-only` if strict-v3 is wanted. (b) Re-upload `privacy_policy.html`
to its hosting so the live page shows the new contact address. (c) Await F-Droid maintainer review;
the flagged toolchain-freshness risk (Gradle 9.6.1 / AGP 9.3.1 / `compileSdk 37`) is still unverified
against their buildserver.

---

## 2026-07-27 — Build fixes, data-layer recovery, and 3D onboarding

**Three things this session (all currently uncommitted):**

1. **Build was broken (`jlink` / JdkImageTransform).** `gradle/gradle-daemon-jvm.properties` required only
   "Java 21, any vendor", so Gradle auto-detected the VS Code Red Hat **JRE** (no `jlink`) and the Android
   `JdkImageTransform` failed nondeterministically. **Fix:** pinned `toolchainVendor=ADOPTIUM` (Eclipse
   Temurin JDK 21, has `jlink`). Removed the now-moot `org.gradle.java.home` from `gradle.properties`.

2. **Entire `data/` layer was missing.** `.gitignore` had a bare `DATA` pattern (for the ref-apps folder);
   with `core.ignorecase=true` on Windows it also matched the `data/` **source package**, so it was never
   committed — and this is a fresh clone, so the files were gone (user confirmed deleted, no backup).
   **Fix:** `.gitignore` → `/DATA/` (anchored). **Reconstructed all 12 types** from the call sites:
   `Settings`/`SettingsRepository`, `PackageManagerSource`/`PackageRepository`,
   `AppIconRequest`/`AppIconKeyer`/`AppIconFetcher` (Coil 3), `ApkSink`/`SafApkSink`/`ApkExtractor`,
   `ApkInspector`/`ApkEntryInfo`, `Exporters`. No new deps; platform APIs only. Added `ApkExtractorTest`
   (6 cases). `assembleDebug`, `assembleRelease` (R8), `testDebugUnitTest` (**19 tests, 0 fail**) all green.
   On-device: installed + launched + UI rendered, no crash (extraction happy-path not yet tapped through).

3. **First-run onboarding (3 screens) + replay.** New `feature/onboarding/`: `OnboardingScreen` (3-page
   `HorizontalPager`, animated pill indicator, Skip/Next/Get-started) + `OnboardingIllustration`. Art is
   **100% Compose-drawn, no assets/dependency**: page 2 is a hand-drawn **isometric 3D box that hinges
   open while an APK card pops out**; pages 1 & 3 are animated pastel orbs. Gated on new
   `Settings.onboardingCompleted` (DataStore); MainActivity holds the splash until settings load to avoid
   a flash. **Settings → Help → "Replay"** re-shows it. *(Considered SceneView/Filament for real `.glb`
   3D — added then reverted: user couldn't source models; Compose-native fits the offline/minimal-dep
   rules far better.)* Compiles + builds + tests green; on-device verify pending (wireless adb kept
   dropping).

---

## 2026-06-08 — Fix: extraction sheet not showing from detail screen

**Bug:** tapping Extract (base/bundle) on the **detail** screen ran the extraction but showed no
progress/done sheet — `ExtractionSheet` was rendered inside `AppListScreen`, which isn't composed
while on the Detail route. **Fix:** render `ExtractionSheet` at the root (`AppRoot`) keyed on
`vm.state.extraction`, so it overlays any screen. Removed the now-unused `onDismissExtraction` param
from `AppListScreen`. Verified: rebuilt, reinstalled, launched clean on device.

---

## 2026-06-08 — Phases 2–6: full pastel UI, extraction UX, details, release hardening

**Built the whole app on top of the Phase-1 engine:**
- **Design system** (`core/design`): Anthropic/Claude pastel palette (`PastelColors`, light + dim
  dark M3 schemes), **Nunito** bundled variable font (`res/font/nunito.ttf`, weights via
  FontVariation), `AppShapes`, `Gradients` (+`LocalAppGradients`), `VadhodTheme`. Components:
  `GradientBackground`, `AppIcon` (Coil), `PillTabs`, `SearchField`, `AppListItem`, `Pill`,
  loading/empty states.
- **Coil 3 icon loading:** `AppIconFetcher`/`Keyer` + `App`/`AppContainer` (manual DI, Coil
  `SingletonImageLoader.Factory`, offline icons from PackageManager).
- **Feature/applist:** `AppListViewModel` (search, sort w/ persistence, multi-select, batch extract
  with per-item + overall progress), `AppListScreen` (two pill tabs, FAB, sort menu), `ExtractionSheet`
  (live **%** + overall progress bar + per-app results).
- **Feature/detail:** metadata, signing SHA-256, APK contents (zip entries), Extract base/bundle,
  Share, Export icon.
- **Feature/settings:** theme mode, bundle-splits default, export folder picker, about/license.
- **MainActivity/AppRoot:** splash, theme, state-based nav (List/Detail/Settings), SAF
  `OpenDocumentTree` + `CreateDocument` launchers, FileProvider share, BackHandler.
- **Manifest hardening:** `android:name=".App"`, `allowBackup=false`, FileProvider (not exported,
  `file_paths.xml` cache/shared only). Still **no INTERNET** (guardrail passes).
- **Release/R8:** `isMinifyEnabled`+`isShrinkResources` on; `proguard-rules.pro`. `assembleRelease`
  BUILD SUCCESSFUL (R8 clean). material-icons-extended added (BOM-governed; R8 strips unused in
  release) — documented reversal in `libraries-used.md`.

**Verification (device: moto g45 5G / Android 15):** debug build installed, launched, **resumed**,
process healthy, **no exceptions** — logs show it actively reading installed APK assets (icons +
listing working). **T-011 on-device listing confirmed.** `assembleDebug`, `assembleRelease`,
`testDebugUnitTest` all green.

**Outstanding (honest):** custom pastel **app launcher icon** still the scaffold default (T-055);
**UI strings not yet externalized** to `strings.xml` (T-066, rules.md §D-23); crossfade nav motion
minimal (T-053); accessibility audit not formalized (T-060). None block usage.

**Re-test pending:** added live **%** to extraction sheet + overall-progress bar; rebuilt OK but
wireless adb dropped before reinstall — needs device reconnect to push the updated debug APK.

---

## 2026-06-08 — Phase 0 complete + Phase 1 core engine implemented & tested

**Phase 0 (foundations) — DONE:**
- T-001 ✅ six libraries added to catalog with `strictly` pins, wired in `app/build.gradle.kts`;
  `assembleDebug` green (~5m first run, deps downloaded).
- T-002 ⏸ deferred (lockfiles near-release; `strictly` already blocks downgrades).
- T-003 ✅ `NoInternetPermissionTest` guardrail (no INTERNET/ACCESS_NETWORK_STATE).
- T-004 ✅ package skeleton (`core/`, `data/`, `feature/` taking shape).
- T-005 ✅ `DispatcherProvider` + `AppResult`/`AppError` + `appResult{}`.
- **Device smoke test:** installed + launched on **moto g45 5G (Android 15)** — runs (scaffold UI).

**Phase 1 (core data + extraction engine) — implemented:**
- Model: `AppEntry`, `SortOrder`(+`sortedByOrder`), `ExportFormat`, `ThemeMode`, `Filenames`.
- Data: `PackageManagerSource` (system/user classify, split paths, sizes), `PackageRepository`
  (+partition), `ApkExtractor` (single copy) + `SplitBundler` (`.apks` zip) + `ApkSink`/`SafApkSink`,
  `SettingsRepository` (DataStore), `ApkInspector` (signing SHA-256 + zip entries).
- Manifest: added `QUERY_ALL_PACKAGES` (Q8); still **no INTERNET** (guardrail passes).
- **Tests green** (`testDebugUnitTest`): model/sort, filenames, AppResult, and the **extraction
  pipeline** — byte-exact single copy, valid `.apks` bundle with all entries, typed NotFound &
  OutOfSpace failures, cancellation re-throw.
- **Status:** `assembleDebug` + `testDebugUnitTest` both BUILD SUCCESSFUL. T-011 device-listing
  verification is the one open item (lands with Phase 2 UI).

**Next:** Phase 2 — pastel theme (Anthropic palette) + Coil icon loader + two-tab app shell + list.

---

## 2026-06-08 — Questionnaire answered + Phase 0 started

**Questionnaire folded in** (see `architecture.md` §3.1). Highlights: name "Vadhod APK Extractor";
**Anthropic/Claude palette** (ivory + clay/coral + kraft + manilla + sage + warm charcoal); light +
dim-pastel dark; **no** dynamic color; **Nunito** font; `QUERY_ALL_PACKAGES`; `.apks` bundle default;
icon export yes; inspector = entries + signing SHA-256 + manifest summary; license **GPL-3.0**;
distribute on Play + GitHub + F-Droid; English + l10n scaffold.

**minSdk:** user originally wrote "10+"; that's infeasible (Compose floor = API 21, splits need 21+).
Clarified → user chose **29** (cleanest). No scaffold change needed.

**Now starting Phase 0 (T-001):** applying locked library catalog + wiring deps.

---

## 2026-06-08 — Project planning & documentation set (this session)

**Who:** Claude (Opus 4.8) + user.

**What was done**
- Studied the three reference apps in `DATA/`:
  - **SAI** (`com.aefyr.sai`, v4.5, SDK 29) — split-APK installer/backup; Shizuku, Room, Glide,
    Firebase/billing flavors. Most relevant architecture reference.
  - **APK-Explorer-Editor** (SDK 37, v0.34) — APK browse/edit; apksig, zip4j, baksmali/smali.
  - **apkExtractor** (2017, SDK 25, support-v7) — minimal historical reference only.
- Inspected the existing scaffold: single-Activity Compose app, AGP 9.2.1, Kotlin 2.4.0, Compose
  BOM 2026.02.01, compileSdk/targetSdk 37, minSdk 29, JVM 11, configuration-cache on.
- **Collected four product decisions from the user** (via questionnaire): pure non-root;
  export both single `.apk` + split bundle `.apks`; save via SAF user-picked folder; v1 features =
  core extract + search/sort + batch multi-select + details/inspector + share/icon export.
- **Researched latest STABLE library versions online (verified 2026-06-08)** and recorded them in
  `libraries-used.md`: Coil 3.4.0, DataStore 1.2.1, coroutines 1.11.0, core-splashscreen 1.2.0,
  lifecycle-viewmodel-compose 2.10.0, documentfile 1.1.0. Decided to **drop** apksig/zip4j/Shizuku/
  nav-library/Firebase (use platform APIs / state-based nav) to minimize attack surface.
- **Authored the documentation set:** `architecture.md`, `rules.md`, `tasks.md`, `todo.md`,
  `progress.md` (this file), `libraries-used.md`, and `questionnaire.md`.

**Verification**
- No code changes yet — build state unchanged from `BUILD SUCCESSFUL` baseline (commit `ca20287`).
- Connected test device confirmed available: `adb-ZD222NX2DM-YuHGqi._adb-tls-connect._tcp`.

**Decisions locked**
- See `architecture.md` §3. No `INTERNET` permission; offline-only; no telemetry; non-root.
- Version-locking policy established (`libraries-used.md` §0, `rules.md` §B).

**Open / next**
- Awaiting `questionnaire.md` answers (do not block Phase 0–1).
- Next session starts at **T-001** (apply library catalog additions + locking).

---

## 2026-06-08 — Baseline (pre-existing)
- Scaffold committed: `ca20287 BUILD SUCCESSFUL in 4s`, `f826fe2 init`.
- `assembleDebug` green (per user terminal output).
