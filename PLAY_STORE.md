# Publishing Vadhod APK Extractor to Google Play

A complete, ordered guide to get this app onto the Play Store. Verified against Google Play policy as of
**July 2026**. Work top to bottom.

---

## 0. Quick checklist

- [ ] Play Console account active (you have this).
- [ ] **Know your account type** — personal accounts have a mandatory 12-tester / 14-day closed test
      *before* production (see §1). Plan for this early; it's the #1 delay.
- [ ] Upload keystore created and backed up safely (§2).
- [ ] Signed release **`.aab`** built (§2).
- [ ] Store listing text ready (§3 — drafted for you below).
- [ ] Graphics ready: 512 icon ✅ (already generated), feature graphic, 4–8 phone screenshots (§4).
- [ ] Privacy policy hosted at a public URL (§5.1 — draft in `store-assets/PRIVACY_POLICY.md`).
- [ ] Data safety form, content rating, target-audience, and **`QUERY_ALL_PACKAGES` declaration** done (§5).
- [ ] App uploaded to a track → tested → submitted for review (§6).

---

## 1. Account type & the testing requirement (read this first)

Google Play requires **new personal developer accounts created after 13 Nov 2023** to run a **closed
test with at least 12 testers who stay opted-in for 14 continuous days** before you can promote your
first app to production. **Organization accounts are exempt** and can publish straight to production.

- "Opted-in" = the tester accepted your opt-in link **and installed the app** on a real device under
  the matching Google account. Invited-but-not-installed does **not** count. No emulators/duplicates.
- If a tester drops out and you fall below 12, the 14-day clock breaks.

**What to do:** create a **Closed testing** track first (§6), recruit 12 real testers (friends, a
testing-exchange community, etc.), keep them in for 14 days, then apply for production access. If you're
on an **organization** account you can skip straight to production.

