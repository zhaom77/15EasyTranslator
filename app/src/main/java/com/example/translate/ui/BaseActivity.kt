package com.example.translate.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.example.translate.R
import com.example.translate.ex.statusBarHeight

abstract class BaseActivity : AppCompatActivity() {

    var isPause = false
    private var mContentLayout: FrameLayout? = null

//    private var isNavigationBar = false

    abstract fun getRootView(): View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = getRootView()
        setContentView(view)
        initStatusBar(view)
        setStatusBar(view)
/*        mContentLayout = findViewById(android.R.id.content)
        mContentLayout?.viewTreeObserver?.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mContentLayout?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                setNavigationBarPadding(mContentLayout!!)
            }

        })*/
    }

    override fun onResume() {
        super.onResume()
        isPause = false
    }

    override fun onPause() {
        super.onPause()
        isPause = true
    }

    private fun setStatusBar(view: View) {
        try {
            val statusView = view.findViewById<View>(R.id.status_view)
            statusView?.viewTreeObserver?.addOnGlobalLayoutListener(object :
                OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    statusView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    statusView.layoutParams.also { lp ->
                        lp.height = statusBarHeight
                    }.also { lp -> statusView.layoutParams = lp }
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initStatusBar(view: View) {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT
        val controller = ViewCompat.getWindowInsetsController(view)
        controller?.isAppearanceLightStatusBars = true
    }


    /**
     * 设置导航栏的距离，留出导航栏位置
     */
    /*private fun setNavigationBarPadding(content: FrameLayout) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return
        }
        val currentExist = isNavigationBarExist()
        if (currentExist != isNavigationBar) {
            isNavigationBar = currentExist
            if (currentExist) {
                val height = getNavigationBarHeight()
                if (height > 0) {
                    content.setPadding(
                        content.paddingLeft,
                        content.paddingTop,
                        content.paddingRight,
                        content.paddingBottom + height
                    )
                }
            } else {
                content.setPadding(
                    content.paddingLeft,
                    content.paddingTop,
                    content.paddingRight,
                    content.paddingBottom
                )
            }
        }
    }

    *//**
     * 获取导航栏的高度
     *//*
    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    private fun getNavigationBarHeight(): Int {
        val resources = this.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId)
        }
        return 0
    }

    *//**
     * 判断导航栏是否存在
     *//*
    private fun isNavigationBarExist(): Boolean {
        val vp = this.window.decorView as ViewGroup
        for (i in 0 until vp.childCount) {
            if (vp.getChildAt(i).id != -1 &&
                this.resources.getResourceEntryName(vp.getChildAt(i).id) == "navigationBarBackground"
            ) {
                return true
            }
        }
        return false
    }*/
}