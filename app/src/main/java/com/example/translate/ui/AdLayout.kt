package com.example.translate.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.example.translate.databinding.ViewAdLayoutBinding
import com.example.translate.databinding.ViewAdmobNativeAdLayoutBinding
import com.example.translate.ex.layoutInflater
import com.google.android.gms.ads.nativead.NativeAd

class AdLayout(context: Context, attrSet: AttributeSet?) : FrameLayout(context, attrSet) {
    private val mBinding by lazy { ViewAdLayoutBinding.inflate(context.layoutInflater) }

    init {
        addView(mBinding.root)
    }

    fun showAd(nativeAd: NativeAd) {
        val binding = ViewAdmobNativeAdLayoutBinding.inflate(context.layoutInflater)
        val adView = binding.root
        adView.mediaView = binding.mediaView
        adView.headlineView = binding.adTitleText
        adView.bodyView = binding.adDescText
        adView.callToActionView = binding.adButton
        adView.iconView = binding.adIconImage
        (adView.headlineView as TextView).text = nativeAd.headline

        adView.bodyView?.visibility = View.INVISIBLE
        adView.callToActionView?.visibility = View.INVISIBLE
        adView.iconView?.visibility = View.GONE

        nativeAd.body?.let {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = it
        }
        nativeAd.callToAction?.let {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = it
        }
        nativeAd.icon?.let {
            adView.iconView?.visibility = View.VISIBLE
            (adView.iconView as ImageView).setImageDrawable(it.drawable)
        }
        nativeAd.mediaContent?.let {
            adView.mediaView?.setMediaContent(it)
        }
        adView.setNativeAd(nativeAd)

        adView.bodyView?.setOnClickListener(null)
        adView.mediaView?.setOnClickListener(null)
        adView.iconView?.setOnClickListener(null)
        adView.headlineView?.setOnClickListener(null)

        mBinding.adPlaceholderImage.visibility = View.GONE
        mBinding.adLayout.removeAllViews()
        mBinding.adLayout.addView(adView)
    }

}