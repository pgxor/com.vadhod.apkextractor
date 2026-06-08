# progress.md

> Append-only log of what actually happened, newest at top. One entry per working session or
> meaningful change. Record: date, what changed, verification result (build/tests), and decisions.
> This is how a future session knows the true state without re-deriving it.

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
