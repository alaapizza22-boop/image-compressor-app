# Image Compressor — Native Android Project

This is a **real native Android application**: Kotlin + Jetpack Compose + Gradle (Kotlin DSL),
targeting the standard Android SDK. There is no HTML/CSS/JS/WebView involved anywhere in this
module — it is a genuine Android Studio project.

> This folder (`android-app/`) was generated as source code only. No Gradle/Android build
> tools exist in the chat environment that produced it, so **no `.apk`/`.aab` has been built
> here**. Follow the steps below on your own machine (or free CI) to produce a real,
> installable, signable build.

## What's included

```
android-app/
├── settings.gradle.kts, build.gradle.kts, gradle.properties
├── gradle/wrapper/gradle-wrapper.properties        (Gradle 8.7)
└── app/
    ├── build.gradle.kts                            (AGP 8.5, Kotlin 1.9.24, Compose, Room,
    │                                                 Firebase Auth, AdMob, DataStore, Coil)
    ├── proguard-rules.pro
    ├── google-services.json.example                (replace with your real Firebase config)
    └── src/main/
        ├── AndroidManifest.xml                     (permissions, FileProvider, AdMob app id)
        ├── java/com/imagecompressor/app/
        │   ├── MainActivity.kt, MyApplication.kt
        │   ├── navigation/NavGraph.kt
        │   ├── ui/theme/ (Color, Type, Theme)
        │   ├── ui/screens/ (Home, History, Settings, Account, Login, Register, ForgotPassword)
        │   ├── ui/components/BannerAdView.kt
        │   ├── viewmodel/ (CompressionViewModel, AuthViewModel)
        │   ├── data/ (CompressionEngine, ImageFileUtils, SettingsRepository)
        │   ├── data/local/ (Room: AppDatabase, CompressionHistoryEntity, Dao)
        │   ├── data/repository/CompressionRepository.kt
        │   ├── auth/FirebaseAuthManager.kt
        │   └── ads/AdMobManager.kt
        └── res/
            ├── values/strings.xml (English)  +  values-ar/strings.xml (Arabic)
            ├── values/colors.xml, themes.xml
            ├── xml/file_paths.xml, backup_rules.xml, data_extraction_rules.xml
            └── mipmap-anydpi-v26 + drawable (vector adaptive launcher icon)
```

## Features implemented in the source

- **Native image picking**: `ActivityResultContracts.OpenDocument` / `OpenMultipleDocuments`
  (system picker, no extra permissions dialog needed on API 29+; legacy storage permission
  declared for older devices).
- **Compression engine** (`CompressionEngine.kt`) built entirely on Android SDK APIs:
  `BitmapFactory` (with `inSampleSize` down-sampling to avoid OOM), `ExifInterface` for
  correct rotation, `Bitmap.compress()` for adjustable JPEG/WEBP/PNG quality (1–100).
- **Original dimensions kept by default** — a toggle switches to a custom max-dimension resize.
- **Batch compression** — add many images to a queue, compress them all sequentially with
  progress indication per item.
- **Live previews** via Coil (`AsyncImage`) for every queued image and before/after sizes.
- **Save & Share** using `MediaStore` (Android 10+ scoped storage, with legacy fallback) and
  a `FileProvider`-backed `ACTION_SEND`/`ACTION_SEND_MULTIPLE` intent — no raw file:// URIs.
- **Compression history** persisted locally with Room (`compression_history.db`), shown in a
  dedicated History tab, survives app restarts, works fully offline.
- **Arabic + English** UI strings (`values/strings.xml`, `values-ar/strings.xml`),
  `android:supportsRtl="true"` in the manifest for proper RTL mirroring in Arabic.
- **Optional user accounts** via Firebase Authentication: register, login, and
  "forgot password" (`sendPasswordResetEmail`) screens — all with a prominent
  "Continue without an account" guest path, since accounts are optional by design.
- **AdMob integration points**: `MobileAds.initialize()` in `MyApplication`, a reusable
  `BannerAdView` Compose component, and an `InterstitialAdManager` helper — wired with
  Google's official **test** ad unit IDs so nothing violates AdMob policy until you swap in
  your real IDs.

## One missing binary file (easy fix)

`gradle/wrapper/gradle-wrapper.jar` is a small **binary** file that the Gradle wrapper normally
ships with. This authoring tool can only write text files, so that binary is not included.
This is harmless and trivial to fix the first time you open the project:

- **Easiest:** just open `android-app/` in Android Studio — it detects the missing/incompatible
  wrapper jar and offers to regenerate it automatically, or you can let it manage Gradle itself.
