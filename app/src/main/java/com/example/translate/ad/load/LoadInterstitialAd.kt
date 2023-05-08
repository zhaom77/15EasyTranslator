package com.example.translate.ad.load

import android.content.Context
import com.example.translate.ad.AdPosition
import com.example.translate.ad.AdType
import com.example.translate.ad.config.AdSource
import com.example.translate.manager.Logger
import com.example.translate.ui.AdLayout
import com.example.translate.ui.BaseActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class LoadInterstitialAd : LoadAd() {

    override suspend fun loadAd(context: Context, source: AdSource): AdInfo? {
        val deferred = CompletableDeferred<AdInfo?>()
        withContext(Dispatchers.Main) {
            val builder = AdRequest.Builder().build()
            InterstitialAd.load(context, source.id, builder, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(p0: InterstitialAd) {
                    Logger.d({ mTag }, { "interstitial ad load success" })
                    deferred.complete(AdInfo(AdType.INTERSTITIAL, null, p0, null))
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Logger.d({ mTag }, { "interstitial ad load fail: ${p0.message}" })
                    deferred.complete(null)
                }
            })
        }
        return deferred.await()
    }

    override fun showAd(
        activity: BaseActivity,
        position: AdPosition,
        adLayout: AdLayout?, complete: (() -> Unit)?
    ) {
        if (activity.isPause) {
            Logger.d({ mTag }, { "interstitial ad activity is pause" })
            complete?.invoke()
            return
        }
        val interstitialAd = CacheAd.getCacheAd(position)?.interstitialAd
        if (interstitialAd == null) {
            Logger.d({ mTag }, { "interstitial is null" })
            complete?.invoke()
            return
        }
        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Logger.d({ mTag }, { "interstitial ad show fail" })
                complete?.invoke()
            }

            override fun onAdShowedFullScreenContent() {
                Logger.d({ mTag }, { "interstitial ad show success" })
            }

            override fun onAdDismissedFullScreenContent() {
                Logger.d({ mTag }, { "interstitial ad close" })
                complete?.invoke()
            }

            override fun onAdImpression() {

            }

            override fun onAdClicked() {

            }
        }
        interstitialAd.show(activity)
    }
}