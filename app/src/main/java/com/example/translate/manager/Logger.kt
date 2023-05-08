package com.example.translate.manager

import android.util.Log
import com.example.translate.BuildConfig

object Logger {

    fun d(tagCb:() -> String, cb: () -> String) {
        if (BuildConfig.DEBUG) {
            Log.d(tagCb.invoke(), cb.invoke())
        }
    }

    fun e(tagCb:() -> String, cb: () -> String) {
        if (BuildConfig.DEBUG) {
            Log.e(tagCb.invoke(), cb.invoke())
        }
    }
}