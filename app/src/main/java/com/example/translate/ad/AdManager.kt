package com.example.translate.ad

import android.content.Context
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import android.util.Base64
import com.example.translate.ad.config.AdConfig
import com.example.translate.ad.config.AdPositionInfo
import com.example.translate.ad.config.AdSource
import com.example.translate.ad.load.*
import com.example.translate.manager.ConfigManager
import com.example.translate.manager.Logger
import com.example.translate.ui.AdLayout
import com.example.translate.ui.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class AdManager {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AdManager() }
    }

    private var mAppOpenLoad: LoadAd? = null
    private var mInterstitialLoad: LoadAd? = null
    private var mNativeLoad: LoadAd? = null
    private val mCompleteListeners = hashMapOf<AdPosition, ((Boolean) -> Unit)>()
    private var mHandler = Handler(Looper.getMainLooper())
    private val mOutTimeMap = hashMapOf<AdPosition, Runnable>()
    private var mAdConfig: AdConfig? = null

    fun isShowBackAd(): Boolean = mAdConfig?.showBackAd ?: false

    fun init(adConfig: String) {
        try {
            val str = String(Base64.decode(adConfig, Base64.NO_WRAP))
            Logger.d({ LoadAd.mTag }, { "adConfig: $str" })
            val json = JSONObject(str)
            mAdConfig = AdConfig.Parser.parse(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAd(context: Context, position: AdPosition, complete: ((Boolean) -> Unit)? = null) {
        if (mAdConfig == null) {
            init(ConfigManager.instance.getAdConf())
        }
        if (mAdConfig == null) {
            complete?.invoke(false)
            Logger.d({ LoadAd.mTag }, { "ad config is null" })
            return
        }
        if (mAdConfig?.enable != true) {
            complete?.invoke(false)
            Logger.d({ LoadAd.mTag }, { "ad enable is false" })
            return
        }
        var info: AdPositionInfo? = null
        for (positionInfo in mAdConfig!!.positionList) {
            if (positionInfo.position == position.scene) {
                info = positionInfo
                break
            }
        }
        if (info == null) {
            complete?.invoke(false)
            Logger.d({ LoadAd.mTag }, { "ad position config is null" })
            return
        }
        loadAd(context, position, info, complete)
    }

    private fun loadAd(
        context: Context,
        position: AdPosition,
        positionInfo: AdPositionInfo,
        complete: ((Boolean) -> Unit)?
    ) {
        if (!isConnectNetwork(context)) {
            complete?.invoke(false)
            Logger.d({ LoadAd.mTag }, { "load ad no network" })
            return
        }
        if (!positionInfo.enable) {
            Logger.d({ LoadAd.mTag }, { "ad position enable: false position: $position" })
            complete?.invoke(false)
            return
        }
        if (CacheAd.hasCacheAd(position)) {
            Logger.d({ LoadAd.mTag }, { "ad has cache position: $position" })
            complete?.invoke(true)
            return
        }
        if (position.isRequesting) {
            Logger.d({ LoadAd.mTag }, { "ad isRequesting: true position: $position" })
            if (complete != null) mCompleteListeners[position] = complete
            return
        }
        position.isRequesting = true
        if (complete != null) mCompleteListeners[position] = complete
        val runnable = Runnable {
            loadComplete(position, false)
        }
        mHandler.postDelayed(runnable, 20000)
        GlobalScope.launch {
            for (source in positionInfo.sourceList) {
                val adInfo = loadAd(context, source)
                if (adInfo != null) {
                    CacheAd.addCacheAd(position, adInfo)
                    loadComplete(position, true)
                    return@launch
                }
            }
            loadComplete(position, false)
        }

    }

    private suspend fun loadAd(context: Context, source: AdSource): AdInfo? {
        return when (source.type) {
            AdType.NATIVE.type -> {
                if (mNativeLoad == null) {
                    mNativeLoad = LoadNativeAd()
                }
                mNativeLoad?.loadAd(context, source)
            }
            AdType.OPEN.type -> {
                if (mAppOpenLoad == null) {
                    mAppOpenLoad = LoadAppOpenAd()
                }
                mAppOpenLoad?.loadAd(context, source)
            }
            else -> {
                if (mInterstitialLoad == null) {
                    mInterstitialLoad = LoadInterstitialAd()
                }
                mInterstitialLoad?.loadAd(context, source)
            }
        }
    }

    private fun loadComplete(position: AdPosition, success: Boolean) =
        GlobalScope.launch(Dispatchers.Main) {
            val runnable = mOutTimeMap[position]
            if (runnable != null) {
                mHandler.removeCallbacks(runnable)
                mOutTimeMap.remove(position)
            }
            position.isRequesting = false
            mCompleteListeners[position]?.invoke(success)
            mCompleteListeners.remove(position)
        }

    private fun isConnectNetwork(context: Context): Boolean {
        val allNetworkInfo =
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.activeNetworkInfo
        return allNetworkInfo?.isConnected == true
    }

    fun showAd(
        activity: BaseActivity,
        position: AdPosition,
        adLayout: AdLayout? = null,
        complete: (() -> Unit)? = null
    ) {
        when (CacheAd.getAdType(position)) {
            AdType.NATIVE -> {
                if (mNativeLoad == null) {
                    mNativeLoad = LoadNativeAd()
                }
                mNativeLoad?.showAd(activity, position, adLayout, complete)
            }
            AdType.OPEN -> {
                if (mAppOpenLoad == null) {
                    mAppOpenLoad = LoadAppOpenAd()
                }
                mAppOpenLoad?.showAd(activity, position, adLayout, complete)
            }
            else -> {
                if (mInterstitialLoad == null) {
                    mInterstitialLoad = LoadInterstitialAd()
                }
                mInterstitialLoad?.showAd(activity, position, adLayout, complete)
            }
        }
    }

}