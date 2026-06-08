# tasks.md

> The full, ordered backlog. Each task is **small and verifiable** and ends in a green build.
> Status legend: ⬜ todo · 🟦 in-progress · ✅ done · ⏸ blocked · ❌ dropped.
> When you finish a task: flip its status, add a dated line to `progress.md`, and run the stated
> verification. Do **not** batch many tasks blindly — one at a time, build between.
>
> Phases: **0** Foundations → **1** Data/core (correctness) → **2** UI shell & list → **3** Extract
> UX → **4** Details/inspect → **5** Pastel polish → **6** Hardening/release.

## Phase 0 — Foundations & tooling
- ✅ **T-001** Apply dependency additions to `gradle/libs.versions.toml` with `strictly` pins; wire
  `implementation(...)` in `app/build.gradle.kts`. *Verified 2026-06-08:* `assembleDebug` green; all
  six new libs resolve.
- ⏸ **T-002** Gradle dependency lockfiles — **deferred to near-release** (per `libraries-used.md`
  §0.3, "once the dependency set is final"). The `strictly` pins already hard-block downgrades, so
  this is reproducibility polish, not a gate. Avoids lockfile churn during active dev.
- ✅ **T-003** Guardrail test `NoInternetPermissionTest` asserts **no `INTERNET`/`ACCESS_NETWORK_STATE`**
  in the manifest. *Verified 2026-06-08:* `testDebugUnitTest` green. (Merged-manifest/instrumented
  variant tracked for T-064.)
- ✅ **T-004** Package structure established (`core/util` created; `core/`, `data/`, `feature/`,
  `core/design/` to grow as real files land — empty dirs intentionally not committed). *Verified:* compiles.
- ✅ **T-005** `DispatcherProvider`/`DefaultDispatcherProvider` + `AppResult`/`AppError` + `appResult{}`
  in `core/util`. *Verified 2026-06-08:* `AppResultTest` green (incl. cancellation re-throw).

## Phase 1 — Data layer & core correctness (do BEFORE UI)
- ✅ **T-010** `AppEntry` (+`allApkPaths`/`isSplit`), `SortOrder` (+`sortedByOrder`), `ExportFormat`,
  `ThemeMode`. *Verified 2026-06-08:* `AppEntryAndSortTest` green.
- 🟦 **T-011** `PackageManagerSource`: enumerates installed packages, maps to `AppEntry`, classifies
  system vs user, resolves base + split paths (SDK-compat for `PackageInfoFlags`). **Compiles;
  on-device listing verification pending** — will be exercised when the list UI lands (Phase 2) or
  via an added instrumented test.
- ✅ **T-012** Size computation (base+splits) done in `PackageManagerSource` on IO dispatcher.
  *(Per-package cache to add if profiling in T-061 shows a need.)*
- ⬜ **T-013** *(optional)* Raise JVM target 11→17/21 if toolchain supports; else defer.
- ✅ **T-014** `PackageRepository` (+`getPartitioned()` → user/system). *Verified:* compiles; used by tests.
- ✅ **T-015** `ApkExtractor` **SingleBaseApk**: stream-copy with progress + cancellation + sanitized
  filename; `ApkSink` abstraction + `SafApkSink`. *Verified:* `ApkExtractorTest` byte-exact copy.
- ✅ **T-016** `SplitBundler` **BundleApks**: zips base+splits via `ZipOutputStream` → `.apks`.
  *Verified:* test re-opens archive, all entries present, byte-exact.
- ✅ **T-017** Failure matrix: typed `AppError` mapping (NotFound / OutOfSpace / PermissionDenied /
  Io); cancellation re-thrown. *Verified:* failure-injection tests return `AppResult.Failure`, no crash.
- ✅ **T-018** `SettingsRepository` (DataStore): theme mode, sort order, persisted SAF tree URI,
  bundle-default flag; corrupt-enum fallback. *Verified:* compiles. *(Read/write test → T-061 polish.)*
- ✅ **T-019** `ApkInspector`: signing SHA-256 via `PackageInfo.signingInfo` + zip-entry listing.
  *Verified:* compiles. *(On-device value check in Phase 4 T-040.)*

