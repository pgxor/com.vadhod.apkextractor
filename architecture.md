# architecture.md

> Working name: **APK Extractor** (final brand name TBD — see `questionnaire.md` Q1).
> Package: `com.vadhod.apkextractor`. This document is the technical blueprint. Keep it in sync as
> the design evolves; every structural decision should be traceable here.

---

## 1. Product in one paragraph

A **secure, open-source, privacy-first, 100% offline** Android app that lists every installed
application — separated into **System** and **User** tabs — and lets the user **extract** any app's
APK to a folder of their choice. Modern apps ship as **split APKs**; the app can export a clean
single `.apk` for simple apps and **bundle base + splits** into a single reinstallable archive for
split apps. Beyond extraction it offers search/sort, batch (multi-select) extraction, an app-details
+ APK-inspector screen, and share/icon-export. The visual identity is the differentiator:
**very soft pastel colors and very soft pastel gradients**, built to be the best-looking app in its
category.

## 2. Non-negotiable principles

1. **Offline-only.** The app declares **no `INTERNET` permission** in the manifest. There is no
   network code, no telemetry, no analytics, no crash reporting, no ads, no third-party SDKs that
   phone home. This is enforced structurally (see §9) and verified in CI/tests.
2. **Privacy-first.** No collection of usage data. No accounts. No background services. Nothing
   leaves the device except files the **user explicitly exports** via the system file picker.
3. **Minimal permissions / minimal attack surface.** Pure non-root. We rely only on
   `PackageManager` + Storage Access Framework. The fewer dependencies and permissions, the smaller
   the surface — see `libraries-used.md` §4 for everything we deliberately *don't* ship.
4. **Open source.** License: TBD (`questionnaire.md` Q9 — recommend GPL-3.0 to match the genre, or
   MIT/Apache-2.0 for permissiveness). Reproducible, dependency-locked builds.
5. **Core correctness before polish.** Extraction must be bulletproof (split handling, large apps,
   cancellation, storage errors) *before* we chase visual flourish — but the UI is the headline
   feature once core is solid.

## 3. Decisions locked in this session (2026-06-08)

| Topic | Decision |
|---|---|
| Access model | **Pure non-root** — `PackageManager` only, zero special permissions. |
| Split APKs | **Both** — single `.apk` for simple apps; **bundle base+splits** (`.apks` zip) for split apps, with an option to export just the base. |
| Save location | **User-picked folder via SAF** (`ACTION_OPEN_DOCUMENT_TREE`), persisted with a takePersistableUriPermission. Default suggestion: a subfolder under Downloads. |
| v1 feature set | Core extract + search/sort; batch multi-select extract; app details + APK inspector; share + icon export. |
| Navigation | State-hoisted sealed `Screen` (no nav library in v1). |
| UI toolkit | Jetpack Compose + Material 3, custom pastel theme. |
| Architecture | Single-Activity, **MVVM + unidirectional data flow (UDF)**, repository pattern. |

### 3.1 Decisions from `questionnaire.md` (answered 2026-06-08)

| Q | Topic | Decision |
|---|---|---|
| Q1 | App name | **"Vadhod APK Extractor"** (package stays `com.vadhod.apkextractor`). |
| Q2/Q3 | Palette | **Anthropic / Claude website palette** — warm ivory surfaces, clay/coral accent, kraft tan, manilla, soft sage, warm charcoal ink (see §8). |
| Q4 | Dark mode | **Light + "dim pastel" dark** (warm charcoal), both polished. |
| Q5 | Dynamic color | **No** — fixed brand palette, no Material You toggle. |
| Q6 | Typeface | **Bundled rounded font — Nunito** (offline asset). |
| Q7 | App icon | Propose 2–3 pastel-gradient concepts (Phase 5, T-055). |
| Q8 | Package visibility | **`QUERY_ALL_PACKAGES`** — list everything. |
| Q9 | Split default | **Bundle `.apks`** by default, base-only optional. |
| Q10 | Bundle extension | **`.apks`**. |
| Q11 | Sort | Default **Name A–Z**; expose **all** `SortOrder` options. |
| Q12 | minSdk | **29** (Android 10) — confirmed (minSdk 10 infeasible with Compose). |
| Q13 | Filename | `<AppLabel>_<versionName>_<versionCode>.apk` (sanitized). |
| Q14 | Icon export | **Yes** (PNG via SAF). |
| Q15 | Inspector depth | Zip entries + signing **SHA-256** + manifest summary. |
| Q16 | License | **GPL-3.0** (F-Droid-friendly; change on request). |
| Q17 | Distribution | **Play Store + GitHub Releases + F-Droid** (Play needs `QUERY_ALL_PACKAGES` justification). |
| Q18 | Language | **English** v1 + localization scaffolding. |
| Q19/Q20 | Other | Prioritize great UI; no extra constraints. |

