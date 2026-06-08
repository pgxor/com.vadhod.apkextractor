# rules.md

> Hard rules for anyone (human or AI) writing code in this repo. These are **constraints**, not
> suggestions. If a rule blocks you, stop and raise it in `progress.md` / ask — do not silently
> work around it. Ordered roughly by importance.

## A. Privacy & security (highest priority — violations are bugs)

1. **No `INTERNET` permission. Ever.** The app is offline-only. Do not add networking code or any
   dependency that opens sockets. A guardrail test enforces this.
2. **No telemetry/analytics/crash-reporting/ads/billing.** No Firebase, no Crashlytics, no GA, no
   third-party SDK that transmits data. Nothing leaves the device except files the **user explicitly
   exports** via SAF.
3. **Pure non-root.** No `su`, no Shizuku/Sui, no reflection into hidden APIs to escalate. Use only
   public `PackageManager` + SAF.
4. **Least privilege.** Storage only via the Storage Access Framework — **no**
   `READ/WRITE_EXTERNAL_STORAGE`, **no** `MANAGE_EXTERNAL_STORAGE`. The only sensitive permission is
   package visibility (`QUERY_ALL_PACKAGES`), and it must stay justified and documented.
5. **No new permission** gets added without an entry in `progress.md` explaining why and a note in
   `architecture.md` §9.
6. **Release builds:** R8/minify + resource shrink on; `debuggable=false`; no logging of sensitive
   data; review `allowBackup`.

## B. Dependency locking (no downgrades — the user's explicit requirement)

7. **`libraries-used.md` is law.** Every dependency + version is recorded there with a verified date.
   **Never downgrade** a pinned version. Upgrades to a newer **stable** release are allowed *only*
   with an updated "Verified on" date and a `progress.md` line.
8. **No pre-release versions** (`-alpha`/`-beta`/`-rc`/`-dev`/snapshots) without an explicit human
   decision recorded in `libraries-used.md`.
9. **All versions live in `gradle/libs.versions.toml`.** No inline hard-coded versions in
   `build.gradle.kts`. Critical libs use `version = { strictly = "x.y.z" }`.
10. **Re-verify versions online** before adding/upgrading — do not trust memorized/knowledge-cutoff
    numbers; they are stale. Update the date column.
11. **Commit lockfiles** once the dependency set stabilizes; keep the Gradle wrapper pinned.
12. **Justify every new dependency** in `libraries-used.md` (purpose + alternatives considered).
    Prefer a platform API over a library when one exists (we did this for apksig/zip4j).

## C. Architecture & code style

13. **MVVM + UDF.** UI is a pure function of immutable state (`StateFlow`); events flow up. No
    business logic in composables; no Android framework types leaking into the domain model.
14. **Single source of truth** per concern via repositories (`PackageRepository`,
    `SettingsRepository`, etc.). ViewModels orchestrate; data layer does I/O.
15. **All file/IPC/PackageManager I/O off the main thread** (`Dispatchers.IO`), cancellable, with
    progress where user-visible. Never block the UI thread.
16. **Kotlin official style** (`kotlin.code.style=official`). Explicit visibility for public API.
    Prefer immutability (`val`, `data class`, immutable collections in state).
17. **No god-files.** Keep composables small and reusable; design-system components live in
    `core/design/components` and are reused — **never** re-implement a styled element inline.
18. **Errors are typed and surfaced**, never swallowed. Extraction never crashes the app; it returns
    `ExtractResult.Failure`.

## D. Design system (the pastel rules)

19. **Use the design system, not magic values.** Colors, gradients, shapes, type, spacing come from
    `core/design`. No hard-coded hex, no inline `Brush`, no ad-hoc `dp` corner radii in feature code.
20. **Pastel discipline:** soft, low-chroma colors and very low-contrast gradients only. No harsh
    saturated colors, no pure black (`#000`) or pure white text on color.
21. **Contrast is mandatory:** every text/background pair meets **WCAG AA (≥4.5:1)**. Pastel is not
    an excuse for unreadable UI. Verify with a contrast check.
22. **Respect system settings:** dark mode, font scale, and reduce-motion are honored.
23. **All user-facing strings in `strings.xml`** (no hard-coded UI text) for future localization.

## E. Process & documentation

24. **Small, verifiable steps.** Follow `tasks.md` task-by-task. Each task ends in a build that
    compiles and (where applicable) tests that pass. Do **not** attempt everything at once.
25. **Update the docs as you go:**
    - `progress.md` — append what changed + verification result, dated.
    - `tasks.md` — flip task status; add follow-ups discovered.
    - `todo.md` — keep the near-term checklist honest.
    - `architecture.md` — update when a structural decision changes.
26. **The build must stay green.** Run `./gradlew assembleDebug` (and relevant tests) before
    marking a task done. Record the result in `progress.md`.
27. **Reference apps in `DATA/` are read-only inspiration.** Do **not** copy code wholesale or copy
    incompatible-licensed code; study patterns and re-implement cleanly for our stack.
28. **Conventional, descriptive commits.** Don't commit secrets, keystores, or `local.properties`.
    Don't commit unless asked. End AI-authored commit messages with the required co-author trailer.
29. **Ask when genuinely blocked** by a product decision (use `questionnaire.md`); otherwise pick the
    documented default and proceed.
