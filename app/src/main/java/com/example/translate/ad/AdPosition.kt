package com.example.translate.ad

enum class AdPosition(val scene: String, var isRequesting: Boolean) {
    WELCOME_AD("welcome_ad", false),
    MAIN_NATIVE_AD("main_native_ad", false),//主页、连接结果页面、翻译结果页共用
    TRANSLATE_NATIVE_AD("translate_native_ad", false),//连接页面、翻译页共用
    INTERSTITIAL_AD("interstitial_ad", false)//主页点击、翻译结果、ocr识别结果、v连接/断开、翻译结果点击、返回
}