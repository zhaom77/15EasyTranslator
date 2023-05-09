package com.example.translate.ad.config

import org.json.JSONObject

data class AdPositionInfo(
    val enable: Boolean,
    val position: String,
    val sourceList: MutableList<AdSource>
) {

    object Parser {
        fun parse(json: JSONObject): AdPositionInfo {
            val enable = json.getBoolean("tr_enable")
            val position = json.getString("tr_position")
            val list = mutableListOf<AdSource>()
            val jsonArray = json.getJSONArray("tr_source")
            for (i in 0 until jsonArray.length()) {
                list.add(AdSource.Parser.parse(jsonArray.getJSONObject(i)))
            }
            list.sortByDescending { it.level }
            return AdPositionInfo(enable, position, list)
        }
    }

}