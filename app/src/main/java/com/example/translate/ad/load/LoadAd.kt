package com.example.translate.ad.load

import android.content.Context
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import com.example.translate.ad.AdPosition
import com.example.translate.ad.config.AdPositionInfo
import com.example.translate.ad.config.AdSource
import com.example.translate.manager.Logger
import com.example.translate.ui.AdLayout
import com.example.translate.ui.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class LoadAd {

    companion object {
        const val mTag = "LoadAd"
    }

    abstract suspend fun loadAd(context: Context, source: AdSource): AdInfo?

    abstract fun showAd(
        activity: BaseActivity,
        position: AdPosition,
        adLayout: AdLayout? = null,
        complete: (() -> Unit)? = null
    )

}