# libraries-used.md

> **PURPOSE OF THIS FILE — READ BEFORE TOUCHING DEPENDENCIES.**
> This file is the **single source of truth** for every dependency in this project, the exact
> version we pin to, the date that version was verified as the latest stable release, and *why*
> it was chosen. **No tool, model, or contributor may downgrade any version listed here.** If a
> newer **stable** release exists, it may be *upgraded* (and this file updated with the new date),
> but it must **never** be downgraded, and pre-release builds (`-alpha`, `-beta`, `-rc`, `-dev`,
> snapshots) must **never** replace a stable pin without an explicit human decision recorded here.
>
> Versions were verified online on **2026-06-08** (project locale: web "current month" = June 2026).
> Knowledge-cutoff note: do not rely on memorized version numbers — they are stale. Re-verify
> against Maven Central / the AndroidX release pages and update the "Verified on" column.

---

## 0. Locking policy (how we prevent downgrades)

1. **All versions live in the Gradle version catalog** (`gradle/libs.versions.toml`). Nothing is
   declared inline in a `build.gradle.kts` with a hard-coded version string.
2. **Critical libraries use `strictly` constraints** in the catalog so Gradle will *fail the build*
   if any transitive dependency tries to pull a lower version. Example:
   ```toml
   [versions]
   coil = "3.4.0"
   [libraries]
   coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version = { strictly = "3.4.0" } }
   ```
3. **A dependency-verification + locking pass** is enabled (see `rules.md` → "Dependency locking"):
   - `org.gradle.configuration-cache=true` (already on).
   - We commit Gradle **dependency lockfiles** (`gradle.lockfile`) once the dependency set is final
     (`./gradlew dependencies --write-locks`).
4. **The Gradle wrapper is pinned** (`gradle/wrapper/gradle-wrapper.properties`) and its distribution
   SHA-256 is committed so the build toolchain itself can't silently change.
5. Any change to a version here requires a one-line entry in `progress.md` and a bump of the
   "Verified on" date.

---

## 1. Build toolchain (already present in scaffold — verified current)

| Component | Pinned version | Verified on | Notes |
|---|---|---|---|
| Android Gradle Plugin (AGP) | `9.2.1` | 2026-06-08 | `com.android.application`. Latest stable AGP line. |
| Kotlin | `2.4.0` | 2026-06-08 | K2 compiler. Drives `org.jetbrains.kotlin.plugin.compose`. |
| Compose Compiler | bundled w/ Kotlin `2.4.0` | 2026-06-08 | Since Kotlin 2.0 the Compose compiler ships with Kotlin; managed by the `kotlin-compose` plugin. |
| compileSdk / targetSdk | `37` | 2026-06-08 | Latest platform. |
| minSdk | `29` (Android 10) | 2026-06-08 | See `architecture.md` → "Why minSdk 29". |
| Java/JVM target | `11` | 2026-06-08 | From scaffold. Candidate to raise to 17/21 (see tasks.md T-013). |
| foojay-resolver-convention | `1.0.0` | 2026-06-08 | Toolchain auto-provisioning (settings.gradle.kts). |

## 2. AndroidX / Compose core (already present — verified current)

| Library | Pinned version | Verified on | Purpose |
|---|---|---|---|
| `androidx.compose:compose-bom` | `2026.02.01` | 2026-06-08 | BOM that aligns all `androidx.compose.*` artifacts. **Compose artifact versions are governed by this BOM — do not pin them individually.** |
| `androidx.compose.material3:material3` | (via BOM) | 2026-06-08 | Material 3 components + theming (our pastel theme is built on M3 color roles). |
| `androidx.compose.ui:ui` / `ui-graphics` / `ui-tooling` / `ui-tooling-preview` | (via BOM) | 2026-06-08 | Compose UI runtime, graphics (gradients/brushes), previews. |
| `androidx.activity:activity-compose` | `1.13.0` | 2026-06-08 | `ComponentActivity` + `setContent`, Activity Result APIs (SAF folder picker). |
| `androidx.core:core-ktx` | `1.19.0` | 2026-06-08 | Core KTX extensions. |
| `androidx.lifecycle:lifecycle-runtime-ktx` | `2.10.0` | 2026-06-08 | Lifecycle-aware coroutine scopes. |

## 3. New libraries we are ADDING for this app (verified latest stable, 2026-06-08)