## Phase 2 — UI shell, theme scaffold & app list
- ⬜ **T-020** `AppTheme()` scaffold: M3 ColorSchemes wired to the pastel palette (placeholder hex
  from architecture §8), Shapes, Type, light/dark. *Verify:* preview renders.
- ⬜ **T-021** `Coil` `ImageLoader` + custom `PackageManager` icon `Fetcher`/`Keyer`; `AppIcon`
  composable with shimmer placeholder. *Verify:* icons render in a preview/list on device.
- ⬜ **T-022** App shell: single Activity, splash screen, `Scaffold`, **pill-style two tabs**
  (System / User). *Verify:* tabs switch on device.
- ⬜ **T-023** `AppListViewModel` + `AppListUiState` (loading/empty/content) per tab; wire repository.
  *Verify:* both tabs populate.
- ⬜ **T-024** `AppListItem` component (icon, label, package, size, split badge). *Verify:* preview + device.
- ⬜ **T-025** Search bar (filter by label/package) + sort menu (SortOrder). *Verify:* filtering works.

## Phase 3 — Extraction UX
- ⬜ **T-030** SAF folder picker (`ACTION_OPEN_DOCUMENT_TREE`) + persist permission; first-run prompt.
  *Verify:* chosen folder persists across restarts.
- ⬜ **T-031** Single-tap extract from a list item → format choice for split apps (base vs bundle) →
  progress sheet → success/share. *Verify:* APK appears in chosen folder; reinstallable.
- ⬜ **T-032** Multi-select mode + batch extract with per-item progress and partial-success summary.
  *Verify:* batch of N apps extracts; failures reported individually.
- ⬜ **T-033** Share extracted file via `FileProvider` + `ACTION_SEND`. *Verify:* share sheet opens.

## Phase 4 — Details & inspector
- ⬜ **T-040** `AppDetailScreen`: metadata (versions, sizes incl. each split, dates, min/target SDK,
  system/user, signing SHA-256). *Verify:* values correct vs system settings.
- ⬜ **T-041** APK inspector: lazy zip-entry list of base.apk. *Verify:* entries match `unzip -l`.
- ⬜ **T-042** Icon export to PNG via SAF. *Verify:* PNG saved, correct image.

## Phase 5 — Pastel visual polish (headline)
- ⬜ **T-050** Finalize palette from `questionnaire.md` answers; replace placeholder hex; pass AA
  contrast checks. *Verify:* contrast test + visual review.
- ⬜ **T-051** Gradient system (`Gradients.kt`): background, FAB, tab indicator, card accents.
  *Verify:* previews in light/dark.
- ⬜ **T-052** Frosted "glass" cards / soft elevation, large rounded shapes, spacing scale. *Verify:* review.
- ⬜ **T-053** Motion: crossfade list↔detail, springy FAB, shimmer loaders; respect reduce-motion.
  *Verify:* on device.
- ⬜ **T-054** Empty/error/loading states styled on-brand. *Verify:* simulate each.
- ⬜ **T-055** App icon + adaptive icon + splash in pastel identity. *Verify:* launcher + cold start.
- ⬜ **T-056** Settings screen (theme mode, default folder, dynamic-color toggle if enabled, about/license).

## Phase 6 — Hardening, accessibility, release
- ⬜ **T-060** Accessibility pass: TalkBack labels, focus order, font-scale, touch targets ≥48dp.
- ⬜ **T-061** Performance: large device (500+ apps) — list scroll, icon caching, size calc. *Verify:* profile.
- ⬜ **T-062** R8/minify + resource shrink for release; keep rules; verify nothing breaks. *Verify:* release build runs.
- ⬜ **T-063** Manifest hardening review (exported flags, FileProvider scope, allowBackup). 
- ⬜ **T-064** Final guardrail/security test sweep + `libraries-used.md` re-verification.
- ⬜ **T-065** README, LICENSE (per Q9), screenshots, F-Droid/Play metadata (no trackers).
- ⬜ **T-066** Localization scaffolding (strings audited, no hard-coded UI text).

## Backlog / future (post-v1)
- ⬜ Optional Shizuku path (only if a concrete need arises) · ⬜ Navigation3 swap-in if graph grows ·
  ⬜ APK metadata search (permissions/components) · ⬜ Export history · ⬜ Multi-module split.
