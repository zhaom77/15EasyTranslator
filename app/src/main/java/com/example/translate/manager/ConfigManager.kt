package com.example.translate.manager

import com.example.translate.R
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig

class ConfigManager {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { ConfigManager() }
    }

    private val mRemoteConfig = Firebase.remoteConfig

    init {
        mRemoteConfig.setDefaultsAsync(R.xml.default_config)
    }

    fun fetch(cb: (Task<Boolean>) -> Unit) = mRemoteConfig.fetchAndActivate().addOnCompleteListener(cb)

    fun getAdConf(): String = mRemoteConfig.getString("ad_conf")

    fun getProgrammeAPer(): Long = mRemoteConfig.getLong("serve_rate")

    fun getConnects(): String = mRemoteConfig.getString("connects")

    fun getMode(): Long = mRemoteConfig.getLong("mode")
}