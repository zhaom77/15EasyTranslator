package com.example.translate.ui

abstract class AdActivity : BaseActivity() {

    override fun onBackPressed() {
        super.onBackPressed()
/*        if (UserManager.instance.isNormalUser || !AdManager.instance.isShowBackAd()) {
            super.onBackPressed()
        } else {
            AdManager.instance.showAd(this, AdPosition.INTERSTITIAL_AD) {
                super.onBackPressed()
            }
        }*/
    }
}