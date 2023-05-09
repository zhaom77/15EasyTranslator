package com.example.translate.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import com.example.translate.R
import com.example.translate.ad.AdManager
import com.example.translate.ad.AdPosition
import com.example.translate.config.TranslatorConfig
import com.example.translate.databinding.ActivityTranslateLayoutBinding
import com.example.translate.dialog.LoadingDialog
import com.example.translate.info.LanguageInfo
import com.example.translate.manager.CommunicationManager
import com.example.translate.manager.FirebaseManager
import com.example.translate.manager.TranslateManager
import kotlinx.coroutines.*
import kotlin.coroutines.resume

class TranslateActivity : AdActivity() {

    companion object {
        const val TRANSLATE_TEXT = "translate_text"
        const val START_AUTO_TRANSLATE = "start_auto_translate"
    }

    private val mBinding by lazy { ActivityTranslateLayoutBinding.inflate(layoutInflater) }

    private var mSourceLanInfo =
        TranslateManager.instance.getLanguageInfo(TranslatorConfig.sourceLanCode)

    private var mTargetLanInfo =
        TranslateManager.instance.getLanguageInfo(TranslatorConfig.targetLanCode)

    private var mSelectResult: ActivityResultLauncher<Intent>? = null

    override fun getRootView(): View = mBinding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inputText = intent.getStringExtra(TRANSLATE_TEXT) ?: ""
        val isAutoTranslate = intent.getBooleanExtra(START_AUTO_TRANSLATE, false)
        val firstInput = TranslatorConfig.firstUseTranslate
        mBinding.apply {
            leftText.text = mSourceLanInfo.name
            rightText.text = mTargetLanInfo.name
            translateEdit.addTextChangedListener {
                deleteImage.visibility = if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            if (firstInput) {
                translateEdit.setText(inputText.ifEmpty { getString(R.string.translate_default_lan) })
            } else {
                translateEdit.setText(inputText)
            }
            backImage.setOnClickListener {
                onBackPressed()
            }
            translateButton.setOnClickListener {
                if (firstInput) {
                    guideBgView.visibility = View.GONE
                    clickTranslateText.visibility = View.GONE
                }
                startTranslate()
            }
            deleteImage.setOnClickListener {
                translateEdit.setText("")
            }
            sourceLayout.setOnClickListener {
                mSelectResult?.launch(Intent(
                    this@TranslateActivity, SelectLanguageActivity::class.java
                ).apply {
                    putExtra(
                        SelectLanguageActivity.SELECT_TYPE,
                        SelectLanguageActivity.SelectType.SELECT_SOURCE
                    )
                })
            }
            targetLayout.setOnClickListener {
                mSelectResult?.launch(Intent(
                    this@TranslateActivity, SelectLanguageActivity::class.java
                ).apply {
                    putExtra(
                        SelectLanguageActivity.SELECT_TYPE,
                        SelectLanguageActivity.SelectType.SELECT_TARGET
                    )
                })
            }
            CommunicationManager.instance.setTranslateListener {
                startTranslate()
            }
            initSelectResult()
            if (firstInput && !isAutoTranslate) {
                TranslatorConfig.firstUseTranslate = false
                guideBgView.visibility = View.VISIBLE
                clickTranslateText.visibility = View.VISIBLE
                guideBgView.setOnClickListener {
                    guideBgView.visibility = View.GONE
                    clickTranslateText.visibility = View.GONE
                    startTranslate()
                }
            }
            if (isAutoTranslate) {
                startTranslate()
            }
        }
    }

    override fun onBackPressed() {
        if (mBinding.guideBgView.visibility != View.VISIBLE) super.onBackPressed()
    }

    private fun startTranslate() {
        val sourceText = mBinding.translateEdit.text.toString()
        if (sourceText.isEmpty()) {
            Toast.makeText(this, R.string.not_text_tip, Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseManager.instance.onEvent(FirebaseManager.EventType.START_TRANS)
        val dialog = LoadingDialog(this@TranslateActivity, R.string.translating)
        dialog.show()
        GlobalScope.launch {
            var targetText: String? = null
            val translate = async {
                targetText = translate(sourceText)
                1
            }
            var loadSuccess = false
            val load = async {
                loadSuccess = loadAd()
                1
            }
            translate.await() + load.await()
            withContext(Dispatchers.Main) {
                dialog.setComplete {
                    if (loadSuccess) {
                        AdManager.instance.showAd(
                            this@TranslateActivity, AdPosition.INTERSTITIAL_AD
                        ) {
                            gotoResult(sourceText, targetText)
                        }
                    } else {
                        gotoResult(sourceText, targetText)
                    }
                }
            }
        }

    }

    private fun gotoResult(sourceText: String, targetText: String?) {
        FirebaseManager.instance.onEvent(if (targetText.isNullOrEmpty()) FirebaseManager.EventType.TRANS_FAIL else FirebaseManager.EventType.TRANS_SUCCESS)
        startActivity(Intent(
            this@TranslateActivity, TranslateResultActivity::class.java
        ).apply {
            putExtra(TranslateResultActivity.SOURCE_TEXT, sourceText)
            putExtra(TranslateResultActivity.TARGET_TEXT, targetText)
            putExtra(
                TranslateResultActivity.SOURCE_LANGUAGE_INFO, mSourceLanInfo
            )
            putExtra(
                TranslateResultActivity.TARGET_LANGUAGE_INFO, mTargetLanInfo
            )
        })
        CommunicationManager.instance.setFinishListener(TranslateActivity::class.java) {
            finish()
        }
    }

    private suspend fun translate(sourceText: String): String? {
        val sourceLanCode = if (mSourceLanInfo.code == "-1") {
            TranslateManager.instance.getTextLan(sourceText)
        } else {
            mSourceLanInfo.code
        }
        if (sourceLanCode.isNullOrEmpty()) {
            return null
        }
        return TranslateManager.instance.translate(
            sourceText, sourceLanCode, mTargetLanInfo.code
        )
    }

    private suspend fun loadAd(): Boolean = suspendCancellableCoroutine {
        var outTime = false
        val job = GlobalScope.launch {
            delay(10000)
            outTime = true
            it.resume(false)
        }
        AdManager.instance.loadAd(this, AdPosition.INTERSTITIAL_AD) { success ->
            if (!outTime) {
                job.cancel()
                it.resume(success)
            }
        }
        AdManager.instance.loadAd(this, AdPosition.MAIN_NATIVE_AD)
    }

    private fun initSelectResult() {
        mSelectResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val type = it.data?.getIntExtra(
                    SelectLanguageActivity.SELECT_TYPE,
                    SelectLanguageActivity.SelectType.SELECT_SOURCE
                )
                val info: LanguageInfo? =
                    it.data?.getParcelableExtra(SelectLanguageActivity.SELECT_LANGUAGE_INFO)
                if (info != null) {
                    if (type == SelectLanguageActivity.SelectType.SELECT_SOURCE) {
                        TranslatorConfig.sourceLanCode = info.code
                        mSourceLanInfo = info
                        mBinding.leftText.text = info.name
                    } else {
                        TranslatorConfig.targetLanCode = info.code
                        mTargetLanInfo = info
                        mBinding.rightText.text = info.name
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        AdManager.instance.apply {
            loadAd(this@TranslateActivity, AdPosition.TRANSLATE_NATIVE_AD) {
                if (it) {
                    showAd(
                        this@TranslateActivity, AdPosition.TRANSLATE_NATIVE_AD, mBinding.adLayout
                    )
                }
            }
        }
    }

}