## 4. Tech stack (see `libraries-used.md` for pinned versions)

- **Language:** Kotlin 2.4.0 (K2), coroutines + Flow.
- **UI:** Jetpack Compose (BOM 2026.02.01), Material 3, custom pastel design system.
- **Async:** kotlinx.coroutines 1.11.0; structured concurrency; `Dispatchers.IO` for file work.
- **State:** `ViewModel` + `StateFlow`; UDF (immutable UI state in, events out).
- **Images:** Coil 3.4.0 with a custom `PackageManager` icon fetcher (offline).
- **Persistence:** DataStore Preferences 1.2.1 (settings + persisted SAF tree URI).
- **Storage I/O:** SAF (`DocumentsContract` / `documentfile` 1.1.0) + `java.util.zip` for bundling.
- **Splash:** core-splashscreen 1.2.0.

## 5. Module & package layout

v1 is a single Gradle module (`:app`). Internally organized by layer/feature so it can be split into
`:core`, `:data`, `:feature-*` modules later without churn.

```
com.vadhod.apkextractor
├─ App.kt                      // Application: Coil ImageLoader setup, DataStore singleton
├─ MainActivity.kt            // single Activity; installs splash; hosts Compose root
├─ core/
│  ├─ model/                  // AppEntry, ApkVariant, ExtractRequest, ExtractResult, SortOrder…
│  ├─ util/                   // formatters (size/date), dispatchers provider, Result types
│  └─ design/                 // ⬅ THE DESIGN SYSTEM (see §8)
│     ├─ color/               // PastelPalette, gradients, M3 ColorSchemes (light/dark)
│     ├─ theme/               // AppTheme(), Shapes, Type, elevation/tokens
│     └─ components/          // reusable: GradientBackground, AppListItem, PillTab,
│                             //   SearchBar, GlassCard, ExtractFab, ProgressSheet…
├─ data/
│  ├─ packages/               // PackageRepository: query installed apps (system vs user)
│  │   ├─ PackageRepository.kt
│  │   ├─ PackageManagerSource.kt
│  │   └─ IconFetcher.kt      // Coil Fetcher/Keyer for app icons
│  ├─ extract/                // ApkExtractor: the extraction pipeline (core correctness)
│  │   ├─ ApkExtractor.kt
│  │   ├─ SplitBundler.kt     // ZipOutputStream → .apks archive
│  │   └─ ExportTarget.kt     // SAF writer (DocumentFile)
│  ├─ inspect/                // ApkInspector: zip entries, manifest summary, signing info
│  └─ settings/               // SettingsRepository (DataStore)
└─ feature/
   ├─ applist/                // AppListViewModel + AppListScreen (two tabs)
   ├─ detail/                 // AppDetailViewModel + AppDetailScreen (+ inspector)
   └─ settings/               // SettingsScreen (theme, default export folder, about)
```

## 6. Core domain model (initial sketch)

```kotlin
data class AppEntry(
    val packageName: String,
    val label: String,
    val versionName: String?,
    val versionCode: Long,
    val isSystem: Boolean,        // (flags & FLAG_SYSTEM) or FLAG_UPDATED_SYSTEM_APP
    val baseApkPath: String,      // ApplicationInfo.sourceDir
    val splitApkPaths: List<String>, // ApplicationInfo.splitSourceDirs (may be empty)
    val totalSizeBytes: Long,     // sum of base + splits
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val minSdk: Int?,
    val targetSdk: Int?,
)

val AppEntry.isSplit: Boolean get() = splitApkPaths.isNotEmpty()

enum class SortOrder { NAME_ASC, NAME_DESC, SIZE_DESC, SIZE_ASC, INSTALL_NEWEST, UPDATED_NEWEST }

sealed interface ExportFormat {
    data object SingleBaseApk : ExportFormat        // base.apk only
    data object BundleApks : ExportFormat           // base + splits zipped → name.apks
}

data class ExtractRequest(val app: AppEntry, val format: ExportFormat, val treeUri: Uri)

sealed interface ExtractResult {
    data class Success(val outputUri: Uri, val bytesWritten: Long) : ExtractResult
    data class Failure(val app: AppEntry, val cause: Throwable) : ExtractResult
}
```

