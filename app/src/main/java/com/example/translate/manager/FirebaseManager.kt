package com.example.translate.manager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseManager {

    companion object {
        const val TAG = "FirebaseManager"
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { FirebaseManager() }
    }

    private var mAnalytics: FirebaseAnalytics? = null

    @SuppressLint("MissingPermission")
    fun init(context: Context) {
        if (mAnalytics == null) {
            mAnalytics = FirebaseAnalytics.getInstance(context)
        }
    }

    /**
     * 事件打点
     */
    fun onEvent(event: String, bundle: Bundle? = null) {
        Logger.d({ TAG }, { "event：$event parameter：${bundle}" })
        mAnalytics?.logEvent(getRealValue(event, 40), bundle)
    }

    /**
     * 判断action参数值长度不能超过指定值
     * 官方文档规定最长可到40
     */
    private fun getRealValue(value: String, limit: Int): String {
        if (value.length > limit) {
            return value.substring(0, limit)
        }
        return value
    }

    object EventType {

        //启动页展示
        const val LOAD_SHOW = "load_show"

        //开始进行referrer时
        const val EY_STAR_REF = "ey_star_ref"

        //referrer结果为买量用户
        const val EY_REF_ML = "ey_ref_ml"

        //referrer结果为普通用户
        const val EY_RER_NOR = "ey_rer_nor"

        //启动页开始进行a/b方案随机逻辑
        const val AB_RAM = "ab_ram"

        //随机为启动页a方案
        const val AB_A = "ab_a"

        //随机为启动页b方案
        const val AB_B = "ab_b"

        //启动页开始连接v
        const val QD_LJ = "qd_lj"

        //启动页连接v成功
        const val QD_LJ_SUC = "qd_lj_suc"

        //入口页开始连接v
        const val VH_LJ = "vh_lj"

        //入口页连接v成功
        const val VH_LJ_SUC = "vh_lj_suc"

        //入口页展示引导蒙层
        const val VG_SHOW = "vg_show"

        //v连接引导蒙层点击try it now按钮
        const val VG_CLK = "vg_clk"

        //开始执行翻译
        const val START_TRANS = "start_trans"

        //翻译成功
        const val TRANS_SUCCESS = "trans_success"

        //翻译失败
        const val TRANS_FAIL = "trans_fail"

        //开始ocr识别
        const val START_OCR = "start_ocr"

        //ocr识别成功
        const val OCR_SUCCESS = "ocr_success"

        //ocr识别失败
        const val OCR_FAIL = "ocr_fail"
    }

}