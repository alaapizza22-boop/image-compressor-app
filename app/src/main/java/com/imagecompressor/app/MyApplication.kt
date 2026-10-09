package com.imagecompressor.app

import android.app.Application
import com.google.android.gms.ads.MobileAds

/**
 * Application entry point.
 *
 * - Initializes the AdMob SDK once for the whole process.
 * - Firebase is auto-initialized via the google-services plugin once
 *   google-services.json is added to the app/ module (see README_BUILD.md).
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) { /* initialization status callback, optional */ }
    }
}
