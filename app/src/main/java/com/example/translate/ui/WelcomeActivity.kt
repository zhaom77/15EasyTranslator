package com.example.translate.ui

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.animation.addListener
import com.example.translate.ad.AdManager
import com.example.translate.ad.AdPosition
import com.example.translate.ad.load.LoadAd
import com.example.translate.databinding.ActivityWelcomeLayoutBinding
import com.example.translate.manager.FirebaseManager
import com.example.translate.manager.Logger
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel

class WelcomeActivity : BaseActivity() {

    companion object {
        const val INPUT_TYPE = "input_type"
        const val TAG = "WelcomeActivity"
    }

    private val mBinding: ActivityWelcomeLayoutBinding by lazy {
        ActivityWelcomeLayoutBinding.inflate(
            layoutInflater
        )
    }

    private var mProgressAnim: ValueAnimator? = null
    private var mIsBack = false
//    private var mConnectManager: ConnectManager? = null

    override fun getRootView(): View = mBinding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mIsBack = intent.getBooleanExtra(INPUT_TYPE, false)
        FirebaseManager.instance.onEvent(FirebaseManager.EventType.LOAD_SHOW, Bundle().apply {
            putString("type", if (mIsBack) "h" else "c")
        })
        startAnim(15000) {
            gotoMain()
        }
        startDownloadTransMode()
        loadAd()
/*        UserManager.instance.apply {
            getConfigInfo {
                if (isDestroyed) return@getConfigInfo
*//*                if (isPlanA) {
                    mConnectManager = ConnectManager(
                        this@WelcomeActivity,
                        object : ConnectManager.OnConnectListener {
                            override fun onObtainPermissionFail() {
                                loadAd()
                            }

                            override fun onObtainStateComplete() {
                                if (ConnectManager.mConnectState == BaseService.State.Connected) {
                                    loadAd()
                                } else {
                                    mProgressAnim?.cancel()
                                    mConnectManager?.switchConnect()
                                }
                            }

                            override fun onConnectStateChange(state: BaseService.State) {
                                when (state) {
                                    BaseService.State.Connecting -> {
                                        startAnim((1000 - mBinding.progressBar.progress) * 15L) {
                                            gotoMain()
                                        }
                                    }
                                    else -> {
                                        loadAd()
                                    }
                                }
                            }

                        })
                    mConnectManager?.startConnect()
                    FirebaseManager.instance.onEvent(FirebaseManager.EventType.QD_LJ)
                } else {*//*
                    loadAd()
//                }
            }
        }*/
    }


    private fun loadAd() {
        AdManager.instance.apply {
            if (!isPause) {
                loadAd(this@WelcomeActivity, AdPosition.WELCOME_AD) {
                    val waitTime = mBinding.progressBar.progress * 15
                    val time = if (waitTime > 2000) {
                        200L
                    } else {
                        2000L - waitTime + 200
                    }
                    startAnim(time) {
                        if (it && !isPause) {
                            showAd(this@WelcomeActivity, AdPosition.WELCOME_AD, null) {
                                gotoMain()
                            }
                        } else {
                            gotoMain()
                        }
                    }
                }
            } else {
                finish()
            }
/*            if (ConnectManager.mConnectState == BaseService.State.Connected) {
                FirebaseManager.instance.onEvent(FirebaseManager.EventType.QD_LJ_SUC)
                loadAd(this@WelcomeActivity, AdPosition.INTERSTITIAL_AD)
                loadAd(this@WelcomeActivity, AdPosition.MAIN_NATIVE_AD)
                loadAd(this@WelcomeActivity, AdPosition.TRANSLATE_NATIVE_AD)
            } else {*/
            loadAd(this@WelcomeActivity, AdPosition.MAIN_NATIVE_AD)
            loadAd(this@WelcomeActivity, AdPosition.TRANSLATE_NATIVE_AD)
//            }
        }

    }

    private fun gotoMain() {
        if (!isDestroyed && !mIsBack) {
            Logger.d({ LoadAd.mTag }, { "start main activity" })
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }

    private fun startAnim(duration: Long, onEnd: (() -> Unit)? = null) {
        mProgressAnim?.cancel()
        mProgressAnim = ValueAnimator.ofInt(mBinding.progressBar.progress, 1000)
        mProgressAnim?.apply {
            this.duration = duration
            addUpdateListener {
                mBinding.progressBar.progress = it.animatedValue as Int
            }

            var isCancel = false
            addListener(onCancel = {
                isCancel = true
            }, onEnd = {
                if (!isCancel) onEnd?.invoke()
            })
            start()
        }
    }


    override fun onBackPressed() = Unit

    private fun startDownloadTransMode() {
        try {
            val modeManager = RemoteModelManager.getInstance()
            val enModel = TranslateRemoteModel.Builder(TranslateLanguage.ENGLISH).build()
            val hindiModel = TranslateRemoteModel.Builder(TranslateLanguage.HINDI).build()
            val conditions = DownloadConditions.Builder().requireWifi().build()
            modeManager.download(enModel, conditions)
            modeManager.download(hindiModel, conditions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}