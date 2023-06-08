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
        private set
    private var mContentLayout: FrameLayout? = null

    abstract fun getRootView(): View

    override fun onCreate(savedInstanceState: Bundle?) {
        adaptScreen()
        super.onCreate(savedInstanceState)
        val view = getRootView()
        setContentView(view)
        initStatusBar(view)
        setStatusBar(view)
    }

    private fun adaptScreen() {
        resources.displayMetrics.apply {
            val finalHeight = heightPixels / 760f
            density = finalHeight
            scaledDensity = finalHeight
            densityDpi = (160 * finalHeight).toInt()
        }
    }

    override fun onResume() {
        super.onResume()
        isPause = false
    }

    override fun onPause() {
        super.onPause()
        isPause = true
    }

    override fun onStop() {
        super.onStop()
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
}