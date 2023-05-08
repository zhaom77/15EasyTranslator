package com.example.translate.manager

import android.app.Activity

class CommunicationManager {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { CommunicationManager() }
    }

    private var mStartTranslateCb: (() -> Unit)? = null
    private val mFinishListeners = hashMapOf<String, () -> Unit>()

    fun setTranslateListener(cb: () -> Unit) {
        mStartTranslateCb = cb
    }

    fun startTranslate() {
        mStartTranslateCb?.invoke()
    }

    fun <T : Activity> setFinishListener(clazz: Class<T>, cb: () -> Unit) {
        mFinishListeners[clazz.name] = cb
    }

    fun <T : Activity> startFinish(clazz: Class<T>) {
        mFinishListeners[clazz.name]?.invoke()
        mFinishListeners.remove(clazz.name)
    }

}