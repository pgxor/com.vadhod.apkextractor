# F-Droid submission playbook

How to get an app into F-Droid with the fewest review rounds. Written after `!44998`
(Vadhod APK Extractor) took four rounds and seventeen comments, most of which were avoidable.

Everything here is taken from primary sources — `fdroidserver` source, the CI job logs, and the actual
merged metadata of recently accepted apps. Where something is unverified, it says so.

---

## 0. First, the premise: is "skip reproducible builds" actually the easy route?

The 25 most recently merged **New App** merge requests in `fdroiddata`:

| MR | App | Comments | Days open |
| --- | --- | --- | --- |
| !45228 | Battery Tile | 4 | **0.4** |
| !45196 | Personal Health Records | 5 | **0.9** |
| !45207 | Hupl | 6 | **1.4** |
| !44637 | XMRPod | 19 | 5.6 |
| !44639 | ReteKey | 20 | 6.2 |
| !44632 | Terminator | 70 | 7.6 |
| !44443 | Malarm | 11 | 8.0 |
| !44161 | Neuron Encrypt | 83 | 9.7 |
| !44430 | BroadBoard | 13 | 10.0 |
| … | median of the 25 | **~15** | **~16** |
| **!44998** | **Vadhod APK Extractor** | **17** | **4** |

Two things follow, and they are not what I expected either.

**Our MR is unremarkable.** 17 comments and 4 days sits at the median for comments and well *below* it
for elapsed time. Submissions with 20, 27, 40, 70, 83 comments get merged routinely. It felt like a
disaster because most of the red icons were empty fork pipelines that never ran a job, not real failures.

**All three of the fastest merges used reproducible builds.** Every one of Battery Tile, Personal Health
Records and Hupl ships `Binaries:` plus `AllowedAPKSigningKeys:` — and they merged in under a day and a
half:

```yaml
Binaries: https://github.com/pgaskin/batterytile/releases/download/v%v/BatteryTile.apk
...
AllowedAPKSigningKeys: 08d46917a818ad0c479733b458acb7488a4cc39f0a5a20ccc6a39181b2fb8f55
```

So **reproducible builds are not the thing that costs rounds.** What costs rounds is submitting a repo
that was not prepared, and guessing at machine-enforced formatting instead of reading the tool output.

That said, §6 documents the no-reproducible-builds route properly, because it is a legitimate choice —
just not the one that makes review faster.

---

## 1. Rules that are machine-enforced — never write these from memory

