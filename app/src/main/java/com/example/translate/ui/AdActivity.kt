package com.example.translate.ui

import com.example.translate.ad.AdManager
import com.example.translate.ad.AdPosition
import com.example.translate.manager.UserManager

abstract class AdActivity : BaseActivity() {

    override fun onBackPressed() {
        if (UserManager.instance.isNormalUser || !AdManager.instance.isShowBackAd()) {
            super.onBackPressed()
        } else {
            AdManager.instance.showAd(this, AdPosition.INTERSTITIAL_AD) {
                super.onBackPressed()
            }
        }
    }
}