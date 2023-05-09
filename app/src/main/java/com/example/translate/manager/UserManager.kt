package com.example.translate.manager

import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.example.translate.Translator
import com.example.translate.ad.AdManager
import com.example.translate.config.TranslatorConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.*
import kotlin.coroutines.resume

class UserManager {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { UserManager() }
        const val TAG = "UserManager"
    }

    val isNormalUser: Boolean
        get() = TranslatorConfig.mlType != UserType.ML.type || ConfigManager.instance.getMode() == 1L

    val isPlanA
        get() = !isNormalUser && TranslatorConfig.userPlan == UserPlan.A.plan


    fun getConfigInfo(complete: () -> Unit) = GlobalScope.launch {
        var rate = ConfigManager.instance.getProgrammeAPer()
        val config = async {
            rate = getConfig()
            1
        }
        val refer = async {
            startRefer()
            1
        }
        config.await() + refer.await()
        Logger.d(
            { TAG },
            { "rate: $rate type: ${TranslatorConfig.mlType} plan: ${TranslatorConfig.userPlan}" })
        if (TranslatorConfig.userPlan == UserPlan.NONE.plan) {
            Logger.d({ TAG }, { "start setting user type" })
            TranslatorConfig.userPlan = if (TranslatorConfig.mlType != UserType.ML.type) {
                UserPlan.B.plan
            } else {
                FirebaseManager.instance.onEvent(FirebaseManager.EventType.AB_RAM)
                val random = RandomManager.random()
                Logger.d({ TAG }, { "random: $random" })
                if (rate >= random) {
                    FirebaseManager.instance.onEvent(FirebaseManager.EventType.AB_A)
                    UserPlan.A.plan
                } else {
                    FirebaseManager.instance.onEvent(FirebaseManager.EventType.AB_B)
                    UserPlan.B.plan
                }
            }
        }
        Logger.d({ TAG }, { "set complete user plan a: $isPlanA" })
        withContext(Dispatchers.Main) {
            val adConf = ConfigManager.instance.getAdConf()
            withContext(Dispatchers.IO) {
                AdManager.instance.init(adConf)
            }
            complete.invoke()
        }
    }

    private suspend fun getConfig() = suspendCancellableCoroutine {
        ConfigManager.instance.fetch { _ ->
            val rate = ConfigManager.instance.getProgrammeAPer()
            it.resume(rate)
        }
    }


    private suspend fun startRefer() = suspendCancellableCoroutine {
        if (TranslatorConfig.mlType != UserType.NONE.type) {
            it.resume(Unit)
            return@suspendCancellableCoroutine
        }
        FirebaseManager.instance.onEvent(FirebaseManager.EventType.EY_STAR_REF)
        val referrerClient = InstallReferrerClient.newBuilder(Translator.context).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(p0: Int) {
                var hasref = false
                when (p0) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        try {
                            val referrer = referrerClient.installReferrer.installReferrer ?: ""

                            if (referrer.contains("fb4a")) {
                                hasref = true
                                TranslatorConfig.mlType = UserType.ML.type
                                FirebaseManager.instance.onEvent(FirebaseManager.EventType.EY_REF_ML)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }
                    }

                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                    }
                }
                if (!hasref) {
                    TranslatorConfig.mlType = UserType.NORMAL.type
                    FirebaseManager.instance.onEvent(FirebaseManager.EventType.EY_RER_NOR)
                }
                try {
                    referrerClient.endConnection()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                it.resume(Unit)
            }

            override fun onInstallReferrerServiceDisconnected() {
            }

        })
    }

}