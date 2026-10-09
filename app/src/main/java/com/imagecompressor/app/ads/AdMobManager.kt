package com.imagecompressor.app.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * AdMob integration points.
 *
 * IMPORTANT: the ad unit IDs below are Google's official *test* IDs. Replace them with
 * your real AdMob ad unit IDs before release (see README_BUILD.md). Keep test IDs during
 * development to avoid policy violations from invalid traffic on your real account.
 */
object AdMobIds {
    const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/9214589741"
    const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
}

class InterstitialAdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null

    fun load(adUnitId: String = AdMobIds.INTERSTITIAL_TEST_ID) {
        val request = AdRequest.Builder().build()
        InterstitialAd.load(context, adUnitId, request, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
            }
        })
    }

    /** Shows the preloaded interstitial (e.g. after a batch compression completes). */
    fun showIfAvailable(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            onDismissed()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                onDismissed()
            }
        }
        ad.show(activity)
    }
}
