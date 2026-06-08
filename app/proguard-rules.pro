# R8 / ProGuard rules for Vadhod APK Extractor (release builds).
#
# Compose, Coil 3, kotlinx-coroutines and DataStore all ship their own consumer rules, so this
# file is intentionally minimal. Add narrowly-scoped keeps here only when a release-build issue is
# actually observed (rules.md §A-6), never blanket -keep class **.

# Keep our domain enums referenced by name from DataStore (enumValueOf round-trips).
-keepclassmembers enum com.vadhod.apkextractor.core.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
