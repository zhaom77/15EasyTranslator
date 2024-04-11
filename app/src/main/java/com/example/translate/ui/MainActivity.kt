package com.example.translate.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.translate.ad.AdManager
import com.example.translate.ad.AdPosition
import com.example.translate.databinding.ActivityMainLayoutBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            /*vpnCardView.setOnClickListener {
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
            }*/
        }
        AdManager.instance.loadAd(this@MainActivity, AdPosition.INTERSTITIAL_AD)
    }

    private fun skip(cb: () -> Unit) {
        cb.invoke()
/*        if (UserManager.instance.isNormalUser) {
            cb.invoke()
            return
        }
        AdManager.instance.showAd(this, AdPosition.INTERSTITIAL_AD, null) {
            cb.invoke()
        }*/
    }

    private var lastShownTms = 0L
    override fun onResume() {
        super.onResume()

        fun showNativeAd() {
            AdManager.instance.apply {
                loadAd(this@MainActivity, AdPosition.MAIN_NATIVE_AD) {
                    if (it && !isPause) {
                        lastShownTms = System.currentTimeMillis()
                        showAd(this@MainActivity, AdPosition.MAIN_NATIVE_AD, mBinding.adLayout)

                        loadAd(this@MainActivity, AdPosition.MAIN_NATIVE_AD)
                    }
                }
            }
        }

        if (System.currentTimeMillis() - lastShownTms < 45_000L) return
        lifecycleScope.launch {
            delay(100L)
            if (isPause) return@launch
            showNativeAd()
        }
    }
}