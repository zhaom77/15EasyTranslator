package com.example.translate.ad.load

import com.example.translate.ad.AdType
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import kotlin.math.abs

data class AdInfo(
    val adType: AdType,
    val nativeAd: NativeAd?,
    val interstitialAd: InterstitialAd?,
    val appOpenAd: AppOpenAd?
) {

    private val initTime = System.currentTimeMillis()

    fun isExpired(): Boolean {
        return abs(System.currentTimeMillis() - initTime) > 1000 * 60 * 59
    }
}