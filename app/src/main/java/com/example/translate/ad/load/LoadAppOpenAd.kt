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
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class LoadAppOpenAd : LoadAd() {

    override suspend fun loadAd(context: Context, source: AdSource): AdInfo? {
        val deferred = CompletableDeferred<AdInfo?>()
        withContext(Dispatchers.Main) {
            val builder = AdRequest.Builder().build()
            AppOpenAd.load(context,
                source.id,
                builder,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(p0: AppOpenAd) {
                        Logger.d({ mTag }, { "app open ad load success" })
                        deferred.complete(AdInfo(AdType.OPEN, null, null, p0))
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Logger.d({ mTag }, { "app open ad load fail" })
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
            Logger.d({ mTag }, { "app open ad activity is pause" })
            complete?.invoke()
            return
        }
        val appOpenAd = CacheAd.getCacheAd(position)?.appOpenAd
        if (appOpenAd == null) {
            Logger.d({ mTag }, { "app open ad is null" })
            complete?.invoke()
            return
        }
        appOpenAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Logger.d({ mTag }, { "app open ad show fail" })
                complete?.invoke()
            }

            override fun onAdShowedFullScreenContent() {
                Logger.d({ mTag }, { "app open ad show success" })
            }

            override fun onAdDismissedFullScreenContent() {
                Logger.d({ mTag }, { "app open ad close" })
                complete?.invoke()
            }

            override fun onAdImpression() {

            }

            override fun onAdClicked() {

            }
        }
        appOpenAd.show(activity)
    }
}