## 7. Key flows

### 7.1 Listing apps (System vs User)
- `PackageManager.getInstalledPackages(PackageManager.GET_META_DATA)` (or `getInstalledApplications`).
- Classify: **System** = `ApplicationInfo.flags & (FLAG_SYSTEM | FLAG_UPDATED_SYSTEM_APP) != 0`;
  **User** = everything else.
- Build `AppEntry` lazily; **icons load on demand via Coil** (never decode all at once).
- Sizes: sum `File(sourceDir).length()` + each split. Compute off the main thread; cache per package.
- Result exposed as `StateFlow<AppListUiState>` per tab; search & sort applied in the ViewModel.
- **Android 11+ package visibility:** to enumerate *all* installed apps we add the
  `QUERY_ALL_PACKAGES` permission (justified: an app extractor's core function is enumerating
  installed apps). Documented in `rules.md` and the Play-listing notes; alternative for stricter
  privacy is to only list launchable apps — see `questionnaire.md` Q6.

### 7.2 Extraction pipeline (the part that must be bulletproof)
1. User picks export folder once (SAF tree URI) → persisted in DataStore.
2. For each selected app, resolve `baseApkPath` (+ splits).
3. **SingleBaseApk:** stream-copy `base.apk` → `DocumentFile.createFile("application/vnd.android.package-archive", "<label>_<version>.apk")`.
4. **BundleApks:** open `ZipOutputStream` on the SAF output stream; add `base.apk` + every split as
   stored/deflated entries → `<label>_<version>.apks`.
5. All I/O on `Dispatchers.IO`, buffered, with **cancellation checks** and **progress callbacks**.
6. Robust error handling: missing/again-uninstalled package, no free space, SAF permission revoked,
   read-protected path → surface a typed `ExtractResult.Failure`, never crash.
7. Batch mode: a coroutine processes the queue, emits per-item progress; partial success allowed.
8. Filenames sanitized; collisions get a numeric suffix.

### 7.3 Inspect / details
- Details: label, package, versions, sizes (base + each split), install/update dates, min/target SDK.
- Signing: `PackageManager.getPackageInfo(pkg, GET_SIGNING_CERTIFICATES)` →
  `signingInfo.apkContentsSigners` → SHA-256 fingerprint via `CertificateFactory` (no third-party lib).
- APK inspector: list zip entries of `base.apk` via `java.util.zip.ZipFile` (read-only, lazy).
- Icon export: render `ApplicationInfo.loadIcon(pm)` to a `Bitmap` → PNG via SAF.

### 7.4 Share
- `FileProvider` + `ACTION_SEND` with the extracted file's content URI (granting read to the target).

## 8. Design system — "very soft pastel" (the headline feature)

> Goal: dreamy, calm, premium — the warm, soft, paper-and-clay aesthetic of the **Anthropic / Claude
> website**, expressed as pastel. Gentle gradients, generous whitespace, large rounded corners, soft
> shadows, subtle motion. Hex values below are the **starting palette**; we tune for AA contrast in
> T-050. The *structure* (palette → M3 roles → tokens → components) is the contract.

- **Color approach:** the Anthropic/Claude palette mapped onto **Material 3 color roles** (so all M3
  components inherit it). **No Material You / dynamic color** (Q5 = no) — the brand palette is fixed
  and signature.
- **Anthropic/Claude pastel palette (light):**
  - Surface / background **Ivory** `#FAF9F5`; secondary surface **Paper** `#F0EEE6`; card `#FFFFFF` with warm tint.
  - **Primary accent — Clay/Coral** `#D97757` (the Claude accent); pressed/deep **Book-cloth** `#CC785C`.
  - **Secondary — Kraft tan** `#D4A27F`; **Tertiary — Manilla** `#EBDBBC`.
  - **Sage** `#C2D2C7` (soft green, success/positive accents).
  - **Ink / on-surface** warm charcoal `#3D3D3A`; strong text `#1F1E1D` (never pure black).
  - **Muted text** `#73726C`; hairlines/outline `#E3E1D9`.
