# Third-party licences

Vadhod APK Extractor is licensed under **GPL-3.0-or-later** (see [`LICENSE`](LICENSE)). This file
records third-party material redistributed **inside the app binary**, which carries its own licence
obligations independent of the app's own licence.

Third-party *libraries* are not listed here: they are resolved by Gradle at build time and each
carries its own licence in its own artifact. See [`libraries-used.md`](libraries-used.md) for the
dependency ledger. This file covers material that ships **embedded in the APK**.

## Nunito (typeface)

The app bundles the Nunito variable font at `app/src/main/res/font/nunito.ttf` and uses it for all
typography.

| | |
| --- | --- |
| Copyright | Copyright 2014 The Nunito Project Authors (https://github.com/googlefonts/nunito) |
| Licence | SIL Open Font License, Version 1.1 |
| Version | 3.602 (variable) |
| Upstream | https://github.com/googlefonts/nunito |
| Full text | [`app/src/main/assets/licenses/OFL-1.1-Nunito.txt`](app/src/main/assets/licenses/OFL-1.1-Nunito.txt) |

The copyright line and licence above are taken verbatim from the font file's own `name` table
(name IDs 0, 13 and 14), so they describe the exact binary that ships — not a version found
elsewhere. The full licence text is bundled as an app asset so that it travels with every copy of
the APK, as OFL 1.1 requires, and is reproduced in the source tree at the path above.

Under OFL 1.1 the font may be bundled and redistributed, including with software sold or licensed
under other terms (such as this app's GPL-3.0-or-later), provided the copyright notice and licence
are included and the font is not sold on its own. The font is used unmodified and is **not**
distributed under a Reserved Font Name.
