package com.example.translate.ad.load

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.translate.ad.AdPosition
import com.example.translate.ad.AdType
import com.example.translate.ad.config.AdSource
import com.example.translate.manager.Logger
import com.example.translate.ui.AdLayout
import com.example.translate.ui.BaseActivity
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class LoadNativeAd : LoadAd() {

    override suspend fun loadAd(context: Context, source: AdSource): AdInfo? {
        val deferred = CompletableDeferred<AdInfo?>()
        withContext(Dispatchers.Main) {
            var mNativeAd: NativeAd? = null
            val builder = AdLoader.Builder(context, source.id).forNativeAd { nativeAd ->
                    mNativeAd = nativeAd
                }.withAdListener(object : com.google.android.gms.ads.AdListener() {
                    override fun onAdClosed() {
                        mNativeAd?.destroy()
                        mNativeAd = null
                        Logger.d({ mTag }, { "native ad close" })
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Logger.d({ mTag }, { "native ad load fail" })
                        deferred.complete(null)
                    }

                    override fun onAdOpened() {
                    }

                    override fun onAdLoaded() {
                        Logger.d({ mTag }, { "native ad load success" })
                        if (mNativeAd != null) {
                            deferred.complete(AdInfo(AdType.NATIVE, mNativeAd, null, null))
                        } else {
                            deferred.complete(null)
                        }
                    }

                    override fun onAdClicked() {
                    }

                    override fun onAdImpression() {
                    }
                }).build()
            val adRequest = AdRequest.Builder().build()
            builder.loadAd(adRequest)
        }
        return deferred.await()
    }

    override fun showAd(
        activity: BaseActivity, position: AdPosition, adLayout: AdLayout?, complete: (() -> Unit)?
    ) {
        if (adLayout == null) {
            Logger.d({ mTag }, { "native dLayout is null" })
            complete?.invoke()
            return
        }
        if (activity.isPause) {
            Logger.d({ mTag }, { "native activity is pause" })
            complete?.invoke()
            return
        }
        val nativeAd = CacheAd.getCacheAd(position)?.nativeAd
        if (nativeAd == null) {
            Logger.d({ mTag }, { "native ad is null" })
            complete?.invoke()
            return
        }
        activity.lifecycle.addObserver(AdLifecycle(nativeAd))
        adLayout.showAd(nativeAd)
    }

    class AdLifecycle(private val ad: NativeAd) : DefaultLifecycleObserver {

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            ad.destroy()
        }
    }
}