- **Gradients:** very low-contrast, 2–3 stop linear/diagonal `Brush`es in-palette (e.g.
  Ivory→Paper background wash, Clay→Kraft on the extract FAB, Manilla→Sage card accents). Defined
  once in `design/color/Gradients.kt` as factories; **never** hard-coded per screen.
- **Dark theme:** "dim pastel" — warm charcoal surfaces (`#1F1E1D` / `#262624` / `#30302E`) with the
  clay accent kept warm (`#D97757`) and low-chroma tan/sage so it stays soft, not neon. On-dark text
  `#F0EEE6`.
- **Shape:** large corner radii (cards 24–28dp, sheets 28dp, buttons full/20dp pill). Tabs are a
  **pill-style** segmented control.
- **Type:** **Nunito** (Q6) — rounded, friendly, bundled offline as a font resource. Clear M3 type scale.
- **Motion:** gentle—crossfades, shared-element-ish detail transition, springy FAB, shimmer
  placeholders while icons/sizes load. Respect "reduce motion" accessibility setting.
- **Elevation:** soft, diffuse shadows / subtle translucency (frosted "glass" cards) rather than hard
  Material shadows.
- **Accessibility:** pastel-on-pastel risks low contrast — every text/background pair must meet
  **WCAG AA (4.5:1)**; we darken "on-" colors enough to pass. This is a hard rule (`rules.md`).

## 9. Security & privacy architecture (enforced, not aspirational)

- **Manifest:** no `INTERNET`; `usesCleartextTraffic=false`; `allowBackup` reviewed (likely
  `false` to avoid exfiltrating any state); no exported components except what's strictly required;
  `FileProvider` paths scoped narrowly.
- **No network code path exists.** A unit/lint check asserts the absence of `INTERNET` and of
  networking dependencies (see `tasks.md` T-030).
- **No runtime-dangerous permissions** beyond package visibility; storage handled entirely by SAF
  (no `READ/WRITE_EXTERNAL_STORAGE`, no `MANAGE_EXTERNAL_STORAGE`).
- **Reproducible & locked builds:** version catalog + `strictly` + committed lockfiles
  (`libraries-used.md` §0).
- **R8/minify** enabled for release; keep rules minimal; resource shrinking on.
- **Dependency hygiene:** smallest possible dependency set; each addition justified in
  `libraries-used.md`.

## 10. Why minSdk 29

minSdk 29 (Android 10) is the scaffold default and a sensible floor: it guarantees scoped-storage
era APIs, stable SAF behavior, and modern split-APK conventions while still covering the vast
majority of active devices. Lowering it would reintroduce legacy-storage code paths that conflict
with the privacy model. (Raising it is fine if device-coverage data warrants — see questionnaire Q7.)

## 11. Testing strategy (summary — details in tasks.md)

- **Unit:** classification (system/user), sort/search, filename sanitation, split detection, signing
  fingerprint formatting.
- **Pipeline:** extraction against a fake `DocumentFile`/temp dir; cancellation; failure injection
  (no space, revoked permission); split-bundle zip integrity (re-open & verify entries).
- **Instrumented/Compose:** tab switching, search filtering, multi-select, snapshot tests of pastel
  components in light/dark, contrast assertions.
- **Guardrail test:** assert no `INTERNET` permission and no banned dependencies on the classpath.

## 12. Reference apps — what we borrow vs. reject

| Ref app | Borrow | Reject |
|---|---|---|
| **SAI** (`com.aefyr.sai`) | Split-APK model, system/user split, SAF export, app-list UX patterns. | View/XML UI, Glide, Room, Firebase/billing flavors, Shizuku requirement. |
| **APK-Explorer-Editor** | APK zip-entry inspection idea, signing-info display, dependency-metadata stripping (`dependenciesInfo { includeInApk=false }`). | Editing/decompiling (baksmali/smali), broad scope. |
| **apkExtractor** (2017) | The minimal "copy sourceDir to storage" concept only. | Everything else (ancient SDK/support libs). |

## 13. Open questions → see `questionnaire.md`
Brand name, exact palette, dynamic-color toggle, dark mode, typeface, package-visibility stance,
minSdk, license, app icon, and more are tracked there. None block starting the core data layer.
