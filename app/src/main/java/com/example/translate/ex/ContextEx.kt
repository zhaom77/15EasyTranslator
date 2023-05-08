package com.example.translate.ex

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.WindowManager

val Context.statusBarHeight: Int
    @SuppressLint("InternalInsetResource")
    get() {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = runCatching { resources.getDimensionPixelSize(resourceId) }.getOrDefault(0)
        }

        if (result == 0) {
            result = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                24f,
                resources.displayMetrics
            ).toInt()
        }

        return result
    }


val Context.layoutInflater: LayoutInflater
    get() {
        return getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

/**
 * 获取屏幕的显示区域
 *
 * @return point.x:屏幕宽度 point.y:屏幕高度
 *
 */
val Context.screenSize: Point
    get() {
        val p = Point()
        val wm: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            p.x = bounds.width()
            p.y = bounds.height()
        } else {
            @Suppress("DEPRECATION")
            wm.defaultDisplay?.getRealSize(p)
        }
        return p
    }