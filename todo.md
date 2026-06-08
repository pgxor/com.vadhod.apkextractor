# todo.md

> The **near-term, actionable** checklist (the next few sessions). The full backlog lives in
> `tasks.md`; this file stays short and honest. Check items off as you go and keep it pruned.

## 🔭 Right now — waiting on you (the user)
- [ ] Fill in **`questionnaire.md`** (brand name, exact pastel palette, dark mode, dynamic color,
      typeface, package-visibility stance, minSdk, license, app icon). None of this blocks Phase 0–1,
      but Q1–Q5 + Q9 are needed before the Phase 5 polish and release.
- [ ] Confirm you're happy with the four locked decisions in `architecture.md` §3.

## ▶️ Next coding session (Phase 0 — Foundations)
- [ ] **T-001** Add the six new libraries to `gradle/libs.versions.toml` with `strictly` pins +
      wire them in `app/build.gradle.kts`; `./gradlew assembleDebug` must stay green.
- [ ] **T-002** Turn on dependency locking; commit lockfiles.
- [ ] **T-003** Add the "no INTERNET / no banned deps" guardrail test.
- [ ] **T-004** Create the package skeleton (`core/`, `data/`, `feature/`, `core/design/`).
- [ ] **T-005** Dispatcher provider + Result/error types.

## 🔜 Then (Phase 1 — make extraction bulletproof before any pretty UI)
- [ ] **T-010 → T-019**: model → list apps → sizes → repository → **extractor (single + bundle)** →
      failure matrix → settings → inspector. This is the "core functionality solid" milestone.

## 🧪 Definition of done for the current milestone (Phase 1)
- [ ] Can enumerate installed apps, correctly classified system vs user (verified on the connected
      device `adb-ZD222NX2DM`).
- [ ] Can extract a **simple** app to a SAF folder as `.apk` (reinstallable).
- [ ] Can extract a **split** app as a `.apks` bundle (archive re-opens with all entries).
- [ ] Cancellation + failure cases never crash; return typed results.
- [ ] All of the above covered by tests; `assembleDebug` + tests green; `progress.md` updated.

## ⚠️ Don't forget
- [ ] No `INTERNET`, no trackers, no root — re-read `rules.md` §A/§B before adding anything.
- [ ] Update `progress.md` + `tasks.md` after every task. Build between tasks.
- [ ] Re-verify any library version online before changing it (no downgrades).