| Library | Pinned version | Latest stable? | Verified on | Why chosen / alternatives considered |
|---|---|---|---|---|
| `androidx.lifecycle:lifecycle-viewmodel-compose` | `2.10.0` | ✅ stable (2.11.0 is **beta** — NOT used) | 2026-06-08 | `viewModel()` in Compose, ViewModel scoping. Keeps us on the same lifecycle train as 2.10.0. |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | `1.11.0` | ✅ stable | 2026-06-08 | Structured concurrency for the extraction pipeline + `Dispatchers.Main`. Declared explicitly even though Compose pulls it transitively, so the version is visible & lockable. |
| `io.coil-kt.coil3:coil-compose` | `3.4.0` | ✅ stable (3.5.0 is **beta** — NOT used) | 2026-06-08 | App-icon loading with memory/disk caching for long lists. Coil 3 (KMP, `coil3` package). **Alternatives:** Glide (used by SAI, but View-based, no first-class Compose API), Landscapist (wrapper, extra layer). Coil 3 is the modern Compose-native choice. We use a **custom `Fetcher`/`Keyer`** to load `Drawable`s straight from `PackageManager` (no network module pulled in → stays offline). |
| `androidx.datastore:datastore-preferences` | `1.2.1` | ✅ stable | 2026-06-08 | Typed, async, flow-based persistence for settings (theme mode, sort order, persisted SAF tree URI). **Alternatives:** SharedPreferences (legacy, synchronous, discouraged in 2026), Proto DataStore (overkill for simple prefs). |
| `androidx.core:core-splashscreen` | `1.2.0` | ✅ stable | 2026-06-08 | Backwards-compatible splash screen API for a polished cold-start (part of the "visually stunning" goal). |
| `androidx.documentfile:documentfile` | `1.1.0` | ✅ stable | 2026-06-08 | Ergonomic writes into the user-picked SAF tree (create files/dirs in the chosen export folder). **Alternative:** raw `DocumentsContract` (more boilerplate). |

## 4. Deliberately NOT used (and why) — anti-bloat / privacy decisions

| Candidate | Decision | Reason |
|---|---|---|
| `com.android.tools.build:apksig` | ❌ Not used | Signing certs/info are read via `PackageInfo.signingInfo` + `java.security.cert.CertificateFactory` (platform APIs). No third-party code parses the APK. |
| `net.lingala.zip4j:zip4j` | ❌ Not used | Split-APK bundling uses `java.util.zip.ZipOutputStream` (platform). Smaller surface, no native code. |
| `org.smali:baksmali` / `smali` | ❌ Not used | We are an **extractor/inspector**, not a decompiler/editor. Out of scope (see APK-Explorer-Editor ref app — different product). |
| `rikka.shizuku:*` (Shizuku/Sui) | ❌ Not used (v1) | User chose **pure non-root**. PackageManager already exposes every installed app's APK path. Keeps zero special permissions. (Revisit only if a future requirement needs it.) |
| `androidx.navigation:navigation-compose` / `androidx.navigation3:*` | ❌ Not used (v1) | Navigation3 is only at RC; classic nav adds a dependency for a 2-tab + detail app. We use **state-hoisted navigation** (sealed `Screen` state) — zero deps, full control, smallest attack surface. Documented as a future swap-in if the screen graph grows. |
| `com.google.firebase:*`, `crashlytics`, `analytics`, `billing` | ❌ **Banned** | App is **offline-only, privacy-first, no telemetry**. No network, no tracking, ever. (SAI uses these behind a flavor — we do not.) |
| `com.github.bumptech.glide:glide` | ❌ Not used | Replaced by Coil 3 (Compose-native). |
| `androidx.compose.material:material-icons-extended` | ❌ Not used | Large, and frozen/deprecated in 2025. We bundle only the specific vector icons we need + Material 3 core icons. |
| Any networking lib (OkHttp/Retrofit/Ktor) | ❌ **Banned** | Offline-only. The app declares **no `INTERNET` permission** (see `architecture.md`). |

## 5. Proposed `gradle/libs.versions.toml` additions (apply in Task T-001)

```toml
[versions]
# --- additions (see libraries-used.md for verification dates) ---
lifecycleViewmodelCompose = "2.10.0"
coroutines = "1.11.0"
coil = "3.4.0"
datastore = "1.2.1"
splashscreen = "1.2.0"
documentfile = "1.1.0"

[libraries]
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version = { strictly = "2.10.0" } }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version = { strictly = "1.11.0" } }
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version = { strictly = "3.4.0" } }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version = { strictly = "1.2.1" } }
androidx-core-splashscreen = { group = "androidx.core", name = "core-splashscreen", version = { strictly = "1.2.0" } }
androidx-documentfile = { group = "androidx.documentfile", name = "documentfile", version = { strictly = "1.1.0" } }
```

> When applying: add the matching `implementation(libs.…)` lines in `app/build.gradle.kts`, then run
> `./gradlew :app:dependencies --write-locks` and commit the generated lockfile.

---

### Sources verified 2026-06-08
- Coil 3 — https://central.sonatype.com/artifact/io.coil-kt.coil3/coil-compose and https://github.com/coil-kt/coil/releases
- DataStore — https://developer.android.com/jetpack/androidx/releases/datastore , https://mvnrepository.com/artifact/androidx.datastore/datastore-preferences
- kotlinx.coroutines — https://github.com/Kotlin/kotlinx.coroutines/releases
- core-splashscreen — https://developer.android.com/jetpack/androidx/releases/core
- lifecycle — https://developer.android.com/jetpack/androidx/releases/lifecycle
- documentfile — https://developer.android.com/jetpack/androidx/releases/documentfile
