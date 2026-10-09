const FILE_TREE = `android-app/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
├── README_BUILD.md                 ← full step-by-step build guide
└── app/
    ├── build.gradle.kts            (AGP, Kotlin, Compose, Room, Firebase, AdMob)
    ├── proguard-rules.pro
    ├── google-services.json.example
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/imagecompressor/app/
        │   ├── MainActivity.kt, MyApplication.kt
        │   ├── navigation/NavGraph.kt
        │   ├── ui/theme/ (Color, Type, Theme)
        │   ├── ui/screens/ (Home, History, Settings, Account,
        │   │                Login, Register, ForgotPassword)
        │   ├── ui/components/BannerAdView.kt
        │   ├── viewmodel/ (CompressionViewModel, AuthViewModel)
        │   ├── data/ (CompressionEngine, ImageFileUtils, SettingsRepository)
        │   ├── data/local/ (Room: AppDatabase, HistoryEntity, Dao)
        │   ├── data/repository/CompressionRepository.kt
        │   ├── auth/FirebaseAuthManager.kt
        │   └── ads/AdMobManager.kt
        └── res/
            ├── values/strings.xml (English)
            ├── values-ar/strings.xml (Arabic)
            ├── values/colors.xml, themes.xml
            ├── xml/ (FileProvider paths, backup rules)
            └── mipmap-anydpi-v26 + drawable (adaptive launcher icon)`;

const FEATURES = [
  "Native image picking via ActivityResultContracts (system picker)",
  "Compression engine built on BitmapFactory + ExifInterface + Bitmap.compress (SDK-only, no 3rd-party native lib)",
  "Adjustable quality (1–100) and original-dimensions-by-default with optional custom max size",
  "Batch compression queue with per-item progress and live Coil previews",
  "Save to gallery via MediaStore + Share via FileProvider intents",
  "Compression history persisted locally with Room, survives restarts",
  "Full English and Arabic string resources with RTL layout support",
  "Optional Firebase Authentication: register, login, password reset, guest mode",
  "AdMob integration points: banner composable + interstitial manager (test IDs wired in)",
];

const STEPS = [
  "Install Android Studio (free) — it bundles the Android SDK and an emulator.",
  "Open the android-app/ folder in Android Studio and let Gradle sync.",
  "(Optional) Add your google-services.json for Firebase Authentication.",
  "(Optional) Swap in your real AdMob app id / ad unit ids before release.",
  "Generate a signing keystore with keytool (one-time, free).",
  "Build → Generate Signed Bundle / APK → Android App Bundle, or run ./gradlew bundleRelease.",
  "Upload the resulting .aab to the Google Play Console.",
];

export default function App() {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <div className="mx-auto max-w-4xl px-6 py-14">
        <div className="mb-8 rounded-xl border border-amber-500/40 bg-amber-500/10 p-5">
          <p className="text-sm font-semibold uppercase tracking-wide text-amber-400">
            Important — read before anything else
          </p>
          <p className="mt-2 text-amber-100">
            This web page is <span className="font-bold">not</span> the product you asked
            for. It is only an index/readme served by this chat tool, which can only run a
            web build command. The actual deliverable — a real native Android Studio
            project written in Kotlin + Jetpack Compose — lives as source files in this
            project's <code className="rounded bg-black/40 px-1.5 py-0.5">android-app/</code>{" "}
            folder. No <code className="rounded bg-black/40 px-1.5 py-0.5">.aab</code> or{" "}
            <code className="rounded bg-black/40 px-1.5 py-0.5">.apk</code> has been built
            anywhere — this environment has no JDK, Android SDK, or Gradle to do that. Open
            the project in Android Studio (free) to build, sign, and ship it.
          </p>
        </div>

        <header className="mb-10">
          <div className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-teal-500 shadow-lg shadow-indigo-900/40">
            <svg viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth={2} className="h-7 w-7">
              <rect x="3" y="3" width="18" height="18" rx="3" />
              <path d="M8 13l3 3 5-6" />
            </svg>
          </div>
          <h1 className="mt-4 text-3xl font-bold tracking-tight">
            Image Compressor — Native Android Project
          </h1>
          <p className="mt-2 text-slate-400">
            Kotlin · Jetpack Compose · Android SDK · Gradle · Firebase Auth · AdMob
          </p>
        </header>

        <section className="mb-10">
          <h2 className="mb-3 text-xl font-semibold">What was generated</h2>
          <p className="mb-4 text-slate-400">
            A complete Android Studio project scaffold (Gradle Kotlin DSL, manifest, Compose
            UI, ViewModels, Room database, compression engine, Firebase Auth hooks, AdMob
            hooks, and English/Arabic resources). File tree:
          </p>
          <pre className="overflow-x-auto rounded-lg bg-slate-900 p-4 text-xs leading-relaxed text-slate-300 ring-1 ring-slate-800">
            {FILE_TREE}
          </pre>
        </section>

        <section className="mb-10">
          <h2 className="mb-3 text-xl font-semibold">Features implemented in the source</h2>
          <ul className="space-y-2">
            {FEATURES.map((f) => (
              <li key={f} className="flex gap-3 text-slate-300">
                <span className="mt-1 h-1.5 w-1.5 shrink-0 rounded-full bg-teal-400" />
                <span>{f}</span>
              </li>
            ))}
          </ul>
        </section>

        <section className="mb-10">
          <h2 className="mb-3 text-xl font-semibold">How to get a signed .aab (free tools)</h2>
          <ol className="space-y-2">
            {STEPS.map((s, i) => (
              <li key={s} className="flex gap-3 text-slate-300">
                <span className="mt-0.5 flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-indigo-500/20 text-sm font-semibold text-indigo-300">
                  {i + 1}
                </span>
                <span>{s}</span>
              </li>
            ))}
          </ol>
          <p className="mt-4 text-sm text-slate-500">
            Full details, including the keystore-signing snippet and an optional GitHub
            Actions CI recipe, are in{" "}
            <code className="rounded bg-black/40 px-1.5 py-0.5">android-app/README_BUILD.md</code>.
          </p>
        </section>

        <footer className="border-t border-slate-800 pt-6 text-sm text-slate-500">
          This environment's tools only support creating/editing text files and running a
          web build. It cannot invoke Gradle, the Android SDK, or an emulator. The Kotlin,
          XML, and Gradle files in <code className="rounded bg-black/40 px-1 py-0.5">android-app/</code>{" "}
          were authored as real text for you to compile in Android Studio — they were not
          compiled or tested by this tool.
        </footer>
      </div>
    </div>
  );
}
