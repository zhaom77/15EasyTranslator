package com.example.translate

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Process
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.Utils
import com.example.translate.ad.AdManager
import com.example.translate.manager.ConfigManager
import com.example.translate.manager.FirebaseManager
import com.example.translate.manager.UserManager
import com.example.translate.ui.WelcomeActivity
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.*

class Translator : Application(), Utils.OnAppStatusChangedListener {

    //key alias: Easy
    //key store password: easy2023
    //key password: easy2023
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: Translator? = null

        val context: Translator by lazy { instance!! }
    }

    override fun attachBaseContext(base: Context?) {
        instance = this
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        /*try {
            Core.init(this, WelcomeActivity::class)
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
        if (getProcessName(Process.myPid()) != packageName) return

        try {
            //FirebaseApp.initializeApp(this)
            MobileAds.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        AppUtils.registerAppStatusChangedListener(this)
        FirebaseManager.instance.init(this)
        ConfigManager.instance.fetch {
            UserManager.instance.getConfigInfo(null)
            AdManager.instance.init(ConfigManager.instance.getAdConf())
        }
    }

    private fun getProcessName(pid: Int): String? {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfoList = am.runningAppProcesses ?: return null
        for (processInfo in processInfoList) {
            if (processInfo.pid == pid) {
                return processInfo.processName
            }
        }
        return null
    }

    override fun onForeground(activity: Activity?) {
        if (activity !is WelcomeActivity && activity !is AdActivity) {
            val mIntent = Intent(activity, WelcomeActivity::class.java)
            mIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            mIntent.putExtra(WelcomeActivity.INPUT_TYPE, true)
            startActivity(mIntent)
        }
    }

    override fun onBackground(activity: Activity?) {
        if (activity is AdActivity) {
            activity.finish()
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        AppUtils.unregisterAppStatusChangedListener(this)
    }
}