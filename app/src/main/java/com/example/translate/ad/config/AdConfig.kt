package com.example.translate.ad.config

import org.json.JSONObject

data class AdConfig(val enable: Boolean, val showBackAd: Boolean, val positionList: MutableList<AdPositionInfo>) {

    object Parser {
        fun parse(json: JSONObject): AdConfig {
            val enable = json.getBoolean("tr_enable")
            val showBackAd = json.optBoolean("tr_show_back_ad")
            val list = mutableListOf<AdPositionInfo>()
            val jsonArray = json.getJSONArray("tr_configs")
            for (i in 0 until jsonArray.length()) {
                list.add(AdPositionInfo.Parser.parse(jsonArray.getJSONObject(i)))
            }
            return AdConfig(enable, showBackAd, list)
        }
    }

}