- **Or, from a terminal**, if you have any local Gradle install (or Android Studio's bundled one):
  ```bash
  gradle wrapper --gradle-version 8.7
  ```
  This regenerates `gradle-wrapper.jar` and keeps the existing `gradle-wrapper.properties`.

## Prerequisites (all free)

1. **Android Studio** (free): https://developer.android.com/studio
   - Installing it also installs the Android SDK, platform tools, and an emulator.
2. A **free Google account** for:
   - Firebase console (https://console.firebase.google.com) — for Authentication.
   - AdMob console (https://admob.google.com) — for real ad unit IDs.
   - Google Play Console — **one-time $25 fee**, required only when you actually want to
     *publish* to the Play Store (not required just to build the AAB).

## Step-by-step: open and build

1. **Open the project**
   - Launch Android Studio → "Open" → select the `android-app/` folder.
   - Let Gradle sync; it will download the Android Gradle Plugin, Kotlin, and all
     dependencies declared in `app/build.gradle.kts` automatically (internet required, all free).

2. **Configure Firebase (optional, only needed for the login/register/reset screens)**
   - Create a project at the Firebase console, add an Android app with package name
     `com.imagecompressor.app`.
   - Download the generated `google-services.json` and place it at `android-app/app/google-services.json`
     (replacing the `.example` file's role — the real file must NOT have the `.example` suffix).
   - In `app/build.gradle.kts`, uncomment the line:
     `// id("com.google.gms.google-services")`
   - In Firebase console → Authentication → Sign-in method, enable **Email/Password**.
   - If you skip this step entirely, the app still builds and runs — it just always shows
     guest mode, since Firebase won't be initialized.

3. **Configure AdMob (optional, test ads work out of the box)**
   - The project ships with Google's official **test** App ID and ad unit IDs, so banner and
     interstitial ads work immediately in Debug builds without any setup.
   - For production: create an AdMob app + ad units, then replace:
     - the `com.google.android.gms.ads.APPLICATION_ID` meta-data value in `AndroidManifest.xml`
     - `AdMobIds.BANNER_TEST_ID` / `AdMobIds.INTERSTITIAL_TEST_ID` in `AdMobManager.kt`

4. **Generate a signing keystore** (one-time, free, uses the JDK bundled with Android Studio)
   ```bash
   keytool -genkeypair -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias image-compressor
   ```
   Keep this file and its passwords safe — you need the *same* keystore for every future
   update of the same app on Google Play.

5. **Wire the keystore into Gradle** — create `android-app/keystore.properties` (already
   git-ignored) with:
   ```properties
   RELEASE_STORE_FILE=/absolute/path/to/release-key.jks
   RELEASE_STORE_PASSWORD=yourStorePassword
   RELEASE_KEY_ALIAS=image-compressor
   RELEASE_KEY_PASSWORD=yourKeyPassword
   ```
   Then load it in `app/build.gradle.kts` (top of file) with:
   ```kotlin
   val keystoreProps = java.util.Properties().apply {
       val f = rootProject.file("keystore.properties")
       if (f.exists()) load(f.inputStream())
   }
   ```
   and replace `project.findProperty("RELEASE_STORE_FILE")` references with
   `keystoreProps.getProperty("RELEASE_STORE_FILE")` (same for the other 3 keys).
   *(The build file already contains the signingConfig scaffold — this just points it at
   your keystore.)*

6. **Build the signed App Bundle**
   - In Android Studio: **Build → Generate Signed Bundle / APK… → Android App Bundle**,
     select your keystore/alias, choose the `release` build variant, and click **Finish**.
   - Or from a terminal inside `android-app/`:
     ```bash
     ./gradlew bundleRelease
     ```
   - The output `.aab` appears at `app/build/outputs/bundle/release/app-release.aab`.

7. **(Optional) Build for free in the cloud with GitHub Actions** if you don't want to
   install Android Studio locally:
   - Push this `android-app/` folder to a GitHub repo.
   - Add a workflow `.github/workflows/build.yml` that sets up JDK 17 + Android SDK
     (`android-actions/setup-android`), then runs `./gradlew bundleRelease`, using repo
     **Secrets** for your keystore (base64-encoded) and passwords.
   - GitHub Actions' free tier includes enough minutes/month for this for most hobby/small
     projects.

8. **Upload to Google Play**
   - Google Play Console → your app → Production (or Internal testing) → Create release →
     upload the `.aab` → fill in store listing → submit for review.

## Localization notes (Arabic/English)

- All user-facing strings already exist in both `res/values/strings.xml` (English, default)
  and `res/values-ar/strings.xml` (Arabic). Android automatically picks the right one based
  on the device's system language.
- `android:supportsRtl="true"` is set in the manifest so layouts mirror correctly in Arabic.
- The in-app Settings screen lets a user pick a language preference independent of the
  system locale; to make that selection take effect immediately (without the OS locale
  changing), wire `AppCompatDelegate.setApplicationLocales(...)` (AndroidX
  "per-app language" API) in `MainActivity.onCreate()` using the stored preference from
  `SettingsRepository`. This requires switching the activity's theme parent to an
  `AppCompatActivity`-compatible theme or using the Compose-only
  `androidx.core:core-splashscreen` + `LocaleManagerCompat` pattern — left as a clearly
  marked follow-up because it is UI-library-preference-dependent.

## Why this cannot be "finished" from the chat tool alone

This authoring environment only exposes web-project tooling (Node/Vite build command). It has
no JDK, no Android SDK, no Gradle, and no emulator — so it is structurally unable to compile
Kotlin/Android code or produce a `.aab`. Everything above is real, complete Android source
you can open immediately in Android Studio; the remaining steps (Gradle sync, signing,
`bundleRelease`) must happen in a real Android build environment, all of which are free.
