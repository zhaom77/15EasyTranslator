package com.example.translate.ui

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.core.animation.addListener
import com.example.translate.R
import com.example.translate.ad.AdManager
import com.example.translate.ad.AdPosition
import com.example.translate.databinding.ActivityConnectLayoutBinding
import com.example.translate.manager.ConnectManager
import com.example.translate.manager.FirebaseManager
import com.github.shadowsocks.bg.BaseService

class ConnectActivity : BaseActivity(), ConnectManager.OnConnectListener {

    companion object {
        const val IS_AUTO_CONNECT = "is_auto_connect"
    }

    private val mBinding by lazy { ActivityConnectLayoutBinding.inflate(layoutInflater) }

    private val mConnectManager = ConnectManager(this, this)

    private var mIsAutoConnect = false

    private var mRotateAnimation: RotateAnimation? = null
    private var mProgressAnim: ValueAnimator? = null
    private var mCurrentProgress = 0
    private var mIsLoading = false

    override fun getRootView(): View = mBinding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mIsAutoConnect = intent.getBooleanExtra(IS_AUTO_CONNECT, false)
        mConnectManager.startConnect()
        mBinding.apply {
            backImage.setOnClickListener {
                if (mIsLoading) return@setOnClickListener
                finish()
            }
            connectLayout.setOnClickListener {
                if (mIsLoading) {
                    Toast.makeText(
                        this@ConnectActivity, R.string.wait_process_tip, Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                startConnect()
            }
            connectDescImage.setOnClickListener {
                if (mIsLoading) {
                    Toast.makeText(
                        this@ConnectActivity, R.string.wait_process_tip, Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                startConnect()
            }
        }
    }

    private fun startConnect() {
        mIsLoading = true
        startLoadingAnim()
        mCurrentProgress = 0
        val isConnect = ConnectManager.mConnectState != BaseService.State.Connected
        mBinding.connectButtonText.text = if (isConnect) {
            String.format(getString(R.string.connecting), "$mCurrentProgress%")
        } else {
            String.format(getString(R.string.disconnecting), "$mCurrentProgress%")
        }
        if (!isConnect) {
            startProgress(15000, false) {
                setConnectCompleteResult()
            }
        }
        if (isConnect) {
            FirebaseManager.instance.onEvent(FirebaseManager.EventType.VH_LJ)
            mConnectManager.switchConnect()
        } else {
            loadAd(false)
        }
    }

    private fun setConnectState() {
        mBinding.apply {
            if (ConnectManager.mConnectState == BaseService.State.Connected) {
                connectDescImage.setImageResource(R.mipmap.connected_desc)
                connectButtonImage.setImageResource(R.mipmap.icon_connected_button)
                connectButtonText.text = getString(R.string.vpn_is_on)
            } else {
                connectDescImage.setImageResource(R.mipmap.disconnect_desc)
                connectButtonImage.setImageResource(R.mipmap.icon_disconnect_button)
                connectButtonText.text = getString(R.string.start_connect)
            }
        }
    }

    private fun startLoadingAnim() {
        mBinding.connectButtonImage.setImageResource(R.mipmap.connect_loading)
        mRotateAnimation?.cancel()
        mRotateAnimation = RotateAnimation(
            0f, 360f, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 1500
            repeatCount = -1
            interpolator = LinearInterpolator()
        }
        mBinding.connectButtonImage.startAnimation(mRotateAnimation)
    }

    private fun startProgress(duration: Long, isConnect: Boolean, onEnd: (() -> Unit)? = null) {
        mProgressAnim?.cancel()
        mProgressAnim = ValueAnimator.ofInt(mCurrentProgress, 100)
        mProgressAnim?.apply {
            this.duration = duration
            addUpdateListener {
                mCurrentProgress = it.animatedValue as Int
                mBinding.connectButtonText.text = if (isConnect) {
                    String.format(getString(R.string.connecting), "$mCurrentProgress%")
                } else {
                    String.format(getString(R.string.disconnecting), "$mCurrentProgress%")
                }
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

    private fun setConnectCompleteResult() {
        mIsLoading = false
        mRotateAnimation?.cancel()
        mRotateAnimation = null
        mProgressAnim?.cancel()
        mProgressAnim = null
        setConnectState()
        startActivity(Intent(this@ConnectActivity, ConnectResultActivity::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        mRotateAnimation?.cancel()
        mRotateAnimation = null
        mProgressAnim?.cancel()
        mProgressAnim = null
    }


    override fun onObtainPermissionFail() {
        setConnectCompleteResult()
    }

    override fun onObtainStateComplete() {
        setConnectState()
        if (ConnectManager.mConnectState != BaseService.State.Connected) {
            if (mIsAutoConnect) {
                startConnect()
            } /*else {
                mBinding.guideBgView.visibility = View.VISIBLE
                mBinding.guideText.visibility = View.VISIBLE
                mBinding.guideBgView.setOnClickListener {
                    if (mIsLoading) return@setOnClickListener
                    mBinding.guideBgView.visibility = View.GONE
                    mBinding.guideText.visibility = View.GONE
                    startConnect()
                }
            }*/
        }
    }

    override fun onBackPressed() {
        if (mIsLoading) return
        super.onBackPressed()
    }

    override fun onConnectStateChange(state: BaseService.State) {
        when (state) {
            BaseService.State.Connecting -> {
                startProgress(15000, true) {
                    setConnectCompleteResult()
                }
            }
            BaseService.State.Idle, BaseService.State.Connected, BaseService.State.Stopped -> {
                if (state == BaseService.State.Connected) {
                    FirebaseManager.instance.onEvent(FirebaseManager.EventType.VH_LJ_SUC)
                    loadAd(true)
                } else {
                    setConnectCompleteResult()
                }

            }
            else -> {}
        }
    }

    private fun loadAd(isConnect: Boolean) {
        AdManager.instance.apply {
            loadAd(this@ConnectActivity, AdPosition.INTERSTITIAL_AD) {
                val waitTime = mCurrentProgress * 150
                val time = if (isConnect) {
                    if (waitTime >= 2000) {
                        200L
                    } else {
                        2000 - waitTime + 200L
                    }
                } else {
                    if (waitTime >= 8000) {
                        200L
                    } else {
                        8000 - waitTime + 200L
                    }
                }
                startProgress(time, isConnect) {
                    if (it) {
                        showAd(this@ConnectActivity, AdPosition.INTERSTITIAL_AD, null) {
                            if (!isConnect) {
                                mConnectManager.switchConnect()
                            } else {
                                setConnectCompleteResult()
                            }
                        }
                    } else {
                        if (!isConnect) {
                            mConnectManager.switchConnect()
                        } else {
                            setConnectCompleteResult()
                        }
                    }
                }
            }
            loadAd(this@ConnectActivity, AdPosition.TRANSLATE_NATIVE_AD)
            loadAd(this@ConnectActivity, AdPosition.MAIN_NATIVE_AD)
            loadAd(this@ConnectActivity, AdPosition.WELCOME_AD)
        }
    }

    override fun onResume() {
        super.onResume()
        AdManager.instance.apply {
            loadAd(this@ConnectActivity, AdPosition.TRANSLATE_NATIVE_AD) {
                if (it && !isPause) {
                    showAd(this@ConnectActivity, AdPosition.TRANSLATE_NATIVE_AD, mBinding.adLayout)
                }
            }
        }
    }
}