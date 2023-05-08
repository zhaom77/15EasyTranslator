package com.example.translate.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.translate.R
import com.example.translate.ad.AdManager
import com.example.translate.ad.AdPosition
import com.example.translate.databinding.ActivityConnectResultLayoutBinding
import com.example.translate.manager.ConnectManager
import com.github.shadowsocks.bg.BaseService

class ConnectResultActivity : AdActivity() {

    private val mBinding by lazy { ActivityConnectResultLayoutBinding.inflate(layoutInflater) }

    override fun getRootView(): View = mBinding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.apply {
            if (ConnectManager.mConnectState == BaseService.State.Connected) {
                resultDescImage.setImageResource(R.mipmap.connect_success_desc)
                resultStateText.text = getString(R.string.connected)
                connectCountryText.visibility = View.VISIBLE
                connectCountryText.text = ConnectManager.mCurrentProfile?.name
            } else {
                resultDescImage.setImageResource(R.mipmap.disconnect_success_desc)
                resultStateText.text = getString(R.string.disconnected)
                connectCountryText.visibility = View.GONE
            }

            backImage.setOnClickListener {
                onBackPressed()
            }

            textCardView.setOnClickListener {
                finish()
                startActivity(Intent(this@ConnectResultActivity, TranslateActivity::class.java))
            }
            ocrCardView.setOnClickListener {
                finish()
                startActivity(Intent(this@ConnectResultActivity, TranslateOcrActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AdManager.instance.apply {
            loadAd(this@ConnectResultActivity, AdPosition.MAIN_NATIVE_AD) {
                if (it) {
                    showAd(this@ConnectResultActivity, AdPosition.MAIN_NATIVE_AD, mBinding.adLayout)
                }
            }
        }
    }
}