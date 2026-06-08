# questionnaire.md

> Fill this in whenever convenient — **none of it blocks** starting Phase 0–1 (foundations + the
> core extraction engine). It _is_ needed before Phase 5 (pastel visual polish) and release.
> For each question: a **recommended default** is given so work can proceed if you skip it.
> Answer inline under each question (e.g. `Answer: ...`). I'll fold answers into `architecture.md`
> and the design system, and log the change in `progress.md`.

---

## Part 1 — Brand & identity

**Q1. Final app name?**
Working name is "APK Extractor" (package `com.vadhod.apkextractor`). Want a distinct brand
(e.g. "Pastel APK", "SoftExtract", "Lumen APK", "Petal")? The package name can stay as-is.

- _Recommended:_ keep "APK Extractor" as the functional name; optionally add a softer brand subtitle.
- **Answer:** Vadhod APK Extractor

**Q2. Pastel palette — do you approve the proposed seed colors, or have specific hex values?**
Proposed (architecture §8): Lavender `#E9E3FB`/`#CDBDF2`, Sky `#DCEBFB`, Mint `#DCF4E8`,
Peach `#FCE7D9`, Blush `#FBE1EE`, Butter `#FBF3D9`, Surface `#FBFAFF`, Ink `#3B3656`.

- _Recommended:_ approve as a starting point; we tune for AA contrast during T-050.
- **Answer:** use the anthropic claude website pastel colors

**Q3. Which hue should be the PRIMARY/brand accent** (used for the FAB, tab indicator, key actions)?

- Options: Lavender (recommended) · Sky · Mint · Peach · Blush · a rotating/gradient accent.
- **Answer:** use the anthropic claude website pastel colors

**Q4. Dark mode?**

- (a) Light only · (b) Light + "dim pastel" dark (recommended) · (c) follow system, both polished.
- **Answer:** b

**Q5. Material You dynamic color toggle?**
Let the OS wallpaper recolor the app (Android 12+)?

- _Recommended:_ off by default (preserves the signature pastel look), available as a user toggle.
- **Answer:** no

**Q6. Typeface?**

- (a) System default (smallest size) · (b) bundled rounded font — _Nunito_ (recommended) /
  _Quicksand_ / _Comfortaa_ (offline, adds a few hundred KB).
- **Answer:** b

**Q7. App icon direction?**
A pastel gradient mark — e.g. a soft "box opening / extracting" glyph, or a stylized APK/Android
motif on a lavender-sky gradient. Any preference, or shall I propose 2–3 concepts?

- **Answer:** yes

---

## Part 2 — Functional details

**Q8. Package visibility stance (privacy vs completeness):**
To list _all_ installed apps on Android 11+, we need the `QUERY_ALL_PACKAGES` permission. Options:

- (a) Use `QUERY_ALL_PACKAGES` to show everything (recommended for an extractor; Play requires
  justification, which we have).
- (b) Stricter: only list apps with a launcher entry (no special permission, but hides some apps).
- (c) Provide both as a setting.
- **Answer:** a

**Q9. Default split-APK export format** when an app HAS splits:

- (a) Default to **bundle `.apks`** with an option for base-only (recommended) ·
  (b) Always ask each time · (c) Default base-only with option to bundle.
- **Answer:** a

**Q10. Archive extension for bundles?**

- `.apks` (SAI-compatible, recommended) · `.apkm` · `.xapk` · `.zip`. (Format is a plain zip either way.)
- **Answer:** yes

**Q11. Default sort order** for the lists?

- Name A–Z (recommended) · Size (largest first) · Recently updated · Recently installed.
- **Answer:** yes but give all options

**Q12. minSdk** — keep **29** (Android 10, recommended) or raise (e.g. 33/34 for newer-only) or
lower (more legacy coverage, reintroduces legacy-storage complexity — discouraged)?

- **Answer:** 10+

**Q13. Filename template for extracted files?**
Proposed: `<AppLabel>_<versionName>_<versionCode>.apk` (sanitized). Prefer package name instead, or
include date?

- **Answer:** yes

**Q14. Should we also show & let users export the app's icon (PNG)?** (Already planned for v1, T-042.)
Keep it, or drop to reduce scope?

- **Answer:** yes

**Q15. Inspector depth for v1** — list zip entries + signing SHA-256 + manifest summary (recommended),
or richer (per-permission view, certificate details, file preview)? Richer = more later work.

- **Answer:** yes

---

## Part 3 — Project & release

**Q16. Open-source license?**

- GPL-3.0 (matches the genre/SAI, strong copyleft) · MIT · Apache-2.0 (recommended for permissive +
  patent grant) · other.
- **Answer:** fdroid

**Q17. Distribution targets?**

- F-Droid (great fit: no trackers, reproducible) · Google Play · GitHub Releases (APK) · all.
- _Note:_ Play requires a `QUERY_ALL_PACKAGES` justification; F-Droid is the most natural home.
- **Answer:** play store/git/fdroid

**Q18. Versioning & language for UI strings?**
Start at `1.0`? English-only for v1 with localization scaffolding (recommended), or target specific
languages now?

- **Answer:** en

**Q19. Anything from the reference apps you specifically liked** and want replicated (a particular
SAI/APK-Explorer behavior or screen)? Or anything you explicitly do **not** want?

- **Answer:** good ui

**Q20. Anything else** — features, constraints, inspirations, hard "must-haves" or "never-do"s I
should bake into `rules.md` / `architecture.md`?

- **Answer:** no
