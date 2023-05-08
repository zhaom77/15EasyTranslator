package com.example.translate.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.translate.adapter.SelectLanguageAdapter
import com.example.translate.databinding.ActivitySelectLanguageLayoutBinding
import com.example.translate.info.LanguageInfo

class SelectLanguageActivity: BaseActivity() {

    companion object {
        const val SELECT_TYPE = "select_type"
        const val SELECT_LANGUAGE_INFO = "select_language_info"
    }

    object SelectType {
        const val SELECT_SOURCE = 1
        const val SELECT_TARGET = 2
        const val SELECT_OCR = 3
    }

    private val mBinding by lazy { ActivitySelectLanguageLayoutBinding.inflate(layoutInflater) }

    private var mSelectLanguageInfo: LanguageInfo? = null

    private var mSelectType: Int = SelectType.SELECT_SOURCE

    override fun getRootView(): View = mBinding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSelectType = intent.getIntExtra(SELECT_TYPE, SelectType.SELECT_SOURCE)
        mBinding.apply {
            backImage.setOnClickListener {
                finish()
            }
            val adapter = SelectLanguageAdapter(this@SelectLanguageActivity, mSelectType) {
                mSelectLanguageInfo = it
                finish()
            }
            languageRecyclerView.layoutManager = LinearLayoutManager(this@SelectLanguageActivity)
            languageRecyclerView.adapter = adapter
        }
    }

    override fun finish() {
        setResult(1000, Intent().apply {
            putExtra(SELECT_LANGUAGE_INFO, mSelectLanguageInfo)
            putExtra(SELECT_TYPE, mSelectType)
        })
        super.finish()
    }
}