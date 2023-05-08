package com.example.translate.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.translate.ad.AdManager
import com.example.translate.ad.AdPosition
import com.example.translate.databinding.ActivityMainLayoutBinding
import com.example.translate.manager.ConnectManager
import com.example.translate.manager.UserManager
import com.github.shadowsocks.bg.BaseService

class MainActivity : BaseActivity() {

    private val mBinding by lazy { ActivityMainLayoutBinding.inflate(layoutInflater) }

    override fun getRootView(): View = mBinding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.apply {
            textCardView.setOnClickListener {
                skip {
                    startActivity(Intent(this@MainActivity, TranslateActivity::class.java))
                }
            }

            ocrCardView.setOnClickListener {
                skip {
                    startActivity(Intent(this@MainActivity, TranslateOcrActivity::class.java))
                }
            }
            vpnCardView.setOnClickListener {
                skip {
                    startActivity(Intent(this@MainActivity, ConnectActivity::class.java))
                }
            }
            if (!UserManager.instance.isNormalUser) {
                vpnCardView.visibility = View.VISIBLE
                if (ConnectManager.mConnectState != BaseService.State.Connected) {
                    startActivity(Intent(this@MainActivity, ConnectGuideActivity::class.java))
                }
            } else {
                vpnCardView.visibility = View.GONE
            }
        }
        AdManager.instance.loadAd(this, AdPosition.TRANSLATE_NATIVE_AD)
    }

    private fun skip(cb: () -> Unit) {
        if (UserManager.instance.isNormalUser) {
            cb.invoke()
            return
        }
        AdManager.instance.showAd(this, AdPosition.INTERSTITIAL_AD, null) {
            cb.invoke()
        }
    }

    override fun onResume() {
        super.onResume()
        AdManager.instance.apply {
            loadAd(this@MainActivity, AdPosition.MAIN_NATIVE_AD) {
                if (it) {
                    showAd(this@MainActivity, AdPosition.MAIN_NATIVE_AD, mBinding.adLayout)
                }
            }
            loadAd(this@MainActivity, AdPosition.INTERSTITIAL_AD)
        }
    }
}