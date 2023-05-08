package com.example.translate.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.translate.ad.AdManager
import com.example.translate.ad.AdPosition
import com.example.translate.databinding.ActivityTranslateResultLayoutBinding
import com.example.translate.info.LanguageInfo
import com.example.translate.manager.CommunicationManager
import com.example.translate.manager.UserManager

class TranslateResultActivity : AdActivity() {

    companion object {
        const val SOURCE_TEXT = "source_text"
        const val TARGET_TEXT = "target_text"
        const val SOURCE_LANGUAGE_INFO = "source_language_info"
        const val TARGET_LANGUAGE_INFO = "target_language_info"
    }

    private val mBinding by lazy { ActivityTranslateResultLayoutBinding.inflate(layoutInflater) }

    override fun getRootView(): View = mBinding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sourceText = intent.getStringExtra(SOURCE_TEXT)
        val targetText = intent.getStringExtra(TARGET_TEXT)
        mBinding.apply {
            backImage.setOnClickListener {
                onBackPressed()
            }
            if (targetText == null) {
                translateFailLayout.visibility = View.VISIBLE
                translateResultLayout.visibility = View.GONE
                tryAgainButton.setOnClickListener {
                    finish()
                    CommunicationManager.instance.startTranslate()
                }
                if (UserManager.instance.isNormalUser) {
                    connectCardView.visibility = View.GONE
                }
                connectCardView.setOnClickListener {
                    skip {
                        startActivity(
                            Intent(
                                this@TranslateResultActivity, ConnectActivity::class.java
                            )
                        )
                        finish()
                        CommunicationManager.instance.startFinish(TranslateActivity::class.java)
                    }
                }
                ocrCardView.setOnClickListener {
                    skip {
                        startActivity(
                            Intent(
                                this@TranslateResultActivity, TranslateOcrActivity::class.java
                            )
                        )
                        finish()
                        CommunicationManager.instance.startFinish(TranslateActivity::class.java)
                    }
                }
            } else {
                translateFailLayout.visibility = View.GONE
                translateResultLayout.visibility = View.VISIBLE
                val sourceLanInfo: LanguageInfo? = intent.getParcelableExtra(SOURCE_LANGUAGE_INFO)
                val targetLanInfo: LanguageInfo? = intent.getParcelableExtra(TARGET_LANGUAGE_INFO)
                formerLanguageText.text = sourceLanInfo?.name
                formerWordText.text = sourceText
                translateLanguageText.text = targetLanInfo?.name
                translateWordText.text = targetText
            }
        }
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
            loadAd(this@TranslateResultActivity, AdPosition.MAIN_NATIVE_AD) {
                if (it) {
                    showAd(
                        this@TranslateResultActivity, AdPosition.MAIN_NATIVE_AD, mBinding.adLayout
                    )
                }
            }
            loadAd(this@TranslateResultActivity, AdPosition.INTERSTITIAL_AD)
        }
    }
}