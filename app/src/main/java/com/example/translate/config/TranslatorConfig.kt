package com.example.translate.config

import com.example.translate.manager.UserPlan
import com.example.translate.manager.UserType
import com.tencent.mmkv.MMKV

object TranslatorConfig {

    private val mConfig: MMKV = MMKV.mmkvWithID("translator_config")

    var mlType: Int
        get() {
            return mConfig.decodeInt("ml_type", UserType.NONE.type)
        }
        set(value) {
            mConfig.encode("ml_type", value)
        }

    var userPlan: Int
        get() {
            return mConfig.decodeInt("user_plan", UserPlan.NONE.plan)
        }
        set(value) {
            mConfig.encode("user_plan", value)
        }

/*    var isShowConnectGuide: Boolean
        get() {
            return mConfig.decodeBool("is_show_connect_guide", true)
        }
        set(value) {
            mConfig.encode("is_show_connect_guide", value)
        }*/

    var sourceLanCode: String
        get() {
            return mConfig.decodeString("source_lan_code", "en") ?: "en"
        }
        set(value) {
            mConfig.encode("source_lan_code", value)
        }

    var targetLanCode: String
        get() {
            return mConfig.decodeString("target_lan_code", "hi") ?: "hi"
        }
        set(value) {
            mConfig.encode("target_lan_code", value)
        }

    var ocrLanCode: String
        get() {
            return mConfig.decodeString("ocr_lan_code", "latin") ?: "latin"
        }
        set(value) {
            mConfig.encode("ocr_lan_code", value)
        }

    var firstUseOcr: Boolean
        get() {
            return mConfig.decodeBool("first_use_ocr", true)
        }
        set(value) {
            mConfig.encode("first_use_ocr", value)
        }

    var firstUseTranslate: Boolean
        get() {
            return mConfig.decodeBool("first_use_translate", true)
        }
        set(value) {
            mConfig.encode("first_use_translate", value)
        }
}