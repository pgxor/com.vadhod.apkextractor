# progress.md

> Append-only log of what actually happened, newest at top. One entry per working session or
> meaningful change. Record: date, what changed, verification result (build/tests), and decisions.
> This is how a future session knows the true state without re-deriving it.

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
