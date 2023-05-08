package com.example.translate.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.translate.R
import com.example.translate.config.TranslatorConfig
import com.example.translate.databinding.ItemSelectLanguageLayoutBinding
import com.example.translate.ex.layoutInflater
import com.example.translate.info.LanguageInfo
import com.example.translate.manager.TranslateManager
import com.example.translate.ui.SelectLanguageActivity

class SelectLanguageAdapter(
    private val context: Context,
    type: Int,
    private val selectListener: (LanguageInfo) -> Unit
) :
    Adapter<SelectLanguageAdapter.SelectLanguageViewHolder>() {


    class SelectLanguageViewHolder(val view: ItemSelectLanguageLayoutBinding) :
        ViewHolder(view.root)

    private val mList = arrayListOf<LanguageInfo>()
    private var mSelectCode = ""

    init {
        when (type) {
            SelectLanguageActivity.SelectType.SELECT_SOURCE -> {
                mSelectCode = TranslatorConfig.sourceLanCode
                mList.addAll(TranslateManager.instance.mSupportLanguageList)
            }
            SelectLanguageActivity.SelectType.SELECT_TARGET -> {
                mSelectCode = TranslatorConfig.targetLanCode
                val list = TranslateManager.instance.mSupportLanguageList
                mList.addAll(list.subList(1, list.size))
            }
            else -> {
                mSelectCode = TranslatorConfig.ocrLanCode
                mList.addAll(TranslateManager.instance.mOcrLanguageList)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectLanguageViewHolder {
        return SelectLanguageViewHolder(
            ItemSelectLanguageLayoutBinding.inflate(
                context.layoutInflater,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = mList.size

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: SelectLanguageViewHolder, position: Int) {
        mList[position].apply {
            holder.view.run {
                languageText.text = name
                selectImage.setImageResource(if (mSelectCode == code) R.mipmap.icon_select else R.mipmap.icon_select_normal)
                root.setOnClickListener {
                    if (mSelectCode != code) {
                        mSelectCode = code
                        selectListener.invoke(this@apply)
                        notifyDataSetChanged()
                    }
                }
                if (position >= mList.size - 1) {
                    lineView.visibility = View.GONE
                } else {
                    lineView.visibility = View.VISIBLE
                }
            }
        }
    }

}