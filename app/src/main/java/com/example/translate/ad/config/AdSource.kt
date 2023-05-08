package com.example.translate.ad.config

import org.json.JSONObject

data class AdSource(val id: String, val level: Int, val type: String) {
    object Parser {
        fun parse(json: JSONObject): AdSource {
            return AdSource(
                json.getString("tr_id"),
                json.getInt("tr_pr"),
                json.getString("tr_type")
            )
        }
    }
}