> Sources: [Play Console Help – testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465),
> [12 testers / 14 days explained](https://www.testfi.app/blog/google-play-closed-testing-requirement-explained).

---

## 2. Build a signed release bundle (`.aab`)

Play requires the **Android App Bundle** (`.aab`), not an APK, for new apps.

### 2.1 Create your upload key (one time)
`keytool` ships with your JDK. From the project root, run (replace passwords):

```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -genkeypair -v `
  -keystore upload-keystore.jks -alias upload `
  -keyalg RSA -keysize 2048 -validity 10000 `
  -storepass YOUR_STORE_PASSWORD -keypass YOUR_KEY_PASSWORD `
  -dname "CN=Vadhod, OU=Apps, O=A5E Consulting, L=City, ST=State, C=IN"
```

- **Back this `.jks` file up in two safe places.** If you lose it you can reset it via Play (since you'll
  use Play App Signing), but keep it safe anyway. **Never commit it** (already git-ignored).

### 2.2 Point Gradle at the key
The build is already wired to read a git-ignored `keystore.properties`. Copy the example and fill it in:

```powershell
Copy-Item keystore.properties.example keystore.properties
```
Edit `keystore.properties` (paths are relative to the project root — or use an absolute path to a `.jks`
kept outside the repo):
```
storeFile=upload-keystore.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=upload
keyPassword=YOUR_KEY_PASSWORD
```

### 2.3 Build the bundle
```powershell
.\gradlew.bat bundleRelease
```
Output: **`app\build\outputs\bundle\release\app-release.aab`** — this is what you upload.

> If `keystore.properties` is absent the release bundle is left **unsigned** (safe default for other
> machines/CI). With it present, the `.aab` is signed with your upload key.

### 2.4 Play App Signing
When you create the app in Play Console, **enable Play App Signing** (default). You upload with your
**upload key**; Google holds the real **app signing key** and re-signs. This is why a lost upload key is
recoverable.

### 2.5 Bump versions for every release
In `app/build.gradle.kts`, increase `versionCode` (integer, must go up every upload) and update
`versionName` (user-visible, e.g. `1.0` → `1.1`) each time you ship.

---

## 3. Store listing text (drafted — edit to taste)

Paste into Play Console → **Main store listing**. Character limits noted.

**App name** (≤30):
```
Vadhod APK Extractor
```

**Short description** (≤80):
```
Extract & back up any app's APK. 100% offline, private — no ads, no tracking.
```

**Full description** (≤4000):
```
Vadhod APK Extractor is a beautiful, privacy-first tool to extract and back up the APK of any app
installed on your device — completely offline.

Modern apps ship as "split APKs". Vadhod handles them properly: simple apps export as a clean single
.apk, while split apps are bundled into one reinstallable .apks archive automatically. Your backups
actually reinstall.

★ PRIVATE BY DESIGN
• 100% offline — the app has NO internet permission at all.
• No tracking, no analytics, no ads, no accounts.
• Nothing ever leaves your device. Files are saved only to the folder you choose.

★ EXTRACT & BACK UP
• Browse every installed app, split into System and User tabs.
• Search and sort by name, size, install date or last update.
• Extract a single app, or select many and batch-extract at once.
• Save to any folder using Android's Storage Access Framework.

★ SPLIT-APK AWARE
• Export the base APK, or bundle base + splits into a reinstallable .apks archive.

★ INSPECT & SHARE
• See version, size, min/target SDK, install dates.
• View the signing certificate (SHA-256) and the APK's contents.
• Share an extracted APK, or export any app's icon as a PNG.

★ BEAUTIFUL
• A soft, calm pastel design with light and dark themes.

Vadhod APK Extractor is open source and built to respect you: minimal permissions, minimal footprint,
zero data collection.

Note: The "Query all apps" permission is used solely to list the apps installed on your device so you
can extract them. This information stays on your device and is never collected or transmitted.
```

**Other listing fields**
- **App category:** Tools
- **Tags:** choose Utilities / Tools relevant tags
- **Contact email:** parjanyagala@gmail.com
- **Website:** (optional — your GitHub repo is a good choice)
- **Privacy Policy URL:** required (see §5.1)

---

## 4. Graphic assets — specs & where to make them

| Asset | Spec | Status |
|-------|------|--------|
| **App icon** | 512×512, 32-bit PNG (alpha ok) | ✅ `store-assets/ic_play_512.png` |
| **Feature graphic** | 1024×500, PNG/JPEG, **no alpha** (required) | ⬜ make it |
| **Phone screenshots** | 2–8 images, PNG/JPEG **no alpha**, 16:9 or 9:16, each side 320–3840 px | ⬜ capture 4–8 |
| **7" tablet** (optional) | up to 8, same rules | optional |
| **10" tablet** (optional) | up to 8, same rules | optional |
| **Promo video** (optional) | YouTube URL | optional |

> Note on the icon: Play shows the **listing** icon with its own rounded-corner mask, so the 512 was
> generated with ~6% padding to protect the arrow/box corners. (Your **launcher** icon on the phone
> stays the full, unmasked shape.) If the transparent background looks odd on Play, ask me and I'll
> render a version on a soft background.

### Capture great screenshots
1. Take raw screenshots on your phone (or via adb): the app list, the extraction sheet with live %, an
   app-detail/inspector screen, the settings/dark theme, and an onboarding screen. Aim for 4–6.
   - adb: `adb exec-out screencap -p > shot1.png`
2. Drop them into a **device frame + caption** tool to make them look polished:

**Free / freemium screenshot & mockup tools**
- **AppMockUp** — https://app-mockup.com (free, Play/App-Store presets, device frames, captions)
- **Previewed** — https://previewed.app (3D device mockups)
- **Screenshots.pro / Studio** — https://screenshots.pro
- **Hotpot App Store Screenshot** — https://hotpot.ai/templates/app-store-screenshot
- **AppScreens** — https://appscreens.com
- **Shotbot** — https://shotbot.io
- **Mockuphone** — https://mockuphone.com (simple free device frames)
- **Canva** — Play Store screenshot & **feature-graphic** templates (great for the 1024×500)
- **Figma** — device-frame/mockup community plugins + Play screenshot templates

Make the **feature graphic (1024×500)** in Canva or Figma: the box icon + "Extract any APK. 100%
offline." on a pastel background matching the app.

---

## 5. Content & policy declarations (Play Console → "App content")

### 5.1 Privacy policy (required)
Host the draft in `store-assets/PRIVACY_POLICY.md` at a public URL and paste that URL in the listing.
Easiest free option: **GitHub Pages** (put it in a repo, enable Pages) or paste into a generator:
- App Privacy Policy Generator (nisrulz): https://app-privacy-policy-generator.nisrulz.com
- TermsFeed: https://www.termsfeed.com  •  FreePrivacyPolicy: https://www.freeprivacypolicy.com

### 5.2 Data safety form
This app collects nothing, so it's quick: **"No data collected"**, **no data shared**, and (optionally)
note that data isn't encrypted-in-transit because there's no transmission at all. Answer "No" to all
collection categories.

### 5.3 Sensitive permission: `QUERY_ALL_PACKAGES` (important)
Your app declares `QUERY_ALL_PACKAGES`, which is a **sensitive permission** requiring a **Permissions
Declaration** in App content. This is the most likely thing to get a review query, so be precise:
- **Core purpose:** the app is an APK backup/extractor; it must enumerate installed apps so the user can
  select which to extract. This is a documented allowed use ("device search"/app-management style tools).
- Fill the declaration explaining exactly that, and confirm the data isn't transmitted.
- If a narrower alternative is ever acceptable, the app could list only launchable apps without this
  permission — but for a true extractor, full visibility is the intended function.

### 5.4 Content rating
Complete the IARC questionnaire — it's a utility with no objectionable content → expect **Everyone /
PEGI 3**.

### 5.5 Other App-content sections
- **Target audience & content:** not directed at children; general/adult audience is simplest.
- **Ads:** declare **no ads**.
- **News / COVID / financial / health / government:** all **No**.
- **Government / financial features:** No.

---

## 6. Release tracks & submission

1. **Create the app** in Play Console (default language, app name, free, declarations that it's an app).
2. **Enable Play App Signing** (§2.4).
3. Start with **Testing → Internal testing**: upload `app-release.aab`, add yourself, verify install
   from the Play internal link on a device.
4. **Closed testing** (required for personal accounts, §1): add ≥12 testers, keep them 14 continuous
   days. Meanwhile finish store listing + all App-content declarations.
5. **Production**: once eligible, create a production release, set **countries/regions**, roll out
   (consider a **staged rollout**, e.g. 20%).
6. **Review**: first review can take a few days. Address any policy feedback (most likely the
   `QUERY_ALL_PACKAGES` declaration).

---

## 7. After launch
- Bump `versionCode`/`versionName` for every update (§2.5).
- Watch the **pre-launch report** (Play runs your app on real devices) and **Android vitals** (crashes/ANRs).
- Reply to reviews; keep the listing screenshots current as the UI evolves.

---

## Files in this repo for publishing
- `store-assets/ic_play_512.png` — 512×512 Play listing icon.
- `store-assets/PRIVACY_POLICY.md` — privacy policy to host.
- `keystore.properties.example` — copy to `keystore.properties` (git-ignored) and fill in.
- Build: `.\gradlew.bat bundleRelease` → `app\build\outputs\bundle\release\app-release.aab`.
