plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
   
 
    // Uncomment once google-services.json has been added (see README_BUILD.md)
    // id("com.google.gms.google-services")
}

android {
    namespace = "com.imagecompressor.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.imagecompressor.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            // Fill these from gradle.properties / environment variables / CI secrets.
            // NEVER commit a real keystore or its passwords to source control.
            val storeFilePath = project.findProperty("RELEASE_STORE_FILE") as String? ?: ""
            if (storeFilePath.isNotBlank()) {
                storeFile = file(storeFilePath)
                storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String? ?: ""
                keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String? ?: ""
                keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String? ?: ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if ((project.findProperty("RELEASE_STORE_FILE") as String?)?.isNotBlank() == true) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room (compression history persistence)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // DataStore (settings: language, default quality)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // EXIF handling for correct rotation during compression
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // Image loading for previews
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Firebase Authentication (register / login / password reset)
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth-ktx")

    // Google AdMob (banner + interstitial placeholders)
    implementation("com.google.android.gms:play-services-ads:23.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