Three CI jobs (`fdroid rewritemeta`, `fdroid lint`, `checkupdates`) rewrite your file and **fail if the
result differs from what you committed**. There is no style latitude. The order below is not a
convention; it is `yaml_app_field_order` in
[`fdroidserver/metadata.py`](https://gitlab.com/fdroid/fdroidserver/-/blob/master/fdroidserver/metadata.py),
and the `\n` entries in that list are literal blank lines in the output.

### App-level field order (blank lines are part of the spec)

```
Disabled, AntiFeatures, Categories, License, AuthorName, AuthorEmail, AuthorWebSite,
WebSite, SourceCode, IssueTracker, Translation, Changelog, Donate, Liberapay,
OpenCollective, Bitcoin, Litecoin
                                     <-- blank line
Name, AutoName, Summary, Description
                                     <-- blank line
RequiresRoot
                                     <-- blank line
RepoType, Repo, Binaries
                                     <-- blank line
Builds
                                     <-- blank line
AllowedAPKSigningKeys
                                     <-- blank line
MaintainerNotes
                                     <-- blank line
ArchivePolicy, AutoUpdateMode, UpdateCheckMode, UpdateCheckIgnore, VercodeOperation,
UpdateCheckName, UpdateCheckData, CurrentVersion, CurrentVersionCode
```

This single list would have prevented two of our four review rounds. `AutoName` goes above `RepoType`;
`AllowedAPKSigningKeys` goes *after* `Builds`, not before it.

### Build-entry field order

From `build_flags` in the same file. The ones that matter in practice:

```
versionName, versionCode, disable, commit, timeout, subdir, submodules, sudo, init,
patch, gradle, maven, output, binary, srclibs, oldsdkloc, encoding, forceversion,
forcevercode, rm, extlibs, prebuild, androidupdate, target, scanignore, scandelete,
build, buildjni, ndk, preassemble, gradleprops, antcommands, postbuild, novcheck,
antifeatures
```

**`gradle` comes before `prebuild`.** This is counter-intuitive — `prebuild` runs *first* but is
*written* later — and it cost us a round.

### Two formatting traps

- **A single-command `prebuild` is a plain scalar, not a one-item list.** Two or more commands stay a
  list.
  ```yaml
      prebuild: sed -i '/foojay-resolver/d' ../settings.gradle.kts    # correct for one command
  ```
- **A `Binaries:` URL long enough to wrap is emitted as the key with a trailing space**, URL indented
  beneath. That trailing space is significant. Verify with `cat -A` against a real file in fdroiddata:
  ```
  Binaries: $
    https://github.com/…/releases/download/v%v/app-v%v.apk$
  ```

### The rule that replaces all of the above

If CI complains about formatting, **read the diff the job prints and copy it verbatim.** `checkupdates`
and `fdroid rewritemeta` print the exact file they want. Reading it takes thirty seconds; guessing from
the docs cost us two rounds.

```bash
glab api "projects/fdroid%2Ffdroiddata/jobs/<JOB_ID>/trace" | tail -c 5000
```

---

## 2. Prepare the app repo *before* opening the MR

This is where nearly all the real savings are. Do all of it while you still control the timeline.

### 2.1 Tag the exact commit you build the release from

**This is the mistake that cost us the most.** Our v1.0 APK was built at `0dd05b39`; the `v1.0` tag was
applied a week later to `8605b025`. AGP stamps the git HEAD into
`META-INF/version-control-info.textproto`, so F-Droid's rebuild differed from the published binary by
that one string, and everything else matched byte for byte.

Order of operations, every release:

```bash
# 1. commit everything, including docs and CI
# 2. tag
git tag -a v1.2 -m "v1.2"
# 3. build the release FROM that state
./gradlew assembleRelease
# 4. push tag, upload the APK you just built
```

Never build, then commit more, then tag.

### 2.2 Turn off `vcsInfo` so the above stops mattering

```kotlin
android {
    buildTypes {
        release {
            vcsInfo { include = false }   // AGP 8.3+
        }
    }
}
```

With this, the APK no longer records which commit produced it, and a mis-ordered tag can never break
verification again. Do this once and forget it.

### 2.3 Remove the foojay toolchain plugin

`org.gradle.toolchains.foojay-resolver-convention` ships in the Android Studio new-project template and
is on F-Droid's blocklist — it is listed by name in
[`scanner.py`](https://gitlab.com/fdroid/fdroidserver/-/blob/master/fdroidserver/scanner.py) as
`Foojay Toolchains Plugin`, because it downloads a JDK at build time. The scanner treats it as a hard
error:

```
ERROR: Found usual suspect 'org.gradle.toolchains.foojay-resolver' at settings.gradle.kts
ERROR: Could not build app …: Can't build due to 1 error while scanning
```

Delete the line from `settings.gradle.kts`. If nothing in your project declares a Java toolchain (check
for `jvmToolchain`, `java { toolchain { … } }`), the plugin does nothing and removing it is free. An
empty `plugins { }` block is valid.

Every Compose app submitted this year hits this. Navic (!45296) hit it too, with the same fix.

### 2.4 Files the scanner deletes for you — don't work around them

From `scanner.py`, these are removed automatically before the build:

```
gradle-wrapper.jar, gradlew, gradlew.bat, gradle-daemon-jvm.properties, *.apk, *.a
```

So **do not write `prebuild` lines to patch `gradle-daemon-jvm.properties`** — it is already gone by the
time Gradle starts. We wrote two different `sed` commands against a file that never existed at build
time. If you need a specific JDK, use:

```yaml
      - echo "org.gradle.java.home=/usr/lib/jvm/java-21-openjdk-amd64" >> ../gradle.properties
```

though in practice the buildserver's default JDK built our AGP 9.3 / Gradle 9.6 / `compileSdk 37`
project with no pin at all.

### 2.5 Make a clean clone build unsigned

```kotlin
val keystorePropertiesFile = rootProject.file("keystore.properties")
buildTypes {
    release {
        if (keystorePropertiesFile.exists()) signingConfig = signingConfigs.getByName("release")
    }
}
```

F-Droid clones without your keystore. If the build hard-fails on a missing keystore, it cannot be built
at all.

### 2.6 Ship the metadata upstream

```
LICENSE                                    <-- a real FOSS licence file, required
fastlane/metadata/android/en-US/
├── title.txt
├── short_description.txt                  (≤ 80 chars)
├── full_description.txt
├── changelogs/<versionCode>.txt           (≤ 500 chars — one per release, forever)
└── images/
    ├── icon.png                           (512×512)
    └── phoneScreenshots/1.png …
```

`fdroid lint` reports what it picked up as an **Info** finding. Seeing your name, summary, screenshots
and icon listed there is confirmation, not a complaint.

### 2.7 Inclusion policy, in one pass

From the [Inclusion Policy](https://f-droid.org/en/docs/Inclusion_Policy/) — the disqualifiers:

- Not FLOSS, or a non-free dependency anywhere in the tree.
- **Proprietary tracking/advertising/analytics — Google Play Services, Firebase, Crashlytics — are
  "strictly forbidden".**
- Binary dependencies not built from source or from Debian.
- Downloading executable binaries without explicit user consent.
- Fails to rebuild from source; undisclosed anti-features; no maintenance commitment.

Anything network-facing invites discussion. Navic declares `INTERNET`, needs cleartext traffic for HTTP
servers, and contacts github.com on startup for update checks — all disclosed, all still discussion.
An app with no `INTERNET` permission has nothing to argue about.

---

## 3. Pre-flight checks you can run on Windows, without Docker

None of these need the buildserver. All of them would have caught real problems in `!44998`.

```bash
# 1. Does the tag point at the commit you actually built?
git rev-parse v1.0^{commit}

# 2. What commit does the published APK think it is? (must equal the above)
python -c "import zipfile;print(zipfile.ZipFile('app-release.apk').read('META-INF/version-control-info.textproto').decode())"

# 3. Is the foojay plugin still in there?
grep -n foojay settings.gradle.kts

# 4. Does the source tree differ between your build commit and your tag?
git diff --stat <build-commit> <tag> -- app/ gradle/ *.kts gradle.properties   # must be empty

# 5. Signing cert fingerprint — this is your AllowedAPKSigningKeys value
"$SDK/build-tools/36.0.0/apksigner.bat" verify --print-certs app-release.apk

# 6. Prove reproducibility yourself: rebuild from the tag in a clean clone and diff
git clone . /tmp/rb && cd /tmp/rb && git checkout <tag>
./gradlew assembleRelease
python - <<'EOF'
import zipfile
a=zipfile.ZipFile('published.apk'); b=zipfile.ZipFile('rebuilt.apk')
na=[i.filename for i in a.infolist()]; nb=[i.filename for i in b.infolist()]
print('same entries/order:', na==nb)
print('differing:', [n for n in na if n in nb and a.read(n)!=b.read(n)])
EOF
```

Check 6 is the one that matters. **You do not need Linux to test reproducibility of your own build** —
you need two builds of your own and a zip diff. I claimed for three rounds that this was untestable
without a Linux environment. That was wrong: the mismatch was a string in a text file inside the APK,
findable in one command.

---

## 4. Metadata template

Modelled on the three fastest-merged submissions, in the enforced order:

```yaml
Categories:
  - System
License: GPL-3.0-or-later
AuthorName: Your Name
AuthorEmail: you@example.com
WebSite: https://example.com/app/
SourceCode: https://github.com/you/app
IssueTracker: https://github.com/you/app/issues
Changelog: https://github.com/you/app/releases

AutoName: App Name

RepoType: git
Repo: https://github.com/you/app.git
Binaries: https://github.com/you/app/releases/download/v%v/app-v%v.apk

Builds:
  - versionName: '1.0'
    versionCode: 3
    commit: <full 40-char SHA, never the tag name>
    subdir: app
    gradle:
      - yes

AllowedAPKSigningKeys: <lowercase hex sha256, no colons>

AutoUpdateMode: Version
UpdateCheckMode: Tags ^v[0-9.]+$
CurrentVersion: '1.0'
CurrentVersionCode: 3
```

Notes drawn from the accepted files:

- `commit:` is the **full hash**, not the tag. Reviewers ask for this every time.
- `UpdateCheckMode: Tags` takes an optional regex, and the fast-merged apps all use one
  (`^v[0-9]+$`, `^v[\d.+]+$`, `^v[0-9.]+$`). It stops F-Droid tracking `-alpha`/`-rc` tags.
- `%v` = versionName, `%c` = versionCode, in both `Binaries` and the `AutoUpdateMode` tag pattern.
- `AutoName` is the **launcher label from the manifest**, which may differ from your store name. That is
  fine — F-Droid displays `fastlane/…/title.txt`. Do not add `Name:` to override it; the reference
  documentation says of `Name`: *"Don't use this."*

---

## 5. Reproducible builds: decide once, at inclusion, and it is final

The maintainer's words on `!44998`:

> We can't switch for signing by F-Droid to reproducible build. The signature can't be changed like this
> since the users can't get updates. You have to decide now if you want reproducible build.

F-Droid cannot migrate an app from their signing key to yours later, because the signature change breaks
updates for everyone who already installed it. **Inclusion is the only moment this is decidable.**

| | Reproducible (`Binaries` + `AllowedAPKSigningKeys`) | F-Droid signs |
| --- | --- | --- |
| Who signs the F-Droid APK | you | F-Droid |
| Users can move between your GitHub release and F-Droid | yes | **no — must uninstall** |
| CI must match your published binary byte-for-byte | yes | no |
| Reversible later | **no** | **no** |

What it obliges you to, forever:

- The release asset URL pattern must keep working: `…/releases/download/v<versionName>/<name>-v<versionName>.apk`.
- Every release must be built from the exact commit its tag points at (or `vcsInfo` disabled — §2.2).
- The signing key must never change.

---

## 6. If you deliberately skip reproducible builds

Omit exactly two fields — `Binaries` and `AllowedAPKSigningKeys` — and leave everything else identical:

```yaml
RepoType: git
Repo: https://github.com/you/app.git

Builds:
  - versionName: '1.0'
    ...

AutoUpdateMode: Version
```

Then in the MR description, leave **Enable Reproducible Builds** unchecked. This is what Navic (!45296)
did, and its `fdroid build` passed on the first buildserver run — because with no `Binaries`, the job
only has to compile the app; there is no published binary to download and compare.

Be clear-eyed about the trade: you are not buying a smoother review — the three fastest merges all used
reproducible builds — you are buying a build that cannot fail on a byte comparison. What you give up is
permanent, and it is a real cost to users who already have your APK installed.

---

## 7. Reading CI instead of guessing

Nine jobs run per pipeline. Only `fdroid build` compiles anything.

| Job | What it means when it fails |
| --- | --- |
| `fdroid rewritemeta` | Your field order/formatting differs from canonical. **Diff is printed — copy it.** |
| `checkupdates` | Same diff, plus tag/version resolution. Also prints `AutoName`. |
| `fdroid lint` | Metadata content problems; Info-level lines about fastlane are informational. |
| `schema validation` | Field name or type is not in the schema. |
| `check source code` / `tools check scripts` / `git redirect` | Repo-wide checks, rarely yours. |
| `fdroid build` | The real build: scanner, then Gradle, then (if `Binaries`) the binary comparison. |
| `check apk` | Runs only after a successful build. |

Pull any log directly rather than clicking through the UI:

```bash
glab api "projects/fdroid%2Ffdroiddata/merge_requests/<IID>/pipelines"
glab api "projects/fdroid%2Ffdroiddata/pipelines/<PIPELINE_ID>/jobs"
glab api "projects/fdroid%2Ffdroiddata/jobs/<JOB_ID>/trace" | tail -c 6000
```

### Fork pipelines are noise

Pipelines created on **your fork** may fail instantly with zero jobs and
`Pipeline creation failed`. Ours did, on every push. Other forks (e.g. `ssalggnikool/fdroiddata`) run all
nine jobs with identical project settings — `jobs_enabled`, `builds_access_level`,
`shared_runners_enabled`, `ci_config_path` and visibility all match, so **the cause is unknown**.

What matters: only pipelines in `fdroid/fdroiddata` on `refs/merge-requests/<IID>/head` test anything,
and those are triggered by maintainers. **Pressing "Run pipeline" on your fork adds a red row and
nothing else.** Do not do it, and do not let the red count alarm you — check the project ID:

```bash
glab api "projects/fdroid%2Ffdroiddata/merge_requests/<IID>/pipelines" \
  | python -c "import sys,json;[print(p['id'],p['status'],p['project_id']) for p in json.load(sys.stdin)]"
# project_id 36528 = fdroid/fdroiddata (real).  Anything else = your fork (ignore).
```

---

## 8. The mistakes from `!44998`, so they are not repeated

Recorded plainly, because most of the seventeen comments trace back to these.

| # | Mistake | Cost | What should have happened |
| --- | --- | --- | --- |
| 1 | Wrote metadata formatting from general knowledge instead of `rewritemeta`'s output | 1 round | Read the `checkupdates` job diff |
| 2 | Two `prebuild` commands invented against `gradle-daemon-jvm.properties` | 2 rounds | Read the build log — the scanner deletes it |
| 3 | Declared reproducibility "untestable without Linux" | the whole repro saga | Rebuild locally and zip-diff (§3, check 6) |
| 4 | Tagged `v1.0` on a later commit than the one built | 1 round + a re-release | Tag before building (§2.1) |
| 5 | Gave maintainers a confident, wrong explanation for the empty fork pipelines | a retraction | Compare against another fork first |
| 6 | Pressed "Run pipeline" on the fork | extra red rows | Only maintainers can run the real one |

The pattern in all six: **reasoning from how F-Droid probably works, instead of opening the artifact —
the log, the metadata file in fdroiddata, or the APK itself.** Every time a primary source was actually
consulted, the answer was right on the first attempt.

---

## 9. Ordered checklist for the next submission

1. Remove foojay from `settings.gradle.kts`; add `vcsInfo { include = false }`.
2. Confirm no `INTERNET`/Firebase/GMS/analytics, or be ready to justify each.
3. `LICENSE` + `fastlane/` metadata + `changelogs/<versionCode>.txt` committed.
4. Commit everything → tag → **then** build the release → upload that exact APK.
5. Run every check in §3. Fix locally. Nothing goes to fdroiddata until they pass.
6. Write the metadata using the §4 template and the §1 field order.
7. Decide reproducible builds **now** (§5). Recommended: yes.
8. Fork, branch named for the app ID, one commit, MR using the **App Inclusion template**.
9. When CI fails: pull the job trace, read it, fix what it says. Do not theorise.
10. Ignore fork pipelines entirely.
