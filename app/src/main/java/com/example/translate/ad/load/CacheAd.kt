package com.example.translate.ad.load

import com.example.translate.ad.AdPosition
import com.example.translate.ad.AdType

object CacheAd {

    private val mCacheAds = hashMapOf<AdPosition, AdInfo>()

    fun addCacheAd(position: AdPosition, adInfo: AdInfo) {
        synchronized(mCacheAds) {
            mCacheAds[position] = adInfo
        }
    }

    fun getAdType(position: AdPosition): AdType? {
        return mCacheAds[position]?.adType
    }

    fun hasCacheAd(position: AdPosition): Boolean {
        val adInfo = mCacheAds[position]
        if (adInfo != null) {
            return if (adInfo.isExpired()) {
                mCacheAds.remove(position)
                false
            } else {
                true
            }
        }
        return false
    }

    fun getCacheAd(position: AdPosition): AdInfo? {
        synchronized(mCacheAds) {
            val adInfo = mCacheAds[position]
            mCacheAds.remove(position)
            return adInfo
        }
    }
}