package com.imagecompressor.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.imagecompressor.app.ads.AdMobIds

/**
 * Drop-in Compose banner ad, placed e.g. at the bottom of HomeScreen/HistoryScreen.
 * Uses Google's official test ad unit ID by default — swap in your real unit ID
 * (AdMobIds) before shipping to Google Play.
 */
@Composable
fun BannerAdView(adUnitId: String = AdMobIds.BANNER_TEST_ID, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
